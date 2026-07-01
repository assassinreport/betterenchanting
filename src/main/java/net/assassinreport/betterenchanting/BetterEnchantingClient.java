package net.assassinreport.betterenchanting;

import net.assassinreport.betterenchanting.block.entity.ModBlockEntities;
import net.assassinreport.betterenchanting.block.entity.renderer.NewEnchantingTableBlockEntityRenderer;
import net.assassinreport.betterenchanting.screen.ModScreenHandlers;
import net.assassinreport.betterenchanting.screen.NewChiseledBookshelfScreen;
import net.assassinreport.betterenchanting.screen.NewEnchantingScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class BetterEnchantingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModScreenHandlers.NEW_ENCHANTING_SCREEN_HANDLER, NewEnchantingScreen::new);
        MenuScreens.register(ModScreenHandlers.NEW_CHISELED_BOOKSHELF_SCREEN_HANDLER, NewChiseledBookshelfScreen::new);

        BlockEntityRenderers.register(ModBlockEntities.NEW_ENCHANTING_TABLE_BLOCK_ENTITY, NewEnchantingTableBlockEntityRenderer::new);
    }
}