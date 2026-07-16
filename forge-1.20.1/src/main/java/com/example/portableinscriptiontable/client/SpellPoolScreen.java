package com.example.portableinscriptiontable.client;

import com.example.portableinscriptiontable.network.ModNetwork;
import com.example.portableinscriptiontable.network.SaveSpellPoolPayload;
import com.example.portableinscriptiontable.pool.SpellPoolPage;
import com.example.portableinscriptiontable.pool.SpellPoolRow;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpellPoolScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int HEADER_HEIGHT = 54;
    private final int page;
    private final List<RowEditor> rows = new ArrayList<>();
    private EditBox searchBox;
    private int scrollIndex;

    public SpellPoolScreen(int page, List<SpellPoolRow> rows) {
        super(Component.translatable("screen.portable_inscription_table.spell_pool"));
        this.page = SpellPoolPage.clamp(page);
        replaceRows(rows);
    }

    public int page() {
        return page;
    }

    public void replaceRows(List<SpellPoolRow> newRows) {
        rows.clear();
        for (SpellPoolRow row : newRows) {
            rows.add(new RowEditor(row));
        }
        if (searchBox != null) {
            clearWidgets();
            init();
        }
    }

    @Override
    protected void init() {
        searchBox = new EditBox(font, 12, 24, 180, 18, Component.translatable("screen.portable_inscription_table.search"));
        searchBox.setHint(Component.translatable("screen.portable_inscription_table.search"));
        addRenderableWidget(searchBox);
        addRenderableWidget(Button.builder(Component.translatable("screen.portable_inscription_table.save"), button -> save())
                .bounds(width - 102, 22, 90, 20)
                .build());
        for (RowEditor row : rows) {
            row.addWidgets();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        List<RowEditor> visible = filteredRows();
        int maxVisible = Math.max(1, (height - HEADER_HEIGHT - 12) / ROW_HEIGHT);
        scrollIndex = Math.min(scrollIndex, Math.max(0, visible.size() - maxVisible));
        updateRowWidgetVisibility(visible);
        int y = HEADER_HEIGHT;
        for (int i = scrollIndex; i < visible.size() && y + ROW_HEIGHT <= height - 8; i++) {
            RowEditor row = visible.get(i);
            graphics.fill(10, y - 2, width - 10, y + ROW_HEIGHT - 4, row.enabled ? 0x66334F33 : 0x66000000);
            y += ROW_HEIGHT;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(font, Component.translatable("screen.portable_inscription_table.spell_pool_page", page), 12, 8, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("screen.portable_inscription_table.column_spell"), 12, 48, 0xD7D7D7);
        y = HEADER_HEIGHT;
        for (int i = scrollIndex; i < visible.size() && y + ROW_HEIGHT <= height - 8; i++) {
            visible.get(i).renderName(graphics, 12, y);
            y += ROW_HEIGHT;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (scrollY < 0) {
            scrollIndex++;
        } else if (scrollY > 0) {
            scrollIndex = Math.max(0, scrollIndex - 1);
        }
        return true;
    }

    private void updateRowWidgetVisibility(List<RowEditor> visible) {
        for (RowEditor row : rows) {
            row.hide();
        }
        int y = HEADER_HEIGHT;
        for (int i = scrollIndex; i < visible.size() && y + ROW_HEIGHT <= height - 8; i++) {
            visible.get(i).show(width - 92, y);
            y += ROW_HEIGHT;
        }
    }

    private List<RowEditor> filteredRows() {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return rows;
        }
        return rows.stream().filter(row -> row.matches(query)).toList();
    }

    private void save() {
        ModNetwork.sendToServer(new SaveSpellPoolPayload(page, rows.stream().map(RowEditor::toRow).toList()));
    }

    private final class RowEditor {
        private final SpellPoolRow source;
        private final Button toggle;
        private boolean enabled;

        private RowEditor(SpellPoolRow source) {
            this.source = source;
            this.enabled = source.enabled();
            this.toggle = Button.builder(label(), button -> {
                enabled = !enabled;
                button.setMessage(label());
            }).bounds(0, 0, 80, 18).build();
            this.toggle.visible = false;
            this.toggle.active = false;
        }

        private Component label() {
            return Component.translatable(enabled
                    ? "screen.portable_inscription_table.pool_on"
                    : "screen.portable_inscription_table.pool_off");
        }

        private void addWidgets() {
            addRenderableWidget(toggle);
        }

        private void show(int x, int y) {
            toggle.setX(x);
            toggle.setY(y);
            toggle.visible = true;
            toggle.active = true;
        }

        private void hide() {
            toggle.visible = false;
            toggle.active = false;
            toggle.setFocused(false);
        }

        private void renderName(GuiGraphics graphics, int x, int y) {
            graphics.drawString(font, source.displayName(), x, y + 2, 0xFFFFFF);
            graphics.drawString(font, source.source() + " / " + source.castType(), x, y + 12, 0xAAAAAA);
        }

        private boolean matches(String query) {
            return source.spellId().toString().toLowerCase(Locale.ROOT).contains(query)
                    || source.displayName().toLowerCase(Locale.ROOT).contains(query)
                    || source.source().toLowerCase(Locale.ROOT).contains(query)
                    || source.castType().toLowerCase(Locale.ROOT).contains(query);
        }

        private SpellPoolRow toRow() {
            return new SpellPoolRow(source.spellId(), source.displayName(), source.source(), source.castType(), enabled);
        }
    }
}
