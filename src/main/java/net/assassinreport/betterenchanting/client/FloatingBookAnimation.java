package net.assassinreport.betterenchanting.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class FloatingBookAnimation {

    private static final Random RANDOM = Random.create();
    private static final float PAGE_SPEED = 0.4f;
    private static final float FLIP_LIMIT = 0.2f;
    private static final float ROTATION_SPEED = 0.4f;
    private static final float PLAYER_DETECTION_RADIUS = 3f;

    public int ticks;
    public float pageAngle, nextPageAngle;
    public float flipRandom, flipTurn;
    public float pageTurningSpeed, nextPageTurningSpeed;
    public float bookRotation, lastBookRotation, targetBookRotation;

    private static final float PI = (float) Math.PI;
    private static final float TWO_PI = (float) (2 * Math.PI);

    public static float normalizeAngle(float angle) {
        angle %= TWO_PI;
        if (angle >= PI) angle -= TWO_PI;
        if (angle < -PI) angle += TWO_PI;
        return angle;
    }

    public void tick(World world, BlockPos pos) {
        lastBookRotation = bookRotation;
        pageTurningSpeed = nextPageTurningSpeed;

        PlayerEntity player = world.getClosestPlayer(
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

    private void updateTowardsPlayer(BlockPos pos, PlayerEntity player) {
        double dx = player.getX() - (pos.getX() + 0.5);
        double dz = player.getZ() - (pos.getZ() + 0.5);
        targetBookRotation = (float) MathHelper.atan2(dz, dx);

        nextPageTurningSpeed = MathHelper.clamp(nextPageTurningSpeed + 0.1f, 0, 1);

        float prevFlip = flipRandom;
        while (prevFlip == flipRandom) {
            flipRandom += RANDOM.nextInt(4) - RANDOM.nextInt(4);
        }
    }

    private void idleAnimation() {
        targetBookRotation += 0.02f;
        nextPageTurningSpeed = MathHelper.clamp(nextPageTurningSpeed - 0.1f, 0, 1);
    }

    private void animateBookRotation() {
        bookRotation = normalizeAngle(bookRotation);
        targetBookRotation = normalizeAngle(targetBookRotation);
        bookRotation += normalizeAngle(targetBookRotation - bookRotation) * ROTATION_SPEED;
    }

    private void animatePageTurning() {
        pageAngle = nextPageAngle;
        float delta = MathHelper.clamp((flipRandom - nextPageAngle) * PAGE_SPEED, -FLIP_LIMIT, FLIP_LIMIT);
        flipTurn += (delta - flipTurn) * 0.9f;
        nextPageAngle += flipTurn;
    }
}