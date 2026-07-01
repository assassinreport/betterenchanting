package net.assassinreport.betterenchanting.block.entity;

import net.assassinreport.betterenchanting.client.FloatingBookAnimation;
import net.assassinreport.betterenchanting.enchanting.BookshelfScanner;
import net.assassinreport.betterenchanting.enchanting.RandomPoolBuilder;
import net.assassinreport.betterenchanting.enchanting.XPCostCalculator;
import net.assassinreport.betterenchanting.screen.NewEnchantingScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewEnchantingTableBlockEntity extends BlockEntity implements ExtendedMenuProvider<BlockPos>, ImplementedInventory {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);

    private final Map<EnchantmentLevel, Integer> cachedEnchantments = new HashMap<>();

    public final FloatingBookAnimation bookAnimation = new FloatingBookAnimation();

    // ----------------------------------
    //          Menu Constructors
    // ----------------------------------

    public NewEnchantingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEW_ENCHANTING_TABLE_BLOCK_ENTITY, pos, state);
    }

    @NullMarked
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new NewEnchantingScreenHandler(syncId, playerInventory, this);
    }

    @NullMarked
    @Override
    public Component getDisplayName() {
        return Component.translatable("item.betterenchanting.new_enchanting_table");
    }

    @NullMarked
    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @NullMarked
    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    // -------------------------------------------
    //          Enchantment Constructors
    // -------------------------------------------

    private Map<EnchantmentLevel, Integer> scanBonusBooks() {
        return level == null ? Collections.emptyMap() : BookshelfScanner.scan(level, worldPosition, 3);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, NewEnchantingTableBlockEntity table) {
        table.bookAnimation.tick(world, pos);

        if (world instanceof ServerLevel && table.bookAnimation.ticks % 20 == 0) {
            table.refreshCachedEnchantmentsAndSync();
        }
    }

    public Map<EnchantmentLevel, Integer> getCachedEnchantments() {
        return cachedEnchantments;
    }

    public void refreshCachedEnchantmentsAndSync() {
        if (level instanceof ServerLevel) {
            cachedEnchantments.clear();
            cachedEnchantments.putAll(scanBonusBooks());
            sync();
        }
    }

    // --------------------------------------
    //          Enchanting Logic
    // --------------------------------------

    public List<Holder<Enchantment>> getValidEnchantments(ItemStack item) {
        if (level == null) return List.of();
        var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<Holder<Enchantment>> all = registry.listElements().map(e -> (Holder<Enchantment>) e).toList();
        if (item.is(Items.BOOK)) return all;
        return all.stream()
                .filter(e -> e.value().isSupportedItem(item))
                .toList();
    }

    public List<RandomPoolBuilder.WeightedEntry> getRandomPoolForItem(ItemStack stack) {
        var valid = getValidEnchantments(stack);
        var bonus = getCachedEnchantments();
        return RandomPoolBuilder.buildWeightedPool(valid, bonus, stack);
    }

    public boolean canAfford(Player player,
                              RandomPoolBuilder.WeightedEntry entry,
                              Map<EnchantmentLevel, Integer> bonus) {
        return player.experienceLevel >= XPCostCalculator.computeCost(entry.enchant, bonus);
    }

    public boolean canAffordAny(Player player, ItemStack stack) {
        if (stack.isEmpty()) return false;
        var pool = getRandomPoolForItem(stack); // uses RandomPoolBuilder internally
        return pool.stream().anyMatch(e -> canAfford(player, e, getCachedEnchantments()));
    }

    public void tryEnchantItem(Player player) {
        ItemStack item = inventory.getFirst();
        if (item.isEmpty()) return;

        Map<EnchantmentLevel, Integer> bonusWeights = cachedEnchantments;
        var validEnchantments = getValidEnchantments(item);
        var weightedPool = RandomPoolBuilder.buildWeightedPool(validEnchantments, bonusWeights, item);
        var affordable = weightedPool.stream().filter(e -> canAfford(player, e, bonusWeights)).toList();

        if (affordable.isEmpty()) return;

        SelectedEnchantment selected = RandomPoolBuilder.pickWeightedRandom(affordable, player.getRandom());
        ItemStack base = item.copy();
        ItemStack result = applyEnchant(base, selected);
        inventory.set(0, result);
        player.giveExperienceLevels(-XPCostCalculator.computeCost(selected, bonusWeights));
    }

    private ItemStack applyEnchant(ItemStack stack, SelectedEnchantment selected) {
        if (stack.is(Items.BOOK)) {
            ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.updateEnchantments(result, builder -> builder.set(selected.enchantment(), selected.level()));
            return result;
        }
        stack.enchant(selected.enchantment(), selected.level());
        return stack;
    }

    // ---------------------------
    //        Server Stuff
    // ---------------------------

    public void sync() {
        if (level instanceof ServerLevel) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NullMarked
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);

        ValueOutput.ValueOutputList listValue = output.childrenList("cachedEnchantments");
        for (var entry : cachedEnchantments.entrySet()) {
            EnchantmentLevel key = entry.getKey();
            Identifier id = key.enchantment().unwrapKey()
                    .orElseThrow(() -> new IllegalStateException("Unregistered enchantment"))
                    .identifier();
            ValueOutput child = listValue.addChild();
            child.putString("id", id.toString());
            child.putInt("lvl", key.level());
            child.putInt("count", entry.getValue());
        }
    }

    @NullMarked
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);

        cachedEnchantments.clear();
        var enchantmentRegistry = input.lookup().lookupOrThrow(Registries.ENCHANTMENT);
        for (ValueInput child : input.childrenListOrEmpty("cachedEnchantments")) {
            Identifier id = Identifier.parse(child.getStringOr("id", ""));
            Holder<Enchantment> ench = enchantmentRegistry.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, id));
            int lvl = child.getIntOr("lvl", 0);
            int count = child.getIntOr("count", 0);
            cachedEnchantments.put(new EnchantmentLevel(ench, lvl), count);
        }
    }

    @NullMarked
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    // -------- Records ------------
    public record EnchantmentLevel(Holder<Enchantment> enchantment, int level) {}
    public record SelectedEnchantment(Holder<Enchantment> enchantment, int level) {}
}