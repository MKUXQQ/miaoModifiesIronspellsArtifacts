package com.example.portableinscriptiontable.pool;

public final class SpellPoolPage {
    public static final int PAGE_COUNT = 5;

    private SpellPoolPage() {
    }

    public static int clamp(int page) {
        return Math.max(1, Math.min(PAGE_COUNT, page));
    }

    public static int next(int page) {
        int clamped = clamp(page);
        return clamped == PAGE_COUNT ? 1 : clamped + 1;
    }
}
