package net.assassinreport.betterenchanting.screen.animations;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class Book {

    private final BookModel BOOK_MODEL;
    private final Identifier texture;
    private final Random random = Random.create();

    private int ticks;
    private float nextPageAngle;
    private float pageAngle;
    private float approximatePageAngle;
    private float pageRotationSpeed;
    private float nextPageTurningSpeed;
    private float pageTurningSpeed;
    private int flutterCooldown = 0;

    private ItemStack currentStack = ItemStack.EMPTY;

    private static final Identifier BOOK_TEXTURE = Identifier.of("textures/entity/enchanting_table_book.png");

    public Book(BookModel model, Identifier texture) {
        this.BOOK_MODEL = model;
        this.texture = texture;
    }

    public void tick(ItemStack newStack, boolean shouldAnimate) {
        if (!ItemStack.areEqual(newStack, currentStack)) {
            currentStack = newStack;

            do {
                approximatePageAngle += (random.nextInt(4) - random.nextInt(4));
            } while (nextPageAngle <= approximatePageAngle + 1.0F
                    && nextPageAngle >= approximatePageAngle - 1.0F);
        }

        ticks++;

        // Opening/closing
        float target = shouldAnimate ? 1.0F : 0.0F;
        nextPageTurningSpeed += (target - nextPageTurningSpeed) * 0.2F;
        nextPageTurningSpeed = MathHelper.clamp(nextPageTurningSpeed, 0.0F, 1.0F);
        pageTurningSpeed = nextPageTurningSpeed;

        // Random flutter
        if (shouldAnimate) {
            if (flutterCooldown > 0) {
                flutterCooldown--;
            } else if (random.nextFloat() < 0.1f) {
                approximatePageAngle += (random.nextBoolean() ? 1 : -1) * (0.5F + random.nextFloat() * 1.5F);
                flutterCooldown = 20 + random.nextInt(20);
            }
        }

        // Page flipping
        float f = (approximatePageAngle - nextPageAngle) * 0.4F;
        f = MathHelper.clamp(f, -0.2F, 0.2F);
        pageRotationSpeed += (f - pageRotationSpeed) * 0.9F;
        nextPageAngle += pageRotationSpeed;

        // Smooth current page angle
        pageAngle += (nextPageAngle - pageAngle) * 0.4F;
    }

    public void render(DrawContext context, int x, int y, float delta) {
        float f = MathHelper.lerp(delta, this.pageTurningSpeed, this.nextPageTurningSpeed);
        float g = MathHelper.lerp(delta, this.pageAngle, this.nextPageAngle);
        DiffuseLighting.method_34742();
        context.getMatrices().push();
        context.getMatrices().translate(x + 26F, y + 55.0F, 100.0F);
        float h = 40.0F;
        context.getMatrices().scale(-40.0F, 40.0F, 40.0F);
        context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(25.0F));
        context.getMatrices().translate((1.0F - f) * 0.2F, (1.0F - f) * 0.1F, (1.0F - f) * 0.25F);
        float i = -(1.0F - f) * 90.0F - 90.0F;
        context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i));
        context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
        float j = MathHelper.clamp(MathHelper.fractionalPart(g + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float k = MathHelper.clamp(MathHelper.fractionalPart(g + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        this.BOOK_MODEL.setPageAngles(0.0F, j, k, f);
        VertexConsumer vertexConsumer = context.getVertexConsumers().getBuffer(this.BOOK_MODEL.getLayer(BOOK_TEXTURE));
        this.BOOK_MODEL.render(context.getMatrices(), vertexConsumer, 15728880, OverlayTexture.DEFAULT_UV, 0xFFFFFFFF);
        context.draw();
        context.getMatrices().pop();
        DiffuseLighting.enableGuiDepthLighting();
    }
}