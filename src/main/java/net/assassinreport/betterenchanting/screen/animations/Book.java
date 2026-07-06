package net.assassinreport.betterenchanting.screen.animations;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
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

        float j = MathHelper.clamp(MathHelper.fractionalPart(g + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float k = MathHelper.clamp(MathHelper.fractionalPart(g + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        this.BOOK_MODEL.setAngles(new BookModel.BookModelState(0.0F, j, k, f));

        context.addBookModel(BOOK_MODEL, BOOK_TEXTURE, 40.0F, f, g, x - 26, y + 53, x + 78, y + 133);
    }
}