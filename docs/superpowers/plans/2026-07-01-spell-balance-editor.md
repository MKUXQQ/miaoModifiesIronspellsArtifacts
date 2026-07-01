# Spell Balance Editor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a cat-rune item that opens an in-game table for editing Iron's Spells spell cast time, cooldown, mana multiplier, and damage/power multiplier.

**Architecture:** Register a new item and client screen. The client requests a server-owned spell snapshot, edits rows locally, and sends validated values back. The server persists JSON overrides and applies them immediately to `SpellRegistry.REGISTRY`, using Iron's config-backed parameters where possible and reflection for `castTime`.

**Tech Stack:** Java 21, Minecraft 1.21.1, NeoForge 21.1.200, Iron's Spells 1.21.1.

---

## Tasks

- [ ] Register `cat_rune` item and model resources.
- [ ] Generate a safe 16x16 cat-rune item texture.
- [ ] Add spell balance row/value classes and validation checks.
- [ ] Add server override store that reads/writes JSON and applies values to registered spells.
- [ ] Add network payloads for opening, syncing, and saving the editor.
- [ ] Add client management-table screen with searchable spell rows and editable fields.
- [ ] Localize existing keybind text and new UI/item text to Chinese.
- [ ] Run `.\gradlew.bat build` and produce the updated jar.
