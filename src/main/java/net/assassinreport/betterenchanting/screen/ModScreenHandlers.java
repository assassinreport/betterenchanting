package net.assassinreport.betterenchanting.screen;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public class ModScreenHandlers {

    public static MenuType<NewEnchantingScreenHandler> NEW_ENCHANTING_SCREEN_HANDLER;
    public static MenuType<NewChiseledBookshelfScreenHandler> NEW_CHISELED_BOOKSHELF_SCREEN_HANDLER;

    public static void registerScreenHandlers() {
        NEW_ENCHANTING_SCREEN_HANDLER = Registry.register(
                BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(BetterEnchanting.MOD_ID, "new_enchanting_screen"),
                new ExtendedMenuType<>(NewEnchantingScreenHandler::new, BlockPos.STREAM_CODEC)
        );
        NEW_CHISELED_BOOKSHELF_SCREEN_HANDLER = Registry.register(
                BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(BetterEnchanting.MOD_ID, "new_chiseled_bookshelf_screen"),
                new ExtendedMenuType<>(NewChiseledBookshelfScreenHandler::new, BlockPos.STREAM_CODEC)
        );

        BetterEnchanting.LOGGER.info("Registering Screen Handlers for " + BetterEnchanting.MOD_ID);
    }
}