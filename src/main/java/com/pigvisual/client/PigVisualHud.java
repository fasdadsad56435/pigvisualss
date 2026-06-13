package com.pigvisual.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;

public class PigVisualHud implements HudRenderCallback {

    // Цвета (ARGB)
    private static final int COLOR_BG       = 0xAA0D0D14; // тёмный фон
    private static final int COLOR_WHITE     = 0xFFFFFFFF;
    private static final int COLOR_GRAY      = 0xFF888780;
    private static final int COLOR_PURPLE    = 0xFF7F77DD;
    private static final int COLOR_PURPLE_DK = 0xFF534AB7;
    private static final int COLOR_GREEN     = 0xFF5DCAA5;
    private static final int COLOR_AMBER     = 0xFFEF9F27;
    private static final int COLOR_RED       = 0xFFE24B4A;

    // Счётчик FPS (сглаживание)
    private double smoothFps = 0;

    @Override
    public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden) return;
        if (mc.player == null)   return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        drawWatermark(ctx, sw);
        drawFpsCounter(ctx, sw, mc);
        drawPlayerStats(ctx, sh, mc.player);
    }

    // ─── Водяной знак (верхний левый угол) ───────────────────────────────────
    private void drawWatermark(DrawContext ctx, int sw) {
        String title  = "Pig Visual";
        String sub    = "v1.0 | 1.21.4";

        int x = 6;
        int y = 6;
        int w = 80;
        int h = 24;

        // Фон + левая полоска
        ctx.fill(x,     y,     x + w, y + h, COLOR_BG);
        ctx.fill(x,     y,     x + 2, y + h, COLOR_PURPLE);

        // Текст
        ctx.drawText(
            MinecraftClient.getInstance().textRenderer,
            title, x + 6, y + 4, COLOR_WHITE, false
        );
        ctx.drawText(
            MinecraftClient.getInstance().textRenderer,
            sub, x + 6, y + 14, COLOR_GRAY, false
        );
    }

    // ─── Счётчик FPS (верхний правый угол) ───────────────────────────────────
    private void drawFpsCounter(DrawContext ctx, int sw, MinecraftClient mc) {
        int fps = mc.getCurrentFps();
        smoothFps = smoothFps * 0.85 + fps * 0.15;
        int displayFps = (int) Math.round(smoothFps);

        String label = "FPS";
        String value = String.valueOf(displayFps);

        int fpsColor;
        if (displayFps >= 120)     fpsColor = COLOR_GREEN;
        else if (displayFps >= 60) fpsColor = COLOR_AMBER;
        else                       fpsColor = COLOR_RED;

        int panelW = 48;
        int panelH = 24;
        int x = sw - panelW - 6;
        int y = 6;

        ctx.fill(x,         y, x + panelW, y + panelH, COLOR_BG);
        ctx.fill(x + panelW - 2, y, x + panelW, y + panelH, fpsColor);

        ctx.drawText(
            MinecraftClient.getInstance().textRenderer,
            label, x + 5, y + 4, COLOR_GRAY, false
        );
        ctx.drawText(
            MinecraftClient.getInstance().textRenderer,
            value, x + 5, y + 14, fpsColor, false
        );
    }

    // ─── Статы игрока: HP, еда, броня (нижний левый угол) ───────────────────
    private void drawPlayerStats(DrawContext ctx, int sh, PlayerEntity player) {
        int hp      = (int) Math.ceil(player.getHealth());
        int maxHp   = (int) Math.ceil(player.getMaxHealth());
        int food    = player.getHungerManager().getFoodLevel();
        int armor   = player.getArmor();

        int x = 6;
        int y = sh - 70;
        int barW = 90;
        int rowH = 18;

        // Фон всей панели
        ctx.fill(x, y, x + barW + 12, y + rowH * 3 + 6, COLOR_BG);
        ctx.fill(x, y, x + 2, y + rowH * 3 + 6, COLOR_PURPLE);

        // HP
        drawStatRow(ctx, x + 5, y + 3,  barW, "HP",    hp,   maxHp, COLOR_RED,    "❤");
        // Еда
        drawStatRow(ctx, x + 5, y + 3 + rowH, barW, "Food", food, 20, COLOR_AMBER, "🍖");
        // Броня
        drawStatRow(ctx, x + 5, y + 3 + rowH * 2, barW, "Armor", armor, 20, COLOR_PURPLE, "🛡");
    }

    private void drawStatRow(DrawContext ctx, int x, int y, int barW,
                              String label, int val, int max, int color, String icon) {
        var tr = MinecraftClient.getInstance().textRenderer;

        // Лейбл
        ctx.drawText(tr, label, x, y, COLOR_GRAY, false);

        // Значение
        String valStr = val + "/" + max;
        ctx.drawText(tr, valStr, x + 32, y, color, false);

        // Бар
        int bx = x;
        int by = y + 9;
        int bh = 3;
        int filled = max > 0 ? (int)((float) val / max * barW) : 0;

        ctx.fill(bx,          by, bx + barW, by + bh, 0xFF1E1D30); // трек
        ctx.fill(bx,          by, bx + filled, by + bh, color);   // заполнение
    }
}
