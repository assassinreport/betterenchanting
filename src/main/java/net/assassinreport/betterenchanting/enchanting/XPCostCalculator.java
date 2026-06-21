package net.assassinreport.betterenchanting.enchanting;

import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;

import java.util.Map;

public class XPCostCalculator {

    private static final int BOOKS = 6;

    public static int computeCost(NewEnchantingTableBlockEntity.SelectedEnchantment selected,
                                  Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> bonusCounts) {

        int baseCostPerLevel = Math.max(1, 30 / selected.enchantment().value().getMaxLevel());
        int cost = baseCostPerLevel * selected.level();

        // Makes cost 0 if enchantment is fully unlocked
        NewEnchantingTableBlockEntity.EnchantmentLevel enchantmentLevel =
                new NewEnchantingTableBlockEntity.EnchantmentLevel(selected.enchantment(), selected.level());

        if (bonusCounts.getOrDefault(enchantmentLevel, 0) >= BOOKS) {
            return 0;
        }

        return cost;
    }
}