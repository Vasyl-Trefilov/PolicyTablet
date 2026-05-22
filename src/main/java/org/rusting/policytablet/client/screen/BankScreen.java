package org.rusting.policytablet.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.rusting.policytablet.client.ClientPlayerData;

import java.util.Random;

public class BankScreen extends Screen {

    private static final int GUI_WIDTH = 180;
    private static final int GUI_HEIGHT = 180;

    public BankScreen() {
        super(Component.literal("Bank"));
    }

    @Override
    protected void init() {
        int guiLeft = (width - GUI_WIDTH) / 2;
        int guiTop = (height - GUI_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int guiLeft = (width - GUI_WIDTH) / 2;
        int guiTop = (height - GUI_HEIGHT) / 2;

        renderBackground(guiGraphics);

        guiGraphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xFF222222);
        guiGraphics.renderOutline(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF888888);

        int land = intFromLand(ClientPlayerData.COUNTRY);
        drawFlag(guiGraphics, guiLeft + GUI_WIDTH/2 - 25, guiTop + 10, 50, land);

        guiGraphics.drawString(font, "Balance: $" + ClientPlayerData.BALANCE, guiLeft + 10, guiTop + 70, 0xFFFFFF);
        guiGraphics.drawString(font, "Income: $" + ClientPlayerData.INCOME + "/s", guiLeft + 10, guiTop + 92, 0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void drawFlag(GuiGraphics guiGraphics, int x, int y, int size, int flagType) {
        int stripe = size / 3;

        switch (flagType) {
            case 0 -> {
                guiGraphics.fill(x, y, x + size, y + stripe, 0x55000000);
                guiGraphics.fill(x, y + stripe, x + size, y + 2 * stripe, 0x55FF0000);
                guiGraphics.fill(x, y + 2 * stripe, x + size, y + size, 0x55FFFF00);
            }
            case 1 -> {
                guiGraphics.fill(x, y, x + size, y + stripe, 0x55FFFFFF);
                guiGraphics.fill(x, y + stripe, x + size, y + 2 * stripe, 0x550000FF);
                guiGraphics.fill(x, y + 2 * stripe, x + size, y + size, 0x55FF0000);
            }
            case 2 -> {
                guiGraphics.fill(x, y, x + size, y + stripe, 0x55FF0000);
                guiGraphics.fill(x, y + stripe, x + size, y + 2 * stripe, 0x55FFFFFF);
                guiGraphics.fill(x, y + 2 * stripe, x + size, y + size, 0x550000FF);
            }
            case 3 -> {
                guiGraphics.fill(x, y, x + size, y + size, 0x55FF0000);

                int e = size / 3;
                int ex = x + (size - e) / 2;
                int ey = y + (size - e) / 2;

                guiGraphics.fill(ex, ey, ex + e, ey + e, 0x55FFFF00);
            }
        }
    }

    private int intFromLand(String land) {
        switch (land) {
            case "Germany" -> {
                return 0;
            }
            case "Russland" -> {
                return 1;
            }
            case "USA" -> {
                return 2;
            }
            case "China" -> {
                return 3;
            }
        }
        return 1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}