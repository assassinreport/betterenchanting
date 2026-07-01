package net.assassinreport.betterenchanting.item;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.assassinreport.betterenchanting.block.ModBlocks;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;

public class ModItemGroups {
    public static void registerItemsToVanillaGroups() {

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> entries.accept(ModBlocks.NEW_ENCHANTING_BLOCK.asItem()));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> entries.accept(ModBlocks.NEW_CHISELED_BOOKSHELF_BLOCK.asItem()));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS)
                .register(entries -> entries.accept(ModBlocks.NEW_CHISELED_BOOKSHELF_BLOCK.asItem()));
    }

    public static void registerItemGroups() {
        BetterEnchanting.LOGGER.info("Registering Item Groups for " + BetterEnchanting.MOD_ID);
    }
}