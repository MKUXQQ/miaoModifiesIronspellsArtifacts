package com.example.portableinscriptiontable.pool;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class RandomSpellBookFactory {
    public static final String FINAL_BOOK_NAME = "《终焉之书》";

    private RandomSpellBookFactory() {
    }

    public static ItemStack create(ServerLevel level, int slots, List<SpellPoolEntry> pool) {
        int clampedSlots = Math.max(1, slots);
        ItemStack stack = new ItemStack(ItemRegistry.WIMPY_SPELL_BOOK.get());
        ISpellContainerMutable container = ISpellContainer.create(clampedSlots, true, true).mutableCopy();
        List<SpellPoolEntry> shuffled = new ArrayList<>(pool);
        shuffle(level, shuffled);
        int added = 0;
        for (SpellPoolEntry entry : shuffled) {
            if (added >= clampedSlots) {
                break;
            }
            AbstractSpell spell = SpellRegistry.getSpell(entry.spellId());
            if (spell == SpellRegistry.none()) {
                continue;
            }
            if (container.addSpell(spell, entry.level(), false)) {
                added++;
            }
        }
        ISpellContainer.set(stack, container.toImmutable());
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(FINAL_BOOK_NAME));
        return stack;
    }

    private static void shuffle(ServerLevel level, List<SpellPoolEntry> ids) {
        for (int i = ids.size() - 1; i > 0; i--) {
            int j = level.random.nextInt(i + 1);
            SpellPoolEntry value = ids.get(i);
            ids.set(i, ids.get(j));
            ids.set(j, value);
        }
    }
}
