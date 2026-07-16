package com.example.portableinscriptiontable.pool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SpellPoolStore {
    public static final int CONTAINER_SIZE = 54;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("portable_inscription_table_spell_pools.json");
    private static final Map<Integer, List<SpellPoolEntry>> PAGES = new LinkedHashMap<>();
    private static boolean loaded;

    private SpellPoolStore() {
    }

    public static List<SpellPoolRow> snapshot(int page) {
        ensureLoaded();
        Set<ResourceLocation> enabled = new LinkedHashSet<>();
        for (SpellPoolEntry entry : PAGES.computeIfAbsent(SpellPoolPage.clamp(page), ignored -> new ArrayList<>())) {
            enabled.add(entry.spellId());
        }
        List<SpellPoolRow> rows = new ArrayList<>();
        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            ResourceLocation id = spell.getSpellResource();
            rows.add(new SpellPoolRow(id, spell.getSpellName(), id.getNamespace(), spell.getCastType().name(), enabled.contains(id)));
        }
        rows.sort(Comparator.comparing(row -> row.spellId().toString()));
        return rows;
    }

    public static List<SpellPoolEntry> entriesAcrossAllPages() {
        ensureLoaded();
        List<SpellPoolEntry> entries = new ArrayList<>();
        for (int page = 1; page <= SpellPoolPage.PAGE_COUNT; page++) {
            entries.addAll(PAGES.computeIfAbsent(page, ignored -> new ArrayList<>()));
        }
        return entries;
    }

    public static SimpleContainer createContainer(int page) {
        int clampedPage = SpellPoolPage.clamp(page);
        SimpleContainer container = new SimpleContainer(CONTAINER_SIZE) {
            @Override
            public void stopOpen(Player player) {
                super.stopOpen(player);
                replacePageFromItems(clampedPage, this);
            }
        };
        int slot = 0;
        for (SpellPoolEntry entry : PAGES.computeIfAbsent(clampedPage, ignored -> new ArrayList<>())) {
            if (slot >= CONTAINER_SIZE) {
                break;
            }
            AbstractSpell spell = SpellRegistry.getSpell(entry.spellId());
            if (spell != SpellRegistry.none()) {
                container.setItem(slot++, createScroll(spell, entry.level()));
            }
        }
        return container;
    }

    public static void replacePageFromItems(int page, SimpleContainer container) {
        ensureLoaded();
        List<SpellPoolEntry> entries = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            entries.addAll(spellEntriesFromStack(container.getItem(slot)));
        }
        PAGES.put(SpellPoolPage.clamp(page), entries);
        save();
    }

    private static List<SpellPoolEntry> spellEntriesFromStack(ItemStack stack) {
        List<SpellPoolEntry> entries = new ArrayList<>();
        if (stack.isEmpty() || !ISpellContainer.isSpellContainer(stack)) {
            return entries;
        }
        for (SpellSlot slot : ISpellContainer.get(stack).getActiveSpells()) {
            AbstractSpell spell = slot.getSpell();
            if (spell != SpellRegistry.none()) {
                entries.add(new SpellPoolEntry(spell.getSpellResource(), clampLevel(spell, slot.getLevel())));
            }
        }
        return entries;
    }

    private static ItemStack createScroll(AbstractSpell spell, int level) {
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell, clampLevel(spell, level), stack);
        return stack;
    }

    private static int clampLevel(AbstractSpell spell, int level) {
        return Math.max(1, Math.min(level, Math.max(1, spell.getMaxLevel())));
    }

    private static void ensureLoaded() {
        if (!loaded) {
            load();
        }
    }

    private static void load() {
        loaded = true;
        PAGES.clear();
        for (int page = 1; page <= SpellPoolPage.PAGE_COUNT; page++) {
            PAGES.put(page, new ArrayList<>());
        }
        if (!CONFIG_FILE.toFile().exists()) {
            return;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE.toFile())) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) {
                return;
            }
            for (int page = 1; page <= SpellPoolPage.PAGE_COUNT; page++) {
                JsonArray array = root.getAsJsonArray("page" + page);
                if (array == null) {
                    continue;
                }
                List<SpellPoolEntry> entries = PAGES.get(page);
                for (JsonElement element : array) {
                    String idText = element.isJsonPrimitive() ? element.getAsString() : element.getAsJsonObject().get("spell").getAsString();
                    ResourceLocation id = ResourceLocation.parse(idText);
                    AbstractSpell spell = SpellRegistry.getSpell(id);
                    if (spell != SpellRegistry.none()) {
                        int level = element.isJsonPrimitive() ? 1 : element.getAsJsonObject().get("level").getAsInt();
                        entries.add(new SpellPoolEntry(id, clampLevel(spell, level)));
                    }
                }
            }
        } catch (Exception ignored) {
            PAGES.clear();
            for (int page = 1; page <= SpellPoolPage.PAGE_COUNT; page++) {
                PAGES.put(page, new ArrayList<>());
            }
        }
    }

    private static void save() {
        JsonObject root = new JsonObject();
        for (int page = 1; page <= SpellPoolPage.PAGE_COUNT; page++) {
            JsonArray array = new JsonArray();
            for (SpellPoolEntry entry : PAGES.computeIfAbsent(page, ignored -> new ArrayList<>())) {
                JsonObject savedEntry = new JsonObject();
                savedEntry.addProperty("spell", entry.spellId().toString());
                savedEntry.addProperty("level", entry.level());
                array.add(savedEntry);
            }
            root.add("page" + page, array);
        }
        try {
            CONFIG_FILE.getParent().toFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
                GSON.toJson(root, writer);
            }
        } catch (Exception ignored) {
        }
    }
}
