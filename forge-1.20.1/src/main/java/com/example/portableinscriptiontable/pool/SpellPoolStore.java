package com.example.portableinscriptiontable.pool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("portable_inscription_table_spell_pools.json");
    private static final Map<Integer, Set<ResourceLocation>> PAGES = new LinkedHashMap<>();
    private static boolean loaded;

    private SpellPoolStore() {
    }

    public static List<SpellPoolRow> snapshot(int page) {
        ensureLoaded();
        int clampedPage = SpellPoolPage.clamp(page);
        Set<ResourceLocation> enabled = PAGES.computeIfAbsent(clampedPage, ignored -> new LinkedHashSet<>());
        List<SpellPoolRow> rows = new ArrayList<>();
        for (AbstractSpell spell : SpellRegistry.REGISTRY.get()) {
            ResourceLocation id = spell.getSpellResource();
            rows.add(new SpellPoolRow(id, spell.getSpellName(), id.getNamespace(), spell.getCastType().name(), enabled.contains(id)));
        }
        rows.sort(Comparator.comparing(row -> row.spellId().toString()));
        return rows;
    }

    public static void replacePage(int page, List<SpellPoolRow> rows) {
        ensureLoaded();
        Set<ResourceLocation> enabled = new LinkedHashSet<>();
        for (SpellPoolRow row : rows) {
            if (row.enabled() && SpellRegistry.getSpell(row.spellId()) != SpellRegistry.none()) {
                enabled.add(row.spellId());
            }
        }
        PAGES.put(SpellPoolPage.clamp(page), enabled);
        save();
    }

    public static List<ResourceLocation> enabledSpellIds(int page) {
        ensureLoaded();
        return new ArrayList<>(PAGES.computeIfAbsent(SpellPoolPage.clamp(page), ignored -> new LinkedHashSet<>()));
    }

    public static List<ResourceLocation> enabledSpellIdsAcrossAllPages() {
        ensureLoaded();
        Set<ResourceLocation> ids = new LinkedHashSet<>();
        for (int page = 1; page <= SpellPoolPage.PAGE_COUNT; page++) {
            ids.addAll(PAGES.computeIfAbsent(page, ignored -> new LinkedHashSet<>()));
        }
        return new ArrayList<>(ids);
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
            PAGES.put(page, new LinkedHashSet<>());
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
                String key = "page" + page;
                if (!root.has(key) || !root.get(key).isJsonArray()) {
                    continue;
                }
                JsonArray array = root.getAsJsonArray(key);
                Set<ResourceLocation> ids = PAGES.get(page);
                for (int i = 0; i < array.size(); i++) {
                    ResourceLocation id = new ResourceLocation(array.get(i).getAsString());
                    if (SpellRegistry.getSpell(id) != SpellRegistry.none()) {
                        ids.add(id);
                    }
                }
            }
        } catch (Exception ignored) {
            PAGES.clear();
            for (int page = 1; page <= SpellPoolPage.PAGE_COUNT; page++) {
                PAGES.put(page, new LinkedHashSet<>());
            }
        }
    }

    private static void save() {
        JsonObject root = new JsonObject();
        for (int page = 1; page <= SpellPoolPage.PAGE_COUNT; page++) {
            JsonArray array = new JsonArray();
            for (ResourceLocation id : PAGES.computeIfAbsent(page, ignored -> new LinkedHashSet<>())) {
                array.add(id.toString());
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
