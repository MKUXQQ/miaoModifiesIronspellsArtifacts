package com.example.portableinscriptiontable.item;

import com.example.portableinscriptiontable.network.ModNetwork;
import com.example.portableinscriptiontable.network.SyncSpellPoolPayload;
import com.example.portableinscriptiontable.pool.SpellPoolPage;
import com.example.portableinscriptiontable.pool.SpellPoolStore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
                ModNetwork.sendToPlayer(serverPlayer, new SyncSpellPoolPayload(page, SpellPoolStore.snapshot(page), true));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.portable_inscription_table.random_spell_pool_editor.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.portable_inscription_table.random_spell_pool_editor.page", page(stack)).withStyle(ChatFormatting.DARK_AQUA));
    }

    private static int page(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return SpellPoolPage.clamp(tag == null ? 1 : tag.getInt(PAGE_TAG));
    }

    private static void setPage(ItemStack stack, int page) {
        stack.getOrCreateTag().putInt(PAGE_TAG, SpellPoolPage.clamp(page));
    }
}
