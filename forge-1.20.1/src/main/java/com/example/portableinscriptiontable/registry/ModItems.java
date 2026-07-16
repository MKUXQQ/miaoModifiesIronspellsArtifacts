package com.example.portableinscriptiontable.registry;

import com.example.portableinscriptiontable.PortableInscriptionTable;
import com.example.portableinscriptiontable.item.CatRuneItem;
import com.example.portableinscriptiontable.item.RandomSpellPoolEditorItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, PortableInscriptionTable.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PortableInscriptionTable.MOD_ID);

    public static final RegistryObject<Item> CAT_RUNE = ITEMS.register(
            "cat_rune",
            () -> new CatRuneItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> RANDOM_SPELL_POOL_EDITOR = ITEMS.register(
            "random_spell_pool_editor",
            () -> new RandomSpellPoolEditorItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<CreativeModeTab> CAT_RUNE_TAB = CREATIVE_TABS.register(
            ModRegistryIds.CAT_RUNE_TAB_ID.getPath(),
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.portable_inscription_table.cat_rune_tab"))
                    .icon(() -> new ItemStack(CAT_RUNE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(CAT_RUNE.get());
                        output.accept(RANDOM_SPELL_POOL_EDITOR.get());
                    })
                    .build()
    );

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        CREATIVE_TABS.register(eventBus);
    }
}
