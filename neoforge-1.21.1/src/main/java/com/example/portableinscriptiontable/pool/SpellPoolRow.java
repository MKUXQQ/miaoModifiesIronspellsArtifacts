package com.example.portableinscriptiontable.pool;

import net.minecraft.resources.ResourceLocation;

public record SpellPoolRow(ResourceLocation spellId, String displayName, String source, String castType, boolean enabled) {
}
