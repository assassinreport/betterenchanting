package net.assassinreport.betterenchanting.screen;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ModScreenHandlers {

    public static ScreenHandlerType<NewEnchantingScreenHandler> NEW_ENCHANTING_SCREEN_HANDLER;
    public static ScreenHandlerType<NewChiseledBookshelfScreenHandler> NEW_CHISELED_BOOKSHELF_SCREEN_HANDLER;

    public static void registerScreenHandlers() {
        NEW_ENCHANTING_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(BetterEnchanting.MOD_ID, "new_enchanting_screen"),
                new ExtendedScreenHandlerType<>(NewEnchantingScreenHandler::new, BlockPos.PACKET_CODEC.cast())
        );

        NEW_CHISELED_BOOKSHELF_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(BetterEnchanting.MOD_ID, "new_chiseled_bookshelf_screen"),
                new ExtendedScreenHandlerType<>(NewChiseledBookshelfScreenHandler::new, BlockPos.PACKET_CODEC.cast())
        );

        BetterEnchanting.LOGGER.info("Registering Screen Handlers for " + BetterEnchanting.MOD_ID);
    }
}