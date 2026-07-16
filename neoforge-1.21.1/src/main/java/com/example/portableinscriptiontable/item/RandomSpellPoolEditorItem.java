package com.example.portableinscriptiontable.item;

import com.example.portableinscriptiontable.pool.SpellPoolPage;
import com.example.portableinscriptiontable.pool.SpellPoolStore;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class RandomSpellPoolEditorItem extends Item {
    private static final String PAGE_TAG = "SpellPoolPage";

    public RandomSpellPoolEditorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            int page = page(stack);
            if (player.isShiftKeyDown()) {
                page = SpellPoolPage.next(page);
                setPage(stack, page);
                player.displayClientMessage(Component.translatable("item.portable_inscription_table.random_spell_pool_editor.page", page), true);
            } else {
                int openPage = page;
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inventory, ignored) -> ChestMenu.sixRows(id, inventory, SpellPoolStore.createContainer(openPage)),
                        Component.translatable("screen.portable_inscription_table.spell_pool_page", openPage)
                ));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.portable_inscription_table.random_spell_pool_editor.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.portable_inscription_table.random_spell_pool_editor.page", page(stack)).withStyle(ChatFormatting.DARK_AQUA));
    }

    private static int page(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return SpellPoolPage.clamp(data.copyTag().getInt(PAGE_TAG));
    }

    private static void setPage(ItemStack stack, int page) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        tag.putInt(PAGE_TAG, SpellPoolPage.clamp(page));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
