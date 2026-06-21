package net.assassinreport.betterenchanting.enchanting;

import net.assassinreport.betterenchanting.block.entity.NewChiseledBookshelfBlockEntity;
import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class BookshelfScanner {

    private BookshelfScanner() {}

    private static void addBookshelfCounts(
            Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> counts,
            NewChiseledBookshelfBlockEntity shelf) {

        for (int i = 0; i < shelf.size(); i++) {
            ItemStack stack = shelf.getStack(i);
            if (!stack.isOf(Items.ENCHANTED_BOOK)) continue;

            EnchantmentHelper.getEnchantments(stack).getEnchantmentsMap().forEach(entry ->
                    counts.merge(
                            new NewEnchantingTableBlockEntity.EnchantmentLevel(entry.getKey().value(), entry.getIntValue()),
                            1,
                            Integer::sum
                    )
            );
        }
    }

    public static Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> scan(
            World world,
            BlockPos center,
            int radius
    ) {
        Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> counts = new HashMap<>();
        BlockPos.Mutable pos = new BlockPos.Mutable();

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