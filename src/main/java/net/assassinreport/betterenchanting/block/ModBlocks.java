package net.assassinreport.betterenchanting.block;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.assassinreport.betterenchanting.block.custom.NewChiseledBookshelfBlock;
import net.assassinreport.betterenchanting.block.custom.NewEnchantingBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static final Block NEW_ENCHANTING_BLOCK = registerBlock("new_enchanting_table",
            new NewEnchantingBlock(BlockBehaviour.Properties.ofLegacyCopy(Blocks.STONE).noOcclusion().requiresCorrectToolForDrops().lightLevel(state -> 7)
                    .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(BetterEnchanting.MOD_ID, "new_enchanting_table")))));

    public static final Block NEW_CHISELED_BOOKSHELF_BLOCK = registerBlock("new_chiseled_bookshelf",
            new NewChiseledBookshelfBlock(BlockBehaviour.Properties.ofLegacyCopy(Blocks.CHISELED_BOOKSHELF).noOcclusion()
                    .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(BetterEnchanting.MOD_ID, "new_chiseled_bookshelf")))));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(BetterEnchanting.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(BetterEnchanting.MOD_ID, name),
                new BlockItem(block, new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(BetterEnchanting.MOD_ID, name)))));
    }

    public static void registerModBlocks() {
        BetterEnchanting.LOGGER.info("Registering ModBlocks for " + BetterEnchanting.MOD_ID);
    }
}