package net.assassinreport.betterenchanting.screen.animations;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class Book {

    private final BookModel BOOK_MODEL;
    private final RandomSource random = RandomSource.create();

    private int ticks;
    private float nextPageAngle;
    private float pageAngle;
    private float approximatePageAngle;
    private float pageRotationSpeed;
    private float nextPageTurningSpeed;
    private float pageTurningSpeed;
    private int flutterCooldown = 0;

    private ItemStack currentStack = ItemStack.EMPTY;

    private static final Identifier BOOK_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/enchantment/enchanting_table_book.png");

    public Book(BookModel model) {
        this.BOOK_MODEL = model;
    }

    public void tick(ItemStack newStack, boolean shouldAnimate) {
        if (!ItemStack.matches(newStack, currentStack)) {
            currentStack = newStack;

            do {
                approximatePageAngle += (random.nextInt(4) - random.nextInt(4));
            } while (nextPageAngle <= approximatePageAngle + 1.0F
                    && nextPageAngle >= approximatePageAngle - 1.0F);
        }

        ticks++;

        float target = shouldAnimate ? 1.0F : 0.0F;
        nextPageTurningSpeed += (target - nextPageTurningSpeed) * 0.2F;
        nextPageTurningSpeed = Mth.clamp(nextPageTurningSpeed, 0.0F, 1.0F);
        pageTurningSpeed = nextPageTurningSpeed;

        if (shouldAnimate) {
            if (flutterCooldown > 0) {
                flutterCooldown--;
            } else if (random.nextFloat() < 0.1f) {
                approximatePageAngle += (random.nextBoolean() ? 1 : -1) * (0.5F + random.nextFloat() * 1.5F);
                flutterCooldown = 20 + random.nextInt(20);
            }
        }

        float f = (approximatePageAngle - nextPageAngle) * 0.4F;
        f = Mth.clamp(f, -0.2F, 0.2F);
        pageRotationSpeed += (f - pageRotationSpeed) * 0.9F;
        nextPageAngle += pageRotationSpeed;

        pageAngle += (nextPageAngle - pageAngle) * 0.4F;
    }

    public void render(GuiGraphicsExtractor guiGraphics, int x, int y, float delta) {
        float open = Mth.lerp(delta, this.pageTurningSpeed, this.nextPageTurningSpeed);
        float flip = Mth.lerp(delta, this.pageAngle, this.nextPageAngle);

        guiGraphics.book(BOOK_MODEL, BOOK_TEXTURE, 40.0F, open, flip, x - 26, y + 53, x + 78, y + 133);
    }
}