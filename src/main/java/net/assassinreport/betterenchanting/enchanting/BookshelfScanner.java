package net.assassinreport.betterenchanting.enchanting;

import net.assassinreport.betterenchanting.block.entity.NewChiseledBookshelfBlockEntity;
import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

public class BookshelfScanner {

    private BookshelfScanner() {}

    private static void addBookshelfCounts(
            Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> counts,
            NewChiseledBookshelfBlockEntity shelf) {

        for (int i = 0; i < shelf.getContainerSize(); i++) {
            ItemStack stack = shelf.getItem(i);
            if (!stack.is(Items.ENCHANTED_BOOK)) continue;

            ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);

            enchantments.keySet().forEach(ench ->
                    counts.merge(
                            new NewEnchantingTableBlockEntity.EnchantmentLevel(ench, enchantments.getLevel(ench)),
                            1,
                            Integer::sum
                    )
            );
        }
    }

    public static Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> scan(
            Level world,
            BlockPos center,
            int radius
    ) {
        Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> counts = new HashMap<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    pos.set(cx + x, cy + y, cz + z);
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof NewChiseledBookshelfBlockEntity shelf) {
                        addBookshelfCounts(counts, shelf);
                    }
                }
            }
        }

        return counts;
    }
}