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
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
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

public class NewEnchantingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    private final Map<EnchantmentLevel, Integer> cachedEnchantments = new HashMap<>();

    public final FloatingBookAnimation bookAnimation = new FloatingBookAnimation();

    private static final List<Enchantment> ALL_ENCHANTMENTS = Registries.ENCHANTMENT.stream().toList();

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
        return Text.translatable("block.betterenchanting.new_enchanting_table");
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    // -------------------------------------------
    //          Enchantment Constructors
    // -------------------------------------------

    private static List<Enchantment> getAllEnchantments() {
        return ALL_ENCHANTMENTS;
    }

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

    public List<Enchantment> getValidEnchantments(ItemStack item) {
        if (item.isOf(Items.BOOK)) return getAllEnchantments();
        return getAllEnchantments().stream()
                .filter(e -> e.isAcceptableItem(item))
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
        ItemStack item = inventory.get(0);
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
            EnchantedBookItem.addEnchantment(
                    result,
                    new EnchantmentLevelEntry(selected.enchantment(), selected.level())
            );
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
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);

        NbtList list = new NbtList();
        for (var entry : cachedEnchantments.entrySet()) {
            NbtCompound tag = new NbtCompound();
            EnchantmentLevel key = entry.getKey();
            tag.putString("id", Registries.ENCHANTMENT.getId(key.enchantment()).toString());
            tag.putInt("lvl", key.level());
            tag.putInt("count", entry.getValue());
            list.add(tag);
        }
        nbt.put("cachedEnchantments", list);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);

        cachedEnchantments.clear();
        if (nbt.contains("cachedEnchantments", 9)) {
            NbtList list = nbt.getList("cachedEnchantments", 10);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound tag = list.getCompound(i);
                Enchantment ench = Registries.ENCHANTMENT.get(new Identifier(tag.getString("id")));
                int lvl = tag.getInt("lvl");
                int count = tag.getInt("count");
                cachedEnchantments.put(new EnchantmentLevel(ench, lvl), count);
            }
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    // -------- Records ------------
    public record EnchantmentLevel(Enchantment enchantment, int level) {}
    public record SelectedEnchantment(Enchantment enchantment, int level) {}
}