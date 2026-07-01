package net.assassinreport.betterenchanting.block.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@FunctionalInterface
public interface ImplementedInventory extends WorldlyContainer {

    NonNullList<ItemStack> getItems();

    static ImplementedInventory of(NonNullList<ItemStack> items) {
        return () -> items;
    }

    static ImplementedInventory ofSize(int size) {
        return of(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    // WorldlyContainer

    @NullMarked
    @Override
    default int[] getSlotsForFace(Direction side) {
        int[] result = new int[getItems().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }

    @NullMarked
    @Override
    default boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return true;
    }

    @NullMarked
    @Override
    default boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    // Container

    @Override
    default int getContainerSize() {
        return getItems().size();
    }

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @NullMarked
    @Override
    default ItemStack getItem(int slot) {
        return getItems().get(slot);
    }

    @NullMarked
    @Override
    default ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(getItems(), slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @NullMarked
    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(getItems(), slot);
    }

    @NullMarked
    @Override
    default void setItem(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @NullMarked
    @Override
    default void clearContent() {
        getItems().clear();
    }

    default void setChanged() {
        if (this instanceof net.minecraft.world.level.block.entity.BlockEntity be) {
            be.setChanged();
        }
    }

    @NullMarked
    @Override
    default boolean stillValid(Player player) {
        return true;
    }
}