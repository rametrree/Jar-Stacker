package com.jar.jarstacker.stack.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SplitPlacementResolver {

	// 8 horizontal unit directions: N, NE, E, SE, S, SW, W, NW
	private static final double[][] DIRECTIONS = {
		{ 0.0, -1.0}, // N
		{ 0.7071, -0.7071}, // NE
		{ 1.0,  0.0}, // E
		{ 0.7071,  0.7071}, // SE
		{ 0.0,  1.0}, // S
		{-0.7071,  0.7071}, // SW
		{-1.0,  0.0}, // W
		{-0.7071, -0.7071}  // NW
	};

	// Bounded radial distances in blocks (0.95 ensures non-overlapping AABB for standard 0.9-width mobs)
	private static final double[] RADII = { 0.95, 0.70, 0.45 };

	/**
	 * Finds a safe split position for newEntity around source animal,
	 * ensuring:
	 * 1. level.noCollision for the entity AABB at the candidate position.
	 * 2. Raycast/sweep from source to candidate does not cross fences, walls, or solid obstacles.
	 * 3. Ground support exists beneath candidate.
	 * 4. Minimal/zero AABB overlap with reserved entity bounding boxes.
	 * 5. Conservative fallback to source position if cramped.
	 */
	public static Vec3 findSafeSplitPosition(ServerLevel level, Mob source, Entity newEntity, Set<AABB> reservedBoxes) {
		return findSafeSplitPosition(level, source, newEntity, reservedBoxes, null);
	}

	/**
	 * Finds a safe split position for newEntity around source animal,
	 * ensuring:
	 * 1. level.noCollision for the entity AABB at the candidate position.
	 * 2. Raycast/sweep from source to candidate does not cross fences, walls, or solid obstacles.
	 * 3. Ground support exists beneath candidate.
	 * 4. Minimal/zero AABB overlap with reserved entity bounding boxes.
	 * 5. Player view-ray clearance (preferred positions away from player's line of sight to anchor).
	 * 6. Conservative fallback to source position if cramped.
	 */
	public static Vec3 findSafeSplitPosition(ServerLevel level, Mob source, Entity newEntity, Set<AABB> reservedBoxes, Entity viewer) {
		Vec3 srcPos = source.position();
		AABB baseAABB = newEntity.getDimensions(newEntity.getPose()).makeBoundingBox(0, 0, 0);

		Vec3 bestCandidate = null;
		double bestScore = Double.NEGATIVE_INFINITY;

		Vec3 viewerEye = viewer != null ? viewer.getEyePosition() : null;
		Vec3 anchorCenter = source.getBoundingBox().getCenter();
		Vec3 viewRayDir = (viewerEye != null) ? anchorCenter.subtract(viewerEye).normalize() : null;

		// Candidate search
		for (double radius : RADII) {
			for (double[] dir : DIRECTIONS) {
				double cx = srcPos.x + dir[0] * radius;
				double cy = srcPos.y;
				double cz = srcPos.z + dir[1] * radius;
				Vec3 candidate = new Vec3(cx, cy, cz);
				AABB candidateAABB = baseAABB.move(cx, cy, cz);

				// 1. Block collision check at candidate position
				if (!level.noCollision(newEntity, candidateAABB)) {
					continue;
				}

				// 2. Line-of-sight sweep check: ensure path from source does not cross fences or solid walls
				if (hasImpassableCollisionOnPath(level, newEntity, srcPos, candidate)) {
					continue;
				}

				// 3. Ground support check
				BlockPos belowPos = BlockPos.containing(cx, cy - 0.2, cz);
				BlockState stateBelow = level.getBlockState(belowPos);
				if (stateBelow.isAir()) {
					continue;
				}

				// 4. Overlap scoring with reserved boxes (e.g. source parent, partner)
				boolean intersectsAnyReserved = false;
				double minDistanceToReserved = Double.MAX_VALUE;

				if (reservedBoxes != null) {
					for (AABB res : reservedBoxes) {
						if (candidateAABB.intersects(res)) {
							intersectsAnyReserved = true;
						}
						Vec3 resCenter = res.getCenter();
						double dist = candidate.distanceTo(resCenter);
						if (dist < minDistanceToReserved) {
							minDistanceToReserved = dist;
						}
					}
				}

				// 5. View-ray clearance check: does candidate intersect the player's view-ray to the anchor?
				boolean blocksViewRay = false;
				double viewClearanceScore = 0.0;
				if (viewerEye != null && viewRayDir != null) {
					// Check if segment from viewer's eyes to anchor center intersects candidate AABB
					if (candidateAABB.clip(viewerEye, anchorCenter).isPresent()) {
						blocksViewRay = true;
					} else {
						Vec3 toCand = candidate.subtract(viewerEye).normalize();
						viewClearanceScore = toCand.cross(viewRayDir).length() * 10.0;
					}
				}

				// Score this candidate:
				// Prioritize: (1) no view obstruction, (2) zero AABB overlap, (3) distance and angle
				double score = (blocksViewRay ? -500.0 : 0.0)
					+ (intersectsAnyReserved ? -100.0 : 100.0)
					+ minDistanceToReserved
					+ viewClearanceScore
					- (radius * 0.1);

				if (score > bestScore) {
					bestScore = score;
					bestCandidate = candidate;
					// If we found a completely non-overlapping and non-blocking valid position, accept it
					if (!intersectsAnyReserved && !blocksViewRay) {
						break;
					}
				}
			}
			if (bestCandidate != null && bestScore > 50.0) {
				break;
			}
		}

		// Fallback: return source position if no safe separate spot was found
		if (bestCandidate == null) {
			return srcPos;
		}

		return bestCandidate;
	}

	/**
	 * Checks if the line from srcPos to candidate crosses solid blocks, fences, walls, or closed gates.
	 */
	private static boolean hasImpassableCollisionOnPath(ServerLevel level, Entity entity, Vec3 from, Vec3 to) {
		// Raycast at animal foot level + 0.2 and mid-body level + 0.6
		Vec3 fromFeet = from.add(0, 0.2, 0);
		Vec3 toFeet = to.add(0, 0.2, 0);
		HitResult hitFeet = level.clip(new ClipContext(fromFeet, toFeet, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
		if (hitFeet.getType() != HitResult.Type.MISS) {
			return true;
		}

		Vec3 fromBody = from.add(0, 0.6, 0);
		Vec3 toBody = to.add(0, 0.6, 0);
		HitResult hitBody = level.clip(new ClipContext(fromBody, toBody, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
		return hitBody.getType() != HitResult.Type.MISS;
	}
}

