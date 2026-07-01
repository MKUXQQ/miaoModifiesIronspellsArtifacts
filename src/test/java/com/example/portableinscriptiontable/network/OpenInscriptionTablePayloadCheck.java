package com.example.portableinscriptiontable.network;

import net.minecraft.resources.ResourceLocation;

public final class OpenInscriptionTablePayloadCheck {
    private OpenInscriptionTablePayloadCheck() {
    }

    public static void main(String[] args) {
        ResourceLocation id = OpenInscriptionTablePayload.TYPE.id();

        assertEquals("portable_inscription_table", id.getNamespace(), "payload namespace");
        assertEquals("open_inscription_table", id.getPath(), "payload path");
    }

    private static void assertEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
