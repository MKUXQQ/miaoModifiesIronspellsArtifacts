package com.example.portableinscriptiontable.pool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    public static final int CONTAINER_SIZE = 54;
    private static final String PLACEHOLDER_TAG = "PortableInscriptionPoolPlaceholder";
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

    public static SimpleContainer createContainer(int page) {
        int clampedPage = SpellPoolPage.clamp(page);
        SimpleContainer container = new SimpleContainer(CONTAINER_SIZE) {
            @Override
            public void stopOpen(Player player) {
                super.stopOpen(player);
                replacePageFromItems(clampedPage, this);
                returnUserItems(player, this);
            }
        };
        List<ResourceLocation> ids = enabledSpellIds(clampedPage);
        int slot = 0;
        for (ResourceLocation id : ids) {
            if (slot >= CONTAINER_SIZE) {
                break;
            }
            AbstractSpell spell = SpellRegistry.getSpell(id);
            if (spell == SpellRegistry.none()) {
                continue;
            }
            container.setItem(slot++, createScroll(spell));
        }
        return container;
    }

    public static void replacePageFromItems(int page, SimpleContainer container) {
        ensureLoaded();
        Set<ResourceLocation> enabled = new LinkedHashSet<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            enabled.addAll(spellIdsFromStack(container.getItem(slot)));
        }
        PAGES.put(SpellPoolPage.clamp(page), enabled);
        save();
    }

    private static List<ResourceLocation> spellIdsFromStack(ItemStack stack) {
        List<ResourceLocation> ids = new ArrayList<>();
        if (stack.isEmpty() || !ISpellContainer.isSpellContainer(stack)) {
            return ids;
        }
        ISpellContainer container = ISpellContainer.get(stack);
        for (SpellData spellData : container.getActiveSpells()) {
            if (spellData.getSpell() != SpellRegistry.none()) {
                ids.add(spellData.getSpell().getSpellResource());
            }
        }
        return ids;
    }

    private static ItemStack createScroll(AbstractSpell spell) {
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell, 1, stack);
        stack.getOrCreateTag().putBoolean(PLACEHOLDER_TAG, true);
        return stack;
    }

    private static void returnUserItems(Player player, SimpleContainer container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty() || isPlaceholder(stack)) {
                continue;
            }
            ItemStack returned = stack.copy();
            if (!player.getInventory().add(returned)) {
                player.drop(returned, false);
            }
        }
    }

    private static boolean isPlaceholder(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(PLACEHOLDER_TAG);
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
