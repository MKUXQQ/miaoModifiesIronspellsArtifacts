# Random Spellbook Pool Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a stick-model item that edits five random spell pools and a `/QQironspell` command that gives random Iron's Spells spell books named `《终焉之书》` to one player or all players.

**Architecture:** Add a shared pool store, row DTOs, client screen, packets, editor item, and command in both `forge-1.20.1` and `neoforge-1.21.1`. Reuse Iron's Spells APIs for spell registry, spell book item, and spell container serialization.

**Tech Stack:** Java 17, Forge 1.20.1, NeoForge 1.21.1, Iron's Spells and Spellbooks.

## Global Constraints

- Command root is exactly `/QQironspell`.
- There are exactly 5 editable pages.
- Sneak-use the editor item to switch pages; normal use opens the current page.
- The editor item model is `minecraft:item/stick`.
- Build both `forge-1.20.1` and `neoforge-1.21.1`.

---

### Task 1: Shared Pool Model And Store

**Files:**
- Create under both loaders: `pool/SpellPoolRow.java`, `pool/SpellPoolPage.java`, `pool/SpellPoolStore.java`, `pool/RandomSpellBookFactory.java`
- Test: extend `OpenInscriptionTablePayloadCheck.java` with page clamping and selection checks.

**Interfaces:**
- `SpellPoolStore.PAGE_COUNT = 5`
- `SpellPoolStore.snapshot(int page): List<SpellPoolRow>`
- `SpellPoolStore.replacePage(int page, List<SpellPoolRow> rows): void`
- `SpellPoolStore.enabledSpellIds(int page): List<ResourceLocation>`
- `RandomSpellBookFactory.create(ServerLevel level, int slots, List<ResourceLocation> pool): ItemStack`

- [ ] Write failing unit checks for page clamping and selected row filtering.
- [ ] Implement the store with JSON config file `portable_inscription_table_spell_pools.json`.
- [ ] Implement the book factory using Iron's `WIMPY_SPELL_BOOK`, `ISpellContainer`, and selected pool ids.
- [ ] Run unit checks.

### Task 2: Networking And Editor Screen

**Files:**
- Create under both loaders: `network/RequestSpellPoolPayload.java`, `network/SaveSpellPoolPayload.java`, `network/SyncSpellPoolPayload.java`, `client/SpellPoolScreen.java`, `client/SpellPoolClientScreenBridge.java`
- Modify both `ModNetwork.java`.

**Interfaces:**
- Client opens `SpellPoolScreen(rows, page)`.
- Save packet sends page and rows to server.

- [ ] Add packets for request/save/sync.
- [ ] Build a simple searchable toggle list screen.
- [ ] Save only the current page.
- [ ] Run both loader builds.

### Task 3: Item, Resources, And Commands

**Files:**
- Create under both loaders: `item/RandomSpellPoolEditorItem.java`, `command/QQIronSpellCommand.java`
- Modify both `ModItems.java`, `PortableInscriptionTable.java`, language JSON files, item model JSON files.

**Interfaces:**
- `/QQironspell give <targets> <slots>`
- `/QQironspell giveAll <slots>`

- [ ] Register the item with stick model and creative tab entry.
- [ ] Register command events in both loaders.
- [ ] Give generated books to selected targets or all online players using the union of all 5 pages.
- [ ] Build both loaders and commit.
