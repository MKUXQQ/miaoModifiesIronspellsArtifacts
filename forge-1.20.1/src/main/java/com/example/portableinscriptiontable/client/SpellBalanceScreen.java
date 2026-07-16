package com.example.portableinscriptiontable.client;

import com.example.portableinscriptiontable.balance.SpellBalanceRow;
import com.example.portableinscriptiontable.balance.SpellBalanceValues;
import com.example.portableinscriptiontable.network.ModNetwork;
import com.example.portableinscriptiontable.network.SaveSpellBalancePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpellBalanceScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int HEADER_HEIGHT = 54;
    private static final int BOX_WIDTH = 62;
    private final List<SpellBalanceRow> sourceRows = new ArrayList<>();
    private final List<RowEditor> rows = new ArrayList<>();
    private EditBox searchBox;
    private int scrollIndex;

    public SpellBalanceScreen(List<SpellBalanceRow> rows) {
        super(Component.translatable("screen.portable_inscription_table.spell_balance"));
        replaceRows(rows);
    }

    public void replaceRows(List<SpellBalanceRow> newRows) {
        String query = searchBox == null ? "" : searchBox.getValue();
        sourceRows.clear();
        sourceRows.addAll(newRows);
        rows.clear();
        if (font != null) {
            rebuildEditors();
        }
        if (searchBox != null) {
            clearWidgets();
            init();
            searchBox.setValue(query);
        }
    }

    @Override
    protected void init() {
        rebuildEditors();
        searchBox = new EditBox(font, 12, 24, 180, 18, Component.translatable("screen.portable_inscription_table.search"));
        searchBox.setHint(Component.translatable("screen.portable_inscription_table.search"));
        addRenderableWidget(searchBox);
        addRenderableWidget(Button.builder(
                Component.translatable("screen.portable_inscription_table.save"),
                button -> save()
        ).bounds(width - 102, 22, 90, 20).build());
        for (RowEditor row : rows) {
            row.addWidgets();
        }
    }

    private void rebuildEditors() {
        rows.clear();
        for (SpellBalanceRow row : sourceRows) {
            rows.add(new RowEditor(row));
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
            visible.get(i).renderRowBackground(graphics, 12, y, width);
            y += ROW_HEIGHT;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        y = HEADER_HEIGHT;
        for (int i = scrollIndex; i < visible.size() && y + ROW_HEIGHT <= height - 8; i++) {
            visible.get(i).renderSelectedRowOverlay(graphics, 12, y, width);
            y += ROW_HEIGHT;
        }
        for (RowEditor row : visible) {
            row.renderFocusedBoxBorder(graphics);
        }
        renderFixedLabels(graphics);
        y = HEADER_HEIGHT;
        for (int i = scrollIndex; i < visible.size() && y + ROW_HEIGHT <= height - 8; i++) {
            visible.get(i).renderName(graphics, 12, y);
            y += ROW_HEIGHT;
        }
    }

    private void renderFixedLabels(GuiGraphics graphics) {
        graphics.drawString(font, title, 12, 8, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("screen.portable_inscription_table.column_spell"), 12, 48, 0xD7D7D7);
        graphics.drawString(font, Component.translatable("screen.portable_inscription_table.column_survival"), width - 542, 48, 0xD7D7D7);
        graphics.drawString(font, Component.translatable("screen.portable_inscription_table.column_cast"), width - 466, 48, 0xD7D7D7);
        graphics.drawString(font, Component.translatable("screen.portable_inscription_table.column_cooldown"), width - 390, 48, 0xD7D7D7);
        graphics.drawString(font, Component.translatable("screen.portable_inscription_table.column_mana"), width - 314, 48, 0xD7D7D7);
        graphics.drawString(font, Component.translatable("screen.portable_inscription_table.column_power"), width - 238, 48, 0xD7D7D7);
        graphics.drawString(font, Component.translatable("screen.portable_inscription_table.column_projectile_speed"), width - 162, 48, 0xD7D7D7);
    }

    private void updateRowWidgetVisibility(List<RowEditor> visible) {
        for (RowEditor row : rows) {
            row.hide(!visible.contains(row));
        }
        int y = HEADER_HEIGHT;
        for (int i = scrollIndex; i < visible.size() && y + ROW_HEIGHT <= height - 8; i++) {
            visible.get(i).show(width, y);
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

    private List<RowEditor> filteredRows() {
        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return rows;
        }
        return rows.stream().filter(row -> row.matches(query)).toList();
    }

    private void save() {
        List<SpellBalanceRow> savedRows = rows.stream().map(RowEditor::toRow).toList();
        ModNetwork.sendToServer(new SaveSpellBalancePayload(savedRows));
    }

    private final class RowEditor {
        private final SpellBalanceRow source;
        private final EditBox cast;
        private final EditBox cooldown;
        private final EditBox mana;
        private final EditBox power;
        private final EditBox projectileSpeed;
        private final Button survival;
        private boolean survivalAllowed;

        private RowEditor(SpellBalanceRow source) {
            this.source = source;
            this.survivalAllowed = source.values().survivalAllowed();
            this.cast = box(format(source.values().castTimeSeconds()));
            this.cooldown = box(format(source.values().cooldownSeconds()));
            this.mana = box(format(source.values().manaCostMultiplier()));
            this.power = box(format(source.values().powerMultiplier()));
            this.projectileSpeed = box(format(source.values().projectileSpeed()));
            this.survival = Button.builder(survivalLabel(), button -> {
                survivalAllowed = !survivalAllowed;
                button.setMessage(survivalLabel());
            }).bounds(0, 0, BOX_WIDTH, 18).build();
            this.survival.visible = false;
            this.survival.active = false;
        }

        private EditBox box(String value) {
            EditBox box = new EditBox(font, 0, 0, BOX_WIDTH, 18, Component.empty());
            box.setValue(value);
            box.visible = false;
            box.active = false;
            return box;
        }

        private Component survivalLabel() {
            return Component.translatable(survivalAllowed
                    ? "screen.portable_inscription_table.survival_on"
                    : "screen.portable_inscription_table.survival_off");
        }

        private void addWidgets() {
            addRenderableWidget(survival);
            addRenderableWidget(cast);
            addRenderableWidget(cooldown);
            addRenderableWidget(mana);
            addRenderableWidget(power);
            addRenderableWidget(projectileSpeed);
        }

        private void renderRowBackground(GuiGraphics graphics, int x, int y, int screenWidth) {
            boolean selected = hasFocusedWidget();
            graphics.fill(x - 2, y - 2, screenWidth - 10, y + ROW_HEIGHT - 4, SpellBalanceSelectionStyle.rowBackground(selected));
        }

        private void renderSelectedRowOverlay(GuiGraphics graphics, int x, int y, int screenWidth) {
            boolean selected = hasFocusedWidget();
            if (selected) {
                drawBorder(graphics, x - 3, y - 3, screenWidth - 9, y + ROW_HEIGHT - 3, SpellBalanceSelectionStyle.focusBorderColor());
                graphics.fill(x - 2, y - 2, screenWidth - 10, y - 1, 0xAAFFFFFF);
                graphics.fill(x - 2, y + ROW_HEIGHT - 5, screenWidth - 10, y + ROW_HEIGHT - 4, 0xAA000000);
            }
        }

        private void renderName(GuiGraphics graphics, int x, int y) {
            graphics.drawString(font, source.displayName(), x, y + 2, 0xFFFFFF);
            graphics.drawString(font, source.source() + " / " + source.castType(), x, y + 12, 0xAAAAAA);
        }

        private void show(int screenWidth, int y) {
            place(survival, screenWidth - 548, y);
            place(cast, screenWidth - 472, y);
            place(cooldown, screenWidth - 396, y);
            place(mana, screenWidth - 320, y);
            place(power, screenWidth - 244, y);
            place(projectileSpeed, screenWidth - 168, y);
        }

        private void place(AbstractWidget widget, int x, int y) {
            widget.setX(x);
            widget.setY(y);
            widget.visible = true;
            widget.active = true;
        }

        private void hide(boolean hiddenAfterRefresh) {
            if (!hiddenAfterRefresh) {
                return;
            }
            hide(survival, hiddenAfterRefresh);
            hide(cast, hiddenAfterRefresh);
            hide(cooldown, hiddenAfterRefresh);
            hide(mana, hiddenAfterRefresh);
            hide(power, hiddenAfterRefresh);
            hide(projectileSpeed, hiddenAfterRefresh);
        }

        private void hide(AbstractWidget widget, boolean hiddenAfterRefresh) {
            widget.visible = false;
            widget.active = false;
            if (SpellBalanceWidgetVisibility.shouldClearFocus(!hiddenAfterRefresh)) {
                widget.setFocused(false);
            }
        }

        private boolean matches(String query) {
            return source.spellId().toString().toLowerCase(Locale.ROOT).contains(query)
                    || source.displayName().toLowerCase(Locale.ROOT).contains(query)
                    || source.source().toLowerCase(Locale.ROOT).contains(query)
                    || source.castType().toLowerCase(Locale.ROOT).contains(query);
        }

        private boolean hasFocusedWidget() {
            return survival.isFocused() || cast.isFocused() || cooldown.isFocused() || mana.isFocused()
                    || power.isFocused() || projectileSpeed.isFocused();
        }

        private void renderFocusedBoxBorder(GuiGraphics graphics) {
            renderWidgetBorderIfFocused(graphics, survival);
            renderWidgetBorderIfFocused(graphics, cast);
            renderWidgetBorderIfFocused(graphics, cooldown);
            renderWidgetBorderIfFocused(graphics, mana);
            renderWidgetBorderIfFocused(graphics, power);
            renderWidgetBorderIfFocused(graphics, projectileSpeed);
        }

        private void renderWidgetBorderIfFocused(GuiGraphics graphics, AbstractWidget widget) {
            if (widget.isFocused() && widget.visible) {
                drawBorder(
                        graphics,
                        widget.getX() - 2,
                        widget.getY() - 2,
                        widget.getX() + widget.getWidth() + 2,
                        widget.getY() + widget.getHeight() + 2,
                        SpellBalanceSelectionStyle.focusBorderColor()
                );
            }
        }

        private void drawBorder(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
            graphics.fill(left, top, right, top + 1, color);
            graphics.fill(left, bottom - 1, right, bottom, color);
            graphics.fill(left, top, left + 1, bottom, color);
            graphics.fill(right - 1, top, right, bottom, color);
        }

        private SpellBalanceRow toRow() {
            SpellBalanceValues values = SpellBalanceValues.sanitize(
                    SpellBalanceValues.secondsToTicks(parse(cast.getValue(), source.values().castTimeSeconds())),
                    parse(cooldown.getValue(), source.values().cooldownSeconds()),
                    parse(mana.getValue(), source.values().manaCostMultiplier()),
                    parse(power.getValue(), source.values().powerMultiplier()),
                    survivalAllowed,
                    parse(projectileSpeed.getValue(), source.values().projectileSpeed())
            );
            return new SpellBalanceRow(source.spellId(), source.displayName(), source.source(), source.castType(), values);
        }

        private double parse(String value, double fallback) {
            try {
                return Double.parseDouble(value.trim());
            } catch (Exception ignored) {
                return fallback;
            }
        }

        private String format(double value) {
            return String.format(Locale.ROOT, "%.2f", value);
        }
    }
}
