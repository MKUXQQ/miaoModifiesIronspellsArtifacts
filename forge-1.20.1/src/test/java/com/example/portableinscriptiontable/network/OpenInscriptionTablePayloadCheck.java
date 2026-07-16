package com.example.portableinscriptiontable.network;

import com.example.portableinscriptiontable.menu.SpellbookReturnPolicy;
import com.example.portableinscriptiontable.pool.SpellPoolPage;
import com.example.portableinscriptiontable.balance.SpellBalanceValues;
import com.example.portableinscriptiontable.client.SpellBalanceSelectionStyle;
import com.example.portableinscriptiontable.client.SpellBalanceWidgetVisibility;
import com.example.portableinscriptiontable.registry.ModRegistryIds;
import net.minecraft.resources.ResourceLocation;

public final class OpenInscriptionTablePayloadCheck {
    private OpenInscriptionTablePayloadCheck() {
    }

    public static void main(String[] args) {
        manualSpellbookReturnsToInventory();
        equippedSpellbookWritesBackToEquipmentSlot();
        spellBalanceValuesClampInvalidInputs();
        spellBalanceValuesConvertSecondsToTicks();
        visibleSpellBalanceWidgetsKeepFocus();
        selectedSpellBalanceRowUsesRaisedStyle();
        catRuneCreativeTabUsesStableId();
        spellPoolPageClampsLowValues();
        spellPoolPageClampsHighValues();
        ResourceLocation id = ModNetworkIds.OPEN_INSCRIPTION_TABLE;

        assertEquals("portable_inscription_table", id.getNamespace(), "payload namespace");
        assertEquals("open_inscription_table", id.getPath(), "payload path");
    }

    private static void manualSpellbookReturnsToInventory() {
        assertFalse(
                SpellbookReturnPolicy.shouldWriteBackToEquippedSlot(false),
                "manual spellbook return policy"
        );
    }

    private static void equippedSpellbookWritesBackToEquipmentSlot() {
        assertTrue(
                SpellbookReturnPolicy.shouldWriteBackToEquippedSlot(true),
                "equipped spellbook return policy"
        );
    }

    private static void spellBalanceValuesClampInvalidInputs() {
        SpellBalanceValues values = SpellBalanceValues.sanitize(-1, -2.0, Double.NaN, Double.POSITIVE_INFINITY);

        assertEquals(0, values.castTimeTicks(), "cast time clamp");
        assertEquals(0.0, values.cooldownSeconds(), "cooldown clamp");
        assertEquals(0.0, values.manaCostMultiplier(), "mana multiplier clamp");
        assertEquals(0.0, values.powerMultiplier(), "power multiplier clamp");
    }

    private static void spellBalanceValuesConvertSecondsToTicks() {
        assertEquals(30, SpellBalanceValues.secondsToTicks(1.5), "seconds to ticks");
    }

    private static void visibleSpellBalanceWidgetsKeepFocus() {
        assertFalse(
                SpellBalanceWidgetVisibility.shouldClearFocus(true),
                "visible spell balance widgets keep focus"
        );
        assertTrue(
                SpellBalanceWidgetVisibility.shouldClearFocus(false),
                "hidden spell balance widgets clear focus"
        );
    }

    private static void selectedSpellBalanceRowUsesRaisedStyle() {
        assertEquals(0xAA1F2530, SpellBalanceSelectionStyle.rowBackground(true), "selected row background");
        assertEquals(0x66000000, SpellBalanceSelectionStyle.rowBackground(false), "normal row background");
        assertEquals(0xFFFFD36A, SpellBalanceSelectionStyle.focusBorderColor(), "focused box border");
    }

    private static void catRuneCreativeTabUsesStableId() {
        assertEquals(
                "portable_inscription_table:cat_rune_tab",
                ModRegistryIds.CAT_RUNE_TAB_ID.toString(),
                "cat rune creative tab id"
        );
    }

    private static void spellPoolPageClampsLowValues() {
        assertEquals(1, SpellPoolPage.clamp(0), "spell pool low page clamp");
    }

    private static void spellPoolPageClampsHighValues() {
        assertEquals(5, SpellPoolPage.clamp(99), "spell pool high page clamp");
    }

    private static void assertEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertEquals(double expected, double actual, String label) {
        if (Double.compare(expected, actual) != 0) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertFalse(boolean actual, String label) {
        if (actual) {
            throw new AssertionError(label + " expected <false> but was <true>");
        }
    }

    private static void assertTrue(boolean actual, String label) {
        if (!actual) {
            throw new AssertionError(label + " expected <true> but was <false>");
        }
    }
}
