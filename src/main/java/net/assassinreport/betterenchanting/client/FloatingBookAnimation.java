package net.assassinreport.betterenchanting.client;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FloatingBookAnimation {

    private static final RandomSource RANDOM = RandomSource.create();
    private static final float PAGE_SPEED = 0.4f;
    private static final float FLIP_LIMIT = 0.2f;
    private static final float ROTATION_SPEED = 0.4f;
    private static final float PLAYER_DETECTION_RADIUS = 3f;

    public int ticks;
    public float pageAngle, nextPageAngle;
    public float flipRandom, flipTurn;
    public float pageTurningSpeed, nextPageTurningSpeed;
    public float bookRotation, lastBookRotation, targetBookRotation;

    public static float normalizeAngle(float angle) {
        angle %= 2 * Mth.PI;
        if (angle >= Mth.PI) angle -= 2 * Mth.PI;
        if (angle < -Mth.PI) angle += 2 * Mth.PI;
        return angle;
    }

    public void tick(Level world, BlockPos pos) {
        lastBookRotation = bookRotation;
        pageTurningSpeed = nextPageTurningSpeed;

        Player player = world.getNearestPlayer(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                PLAYER_DETECTION_RADIUS, false
        );

        if (player != null) {
            updateTowardsPlayer(pos, player);
        } else {
            idleAnimation();
        }

        animateBookRotation();
        animatePageTurning();
        ticks++;
    }

    private void updateTowardsPlayer(BlockPos pos, Player player) {
        double dx = player.getX() - (pos.getX() + 0.5);
        double dz = player.getZ() - (pos.getZ() + 0.5);
        targetBookRotation = (float) Mth.atan2(dz, dx);

        nextPageTurningSpeed = Mth.clamp(nextPageTurningSpeed + 0.1f, 0, 1);

        float prevFlip = flipRandom;
        while (prevFlip == flipRandom) {
            flipRandom += RANDOM.nextInt(4) - RANDOM.nextInt(4);
        }
    }

    private void idleAnimation() {
        targetBookRotation += 0.02f;
        nextPageTurningSpeed = Mth.clamp(nextPageTurningSpeed - 0.1f, 0, 1);
    }

    private void animateBookRotation() {
        bookRotation = normalizeAngle(bookRotation);
        targetBookRotation = normalizeAngle(targetBookRotation);
        bookRotation += normalizeAngle(targetBookRotation - bookRotation) * ROTATION_SPEED;
    }

    private void animatePageTurning() {
        pageAngle = nextPageAngle;
        float delta = Mth.clamp((flipRandom - nextPageAngle) * PAGE_SPEED, -FLIP_LIMIT, FLIP_LIMIT);
        flipTurn += (delta - flipTurn) * 0.9f;
        nextPageAngle += flipTurn;
    }
}