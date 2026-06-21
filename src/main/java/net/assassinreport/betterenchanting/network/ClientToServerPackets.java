package net.assassinreport.betterenchanting.network;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.assassinreport.betterenchanting.screen.NewEnchantingScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ClientToServerPackets {

    public record RandomEnchantPayload() implements CustomPayload {
        public static final CustomPayload.Id<RandomEnchantPayload> ID =
                new CustomPayload.Id<>(Identifier.of(BetterEnchanting.MOD_ID, "random_enchant"));
        public static final PacketCodec<PacketByteBuf, RandomEnchantPayload> CODEC =
                PacketCodec.unit(new RandomEnchantPayload());

        @Override
        public CustomPayload.Id<RandomEnchantPayload> getId() { return ID; }
    }

    public record SelectEnchantmentPayload(RegistryEntry<Enchantment> enchantment, int level) implements CustomPayload {
        public static final CustomPayload.Id<SelectEnchantmentPayload> ID =
                new CustomPayload.Id<>(Identifier.of(BetterEnchanting.MOD_ID, "select_enchantment"));
        public static final PacketCodec<RegistryByteBuf, SelectEnchantmentPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.registryEntry(RegistryKeys.ENCHANTMENT), SelectEnchantmentPayload::enchantment,
                        PacketCodecs.VAR_INT, SelectEnchantmentPayload::level,
                        SelectEnchantmentPayload::new
                );

        @Override
        public CustomPayload.Id<SelectEnchantmentPayload> getId() { return ID; }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(RandomEnchantPayload.ID, RandomEnchantPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SelectEnchantmentPayload.ID, SelectEnchantmentPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RandomEnchantPayload.ID,
                (payload, context) -> {
                    ServerPlayerEntity player = context.player();
                    player.server.execute(() -> {
                        if (player.currentScreenHandler instanceof NewEnchantingScreenHandler enchantingHandler) {
                            enchantingHandler.tryRandomEnchant();
                        }
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(SelectEnchantmentPayload.ID,
                (payload, context) -> {
                    ServerPlayerEntity player = context.player();
                    RegistryEntry<Enchantment> enchantment = payload.enchantment();
                    int level = payload.level();

                    player.server.execute(() -> {
                        if (player.currentScreenHandler instanceof NewEnchantingScreenHandler enchantingHandler) {
                            var blockEntity = enchantingHandler.blockEntity;
                            var key = new NewEnchantingTableBlockEntity.EnchantmentLevel(enchantment, level);

                            if (blockEntity.getCachedEnchantments().getOrDefault(key, 0) >= 6) {
                                enchantingHandler.trySelectableEnchant(player, enchantment, level);
                            }
                        }
                    });
                });
    }

    public static void sendRandomEnchantPacket() {
        ClientPlayNetworking.send(new RandomEnchantPayload());
    }

    public static void sendSelectEnchantmentPacket(RegistryEntry<Enchantment> enchantment, int level) {
        ClientPlayNetworking.send(new SelectEnchantmentPayload(enchantment, level));
    }
}