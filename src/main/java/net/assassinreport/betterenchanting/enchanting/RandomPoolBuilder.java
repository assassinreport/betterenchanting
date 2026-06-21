package net.assassinreport.betterenchanting.enchanting;

import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomPoolBuilder {

    private static int calculateWeight(int count) {
        return 100 + 165 * Math.min(count, 5);
    }

    public static class WeightedEntry {
        public final NewEnchantingTableBlockEntity.SelectedEnchantment enchant;
        public final int weight;

        public WeightedEntry(NewEnchantingTableBlockEntity.SelectedEnchantment enchant, int weight) {
            this.enchant = enchant;
            this.weight = weight;
        }
    }

    public static List<WeightedEntry> buildWeightedPool(
            List<Enchantment> candidates,
            Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> bonusCounts,
            ItemStack stack) {

        List<WeightedEntry> pool = new ArrayList<>();
        Map<Enchantment, Integer> currentEnchantments = EnchantmentHelper.get(stack);

        Map<Enchantment, Integer> blocked = new HashMap<>();
        Map<Enchantment, Integer> unlockedNextLevel = new HashMap<>();

        // Determine blocked and unlockable enchantments
        for (var entry : bonusCounts.entrySet()) {
            Enchantment ench = entry.getKey().enchantment();
            int level = entry.getKey().level();
            int count = entry.getValue();

            if (count >= 6) {
                blocked.merge(ench, level, Math::max);
                if (level < ench.getMaxLevel()) {
                    unlockedNextLevel.merge(ench, level + 1, Math::max);
                }
            }
        }

        for (Enchantment ench : candidates) {
            if (currentEnchantments.containsKey(ench)) continue;

            Integer nextLevel = unlockedNextLevel.get(ench);
            if (nextLevel != null) {
                int count = bonusCounts.getOrDefault(
                        new NewEnchantingTableBlockEntity.EnchantmentLevel(ench, nextLevel), 0
                );
                pool.add(new WeightedEntry(
                        new NewEnchantingTableBlockEntity.SelectedEnchantment(ench, nextLevel),
                        calculateWeight(count)
                ));
            }

            for (int lvl = 1; lvl <= ench.getMaxLevel(); lvl++) {
                if (blocked.getOrDefault(ench, 0) >= lvl) continue;

                int count = bonusCounts.getOrDefault(
                        new NewEnchantingTableBlockEntity.EnchantmentLevel(ench, lvl), 0
                );
                if (count > 0 || lvl == 1) {
                    pool.add(new WeightedEntry(
                            new NewEnchantingTableBlockEntity.SelectedEnchantment(ench, lvl),
                            calculateWeight(count)
                    ));
                }
            }
        }

        return pool;
    }

    public static NewEnchantingTableBlockEntity.SelectedEnchantment pickWeightedRandom(
            List<WeightedEntry> pool, Random random) {

        int totalWeight = pool.stream().mapToInt(e -> e.weight).sum();
        int r = random.nextInt(totalWeight);

        for (WeightedEntry entry : pool) {
            r -= entry.weight;
            if (r < 0) return entry.enchant;
        }

        // Fallback if something goes wrong
        return pool.get(pool.size() - 1).enchant;
    }
}