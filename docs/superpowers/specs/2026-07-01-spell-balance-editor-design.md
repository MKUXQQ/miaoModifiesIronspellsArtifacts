# Spell Balance Editor Design

## Goal

Add an in-game item that opens a management panel for every registered Iron's Spells spell, including spells added by addons. The panel lets the player edit cast time, cooldown, mana cost multiplier, and damage/power multiplier, then save changes so they take effect immediately.

## Confirmed UI

Use the A方案 management-table layout:

- Searchable list of all spells.
- Columns: spell name/id, source mod id, cast type, cast time seconds, cooldown seconds, mana cost multiplier, damage/power multiplier.
- A save button applies all edited values.
- The existing G-key inscription-table key text should be Chinese.

## Item

Add a new item named `猫咪符文`. The item uses a safe cat-rune icon instead of the provided explicit photo. Right-clicking the item opens the editor screen on the client.

The item is obtainable through `/give @p portable_inscription_table:cat_rune`.

## Data Model

The server owns the active balance data. Each spell override is keyed by spell id and stores:

- `castTimeTicks`
- `cooldownSeconds`
- `manaCostMultiplier`
- `powerMultiplier`

The server persists overrides to a JSON file under the config directory. On startup/reload/save, it applies overrides to all spells currently registered in `SpellRegistry.REGISTRY`, so addon spells are included automatically.

Iron's Spells already supports cooldown, mana multiplier, and power multiplier through its config system. For this addon, the runtime editor applies those same concepts through its own override store. Cast time is not exposed as an Iron's Spells config parameter, so the addon applies it to the `AbstractSpell.castTime` field by reflection.

## Runtime Behavior

When the screen opens, the client requests the latest spell balance snapshot from the server. The server sends one row per spell. The client edits local input fields. Pressing save sends all rows back to the server.

The server validates values:

- cast time seconds: minimum `0`
- cooldown seconds: minimum `0`
- mana multiplier: minimum `0`
- power multiplier: minimum `0`

After validation, the server stores the values, writes the config file, applies the overrides immediately, and sends a refreshed snapshot back to the player.

## Notes

The field labeled `伤害/强度倍率` maps to Iron's Spells `power_multiplier`. Many spells use spell power for damage, but some use it for healing, duration, summon strength, radius, or other effects.
