package net.assassinreport.betterenchanting.network;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.assassinreport.betterenchanting.screen.NewEnchantingScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.NullMarked;

public class ClientToServerPackets {

    public record RandomEnchantPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RandomEnchantPayload> ID =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(BetterEnchanting.MOD_ID, "random_enchant"));
        public static final StreamCodec<FriendlyByteBuf, RandomEnchantPayload> CODEC =
                StreamCodec.unit(new RandomEnchantPayload());

        @NullMarked
        @Override
        public CustomPacketPayload.Type<RandomEnchantPayload> type() { return ID; }
    }

    public record SelectEnchantmentPayload(Holder<Enchantment> enchantment, int level) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SelectEnchantmentPayload> ID =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(BetterEnchanting.MOD_ID, "select_enchantment"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectEnchantmentPayload> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT), SelectEnchantmentPayload::enchantment,
                        ByteBufCodecs.VAR_INT, SelectEnchantmentPayload::level,
                        SelectEnchantmentPayload::new
                );

        @NullMarked
        @Override
        public CustomPacketPayload.Type<SelectEnchantmentPayload> type() { return ID; }
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(RandomEnchantPayload.ID, RandomEnchantPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SelectEnchantmentPayload.ID, SelectEnchantmentPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RandomEnchantPayload.ID,
                (payload, context) -> {
                    ServerPlayer player = context.player();
                    if (player.containerMenu instanceof NewEnchantingScreenHandler enchantingHandler) {
                        enchantingHandler.tryRandomEnchant();
                    }
                });

        ServerPlayNetworking.registerGlobalReceiver(SelectEnchantmentPayload.ID,
                (payload, context) -> {
                    ServerPlayer player = context.player();
                    Holder<Enchantment> enchantment = payload.enchantment();
                    int level = payload.level();

                    if (player.containerMenu instanceof NewEnchantingScreenHandler enchantingHandler) {
                        var blockEntity = enchantingHandler.blockEntity;
                        var key = new NewEnchantingTableBlockEntity.EnchantmentLevel(enchantment, level);

                        if (blockEntity.getCachedEnchantments().getOrDefault(key, 0) >= 6) {
                            enchantingHandler.trySelectableEnchant(player, enchantment, level);
                        }
                    }
                });
    }

    public static void sendRandomEnchantPacket() {
        ClientPlayNetworking.send(new RandomEnchantPayload());
    }

    public static void sendSelectEnchantmentPacket(Holder<Enchantment> enchantment, int level) {
        ClientPlayNetworking.send(new SelectEnchantmentPayload(enchantment, level));
    }
}