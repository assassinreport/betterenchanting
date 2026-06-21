package net.assassinreport.betterenchanting.block.entity;

import net.assassinreport.betterenchanting.client.FloatingBookAnimation;
import net.assassinreport.betterenchanting.enchanting.BookshelfScanner;
import net.assassinreport.betterenchanting.enchanting.RandomPoolBuilder;
import net.assassinreport.betterenchanting.enchanting.XPCostCalculator;
import net.assassinreport.betterenchanting.screen.NewEnchantingScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewEnchantingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    private final Map<EnchantmentLevel, Integer> cachedEnchantments = new HashMap<>();

    public final FloatingBookAnimation bookAnimation = new FloatingBookAnimation();

    // ----------------------------------
    //          Menu Constructors
    // ----------------------------------

    public NewEnchantingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEW_ENCHANTING_TABLE_BLOCK_ENTITY, pos, state);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new NewEnchantingScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("item.betterenchanting.new_enchanting_table");
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return this.pos;
    }

    // -------------------------------------------
    //          Enchantment Constructors
    // -------------------------------------------

    private Map<EnchantmentLevel, Integer> scanBonusBooks() {
        return world == null ? Collections.emptyMap() : BookshelfScanner.scan(world, pos, 3);
    }

    public static void tick(World world, BlockPos pos, BlockState state, NewEnchantingTableBlockEntity table) {
        table.bookAnimation.tick(world, pos);

        if (!world.isClient && table.bookAnimation.ticks % 20 == 0) {
            table.refreshCachedEnchantmentsAndSync();
        }
    }

    public Map<EnchantmentLevel, Integer> getCachedEnchantments() {
        return cachedEnchantments;
    }

    public void refreshCachedEnchantmentsAndSync() {
        if (world != null && !world.isClient) {
            cachedEnchantments.clear();
            cachedEnchantments.putAll(scanBonusBooks());
            sync();
        }
    }

    // --------------------------------------
    //          Enchanting Logic
    // --------------------------------------

    public List<RegistryEntry<Enchantment>> getValidEnchantments(ItemStack item) {
        if (world == null) return List.of();
        var registry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        List<RegistryEntry<Enchantment>> all = registry.streamEntries().map(e -> (RegistryEntry<Enchantment>) e).toList();
        if (item.isOf(Items.BOOK)) return all;
        return all.stream()
                .filter(e -> e.value().isAcceptableItem(item))
                .toList();
    }

    public List<RandomPoolBuilder.WeightedEntry> getRandomPoolForItem(ItemStack stack) {
        var valid = getValidEnchantments(stack);
        var bonus = getCachedEnchantments();
        return RandomPoolBuilder.buildWeightedPool(valid, bonus, stack);
    }

    public boolean canAfford(PlayerEntity player,
                              RandomPoolBuilder.WeightedEntry entry,
                              Map<EnchantmentLevel, Integer> bonus) {
        return player.experienceLevel >= XPCostCalculator.computeCost(entry.enchant, bonus);
    }

    public boolean canAffordAny(PlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) return false;
        var pool = getRandomPoolForItem(stack); // uses RandomPoolBuilder internally
        return pool.stream().anyMatch(e -> canAfford(player, e, getCachedEnchantments()));
    }

    public void tryEnchantItem(PlayerEntity player) {
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
        player.addExperienceLevels(-XPCostCalculator.computeCost(selected, bonusWeights));
    }

    private ItemStack applyEnchant(ItemStack stack, SelectedEnchantment selected) {
        if (stack.isOf(Items.BOOK)) {
            ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.apply(result, builder -> builder.add(selected.enchantment(), selected.level()));
            return result;
        }
        stack.addEnchantment(selected.enchantment(), selected.level());
        return stack;
    }

    // ---------------------------
    //        Server Stuff
    // ---------------------------

    public void sync() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    // ---------------------------
    //          NBT
    // ---------------------------
    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);

        NbtList list = new NbtList();
        for (var entry : cachedEnchantments.entrySet()) {
            NbtCompound tag = new NbtCompound();
            EnchantmentLevel key = entry.getKey();
            Identifier id = key.enchantment().getKey()
                    .orElseThrow(() -> new IllegalStateException("Unregistered enchantment"))
                    .getValue();
            tag.putString("id", id.toString());
            tag.putInt("lvl", key.level());
            tag.putInt("count", entry.getValue());
            list.add(tag);
        }
        nbt.put("cachedEnchantments", list);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);

        cachedEnchantments.clear();
        if (nbt.contains("cachedEnchantments")) {
            var enchantmentRegistry = registryLookup.getOrThrow(RegistryKeys.ENCHANTMENT);
            NbtList list = nbt.getList("cachedEnchantments").orElseThrow();
            for (int i = 0; i < list.size(); i++) {
                NbtCompound tag = list.getCompound(i).orElseThrow();
                Identifier id = Identifier.of(tag.getString("id").orElseThrow());
                RegistryEntry<Enchantment> ench = enchantmentRegistry
                        .getOptional(RegistryKey.of(RegistryKeys.ENCHANTMENT, id))
                        .orElseThrow();
                int lvl = tag.getInt("lvl").orElseThrow();
                int count = tag.getInt("count").orElseThrow();
                cachedEnchantments.put(new EnchantmentLevel(ench, lvl), count);
            }
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt, registryLookup);
        return nbt;
    }

    // -------- Records ------------
    public record EnchantmentLevel(RegistryEntry<Enchantment> enchantment, int level) {}
    public record SelectedEnchantment(RegistryEntry<Enchantment> enchantment, int level) {}
}