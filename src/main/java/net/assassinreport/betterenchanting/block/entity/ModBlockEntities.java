package net.assassinreport.betterenchanting.block.entity;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.assassinreport.betterenchanting.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static final BlockEntityType<NewEnchantingTableBlockEntity> NEW_ENCHANTING_TABLE_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(BetterEnchanting.MOD_ID, "new_enchanting_table"),
                    FabricBlockEntityTypeBuilder.create(NewEnchantingTableBlockEntity::new,
                            ModBlocks.NEW_ENCHANTING_BLOCK).build(null));

    public static final BlockEntityType<NewChiseledBookshelfBlockEntity> NEW_CHISELED_BOOKSHELF_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(BetterEnchanting.MOD_ID, "new_chiseled_bookshelf"),
                    FabricBlockEntityTypeBuilder.create(NewChiseledBookshelfBlockEntity::new,
                            ModBlocks.NEW_CHISELED_BOOKSHELF_BLOCK).build(null));

    public static void registerBlockEntities() {
        BetterEnchanting.LOGGER.info("Registering Block Entities for " + BetterEnchanting.MOD_ID);
    }
}