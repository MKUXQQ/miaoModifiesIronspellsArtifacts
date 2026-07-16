package com.example.portableinscriptiontable.balance;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.redspace.ironsspellbooks.api.config.SpellConfigHolder;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SpellBalanceStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("portable_inscription_table_spell_balance.json");
    private static final Map<ResourceLocation, SpellBalanceValues> OVERRIDES = new LinkedHashMap<>();
    private static final Map<String, Number> DURATION_BASELINES = new LinkedHashMap<>();
    private static boolean loaded;

    private static Field castTimeField;
    private static Field spellConfigMapField;

    private SpellBalanceStore() {
    }

    public static void loadAndApply() {
        load();
        applyAll();
    }

    public static List<SpellBalanceRow> snapshot() {
        ensureLoaded();
        List<SpellBalanceRow> rows = new ArrayList<>();
        for (AbstractSpell spell : SpellRegistry.REGISTRY) {
            ResourceLocation id = spell.getSpellResource();
            SpellBalanceValues values = OVERRIDES.getOrDefault(id, currentValues(spell));
            rows.add(new SpellBalanceRow(
                    id,
                    spell.getSpellName(),
                    id.getNamespace(),
                    spell.getCastType().name(),
                    values
            ));
        }
        rows.sort(Comparator.comparing(row -> row.spellId().toString()));
        return rows;
    }

    public static void replaceAll(List<SpellBalanceRow> rows) {
        loaded = true;
        OVERRIDES.clear();
        for (SpellBalanceRow row : rows) {
            if (SpellRegistry.getSpell(row.spellId()) != SpellRegistry.none()) {
                OVERRIDES.put(row.spellId(), row.values());
            }
        }
        save();
        applyAll();
    }

    public static void applyRowsWithoutSaving(List<SpellBalanceRow> rows) {
        loaded = true;
        OVERRIDES.clear();
        for (SpellBalanceRow row : rows) {
            if (SpellRegistry.getSpell(row.spellId()) != SpellRegistry.none()) {
                OVERRIDES.put(row.spellId(), row.values());
            }
        }
        applyAll();
    }

    public static SpellBalanceValues valuesFor(ResourceLocation spellId) {
        ensureLoaded();
        return OVERRIDES.get(spellId);
    }

    public static SpellBalanceValues currentValues(AbstractSpell spell) {
        return SpellBalanceValues.sanitize(
                spell.getCastTime(1),
                SpellConfigManager.getSpellConfigValue(spell, SpellConfigParameter.COOLDOWN_IN_SECONDS),
                SpellConfigManager.getSpellConfigValue(spell, SpellConfigParameter.MANA_MULTIPLIER),
                SpellConfigManager.getSpellConfigValue(spell, SpellConfigParameter.POWER_MULTIPLIER),
                true,
                1.0,
                1.0
        );
    }

    public static void applyAll() {
        ensureLoaded();
        for (Map.Entry<ResourceLocation, SpellBalanceValues> entry : OVERRIDES.entrySet()) {
            AbstractSpell spell = SpellRegistry.getSpell(entry.getKey());
            if (spell != SpellRegistry.none()) {
                apply(spell, entry.getValue());
            }
        }
    }

    private static void apply(AbstractSpell spell, SpellBalanceValues values) {
        setCastTime(spell, values.castTimeTicks());
        SpellConfigHolder holder = getConfigHolder(spell);
        if (holder != null) {
            holder.set(SpellConfigParameter.COOLDOWN_IN_SECONDS, values.cooldownSeconds());
            holder.set(SpellConfigParameter.MANA_MULTIPLIER, values.manaCostMultiplier());
            holder.set(SpellConfigParameter.POWER_MULTIPLIER, values.powerMultiplier());
        }
        applyDurationMultiplier(spell, values.durationMultiplier());
    }

    private static void load() {
        OVERRIDES.clear();
        loaded = true;
        if (!CONFIG_FILE.toFile().exists()) {
            return;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE.toFile())) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) {
                return;
            }
            for (String key : root.keySet()) {
                if (key.startsWith("_")) {
                    continue;
                }
                JsonObject json = root.getAsJsonObject(key);
                OVERRIDES.put(ResourceLocation.parse(key), SpellBalanceValues.sanitize(
                        json.has("castTimeTicks") ? json.get("castTimeTicks").getAsInt() : 0,
                        json.has("cooldownSeconds") ? json.get("cooldownSeconds").getAsDouble() : 0.0,
                        json.has("manaCostMultiplier") ? json.get("manaCostMultiplier").getAsDouble() : 1.0,
                        json.has("powerMultiplier") ? json.get("powerMultiplier").getAsDouble() : 1.0,
                        json.has("survivalAllowed") ? json.get("survivalAllowed").getAsBoolean() : true,
                        json.has("projectileSpeed") ? json.get("projectileSpeed").getAsDouble() : 1.0,
                        json.has("durationMultiplier") ? json.get("durationMultiplier").getAsDouble() : 1.0
                ));
            }
        } catch (Exception ignored) {
            OVERRIDES.clear();
        }
    }

    private static void ensureLoaded() {
        if (!loaded) {
            load();
        }
    }

    private static void save() {
        JsonObject root = new JsonObject();
        for (Map.Entry<ResourceLocation, SpellBalanceValues> entry : OVERRIDES.entrySet()) {
            SpellBalanceValues values = entry.getValue();
            JsonObject json = new JsonObject();
            json.addProperty("castTimeTicks", values.castTimeTicks());
            json.addProperty("cooldownSeconds", values.cooldownSeconds());
            json.addProperty("manaCostMultiplier", values.manaCostMultiplier());
            json.addProperty("powerMultiplier", values.powerMultiplier());
            json.addProperty("survivalAllowed", values.survivalAllowed());
            json.addProperty("projectileSpeed", values.projectileSpeed());
            json.addProperty("durationMultiplier", values.durationMultiplier());
            root.add(entry.getKey().toString(), json);
        }
        try {
            CONFIG_FILE.getParent().toFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
                GSON.toJson(root, writer);
            }
        } catch (Exception ignored) {
        }
    }

    private static void setCastTime(AbstractSpell spell, int ticks) {
        try {
            if (castTimeField == null) {
                castTimeField = AbstractSpell.class.getDeclaredField("castTime");
                castTimeField.setAccessible(true);
            }
            castTimeField.setInt(spell, ticks);
        } catch (Exception ignored) {
        }
    }

    private static void applyDurationMultiplier(AbstractSpell spell, double multiplier) {
        Class<?> type = spell.getClass();
        while (type != null && AbstractSpell.class.isAssignableFrom(type)) {
            for (Field field : type.getDeclaredFields()) {
                if (!isDurationField(field)) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    String key = spell.getSpellResource() + "|" + field.getDeclaringClass().getName() + "#" + field.getName();
                    Number base = DURATION_BASELINES.computeIfAbsent(key, ignored -> readNumber(field, spell));
                    writeNumber(field, spell, base, multiplier);
                } catch (Exception ignored) {
                }
            }
            type = type.getSuperclass();
        }
    }

    private static boolean isDurationField(Field field) {
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
            return false;
        }
        String name = field.getName().toLowerCase();
        Class<?> type = field.getType();
        return name.contains("duration")
                && (type == int.class || type == long.class || type == float.class || type == double.class);
    }

    private static Number readNumber(Field field, Object target) {
        try {
            Object value = field.get(target);
            return value instanceof Number number ? number : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static void writeNumber(Field field, Object target, Number base, double multiplier) throws IllegalAccessException {
        Class<?> type = field.getType();
        double value = Math.max(0.0, base.doubleValue() * multiplier);
        if (type == int.class) {
            field.setInt(target, (int) Math.round(value));
        } else if (type == long.class) {
            field.setLong(target, Math.round(value));
        } else if (type == float.class) {
            field.setFloat(target, (float) value);
        } else if (type == double.class) {
            field.setDouble(target, value);
        }
    }

    @SuppressWarnings("unchecked")
    private static SpellConfigHolder getConfigHolder(AbstractSpell spell) {
        try {
            if (spellConfigMapField == null) {
                spellConfigMapField = SpellConfigManager.class.getDeclaredField("config");
                spellConfigMapField.setAccessible(true);
            }
            ImmutableMap<AbstractSpell, SpellConfigHolder> config =
                    (ImmutableMap<AbstractSpell, SpellConfigHolder>) spellConfigMapField.get(SpellConfigManager.INSTANCE);
            return config.get(spell);
        } catch (Exception ignored) {
            return null;
        }
    }
}
