package org.rusting.policytablet.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

public class MapScreen extends Screen {

    private static final int GUI_WIDTH = 180;
    private static final int GUI_HEIGHT = 180;
    private static final int CELL_SIZE = 30;

    private static final ResourceLocation MAP_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("policytablet", "textures/gui/map.png");

    private final Random random = new Random();

    private int[][] cellFlags;
    private int cols;
    private int rows;

    public MapScreen() {
        super(Component.literal("Tablet"));
    }

    @Override
    protected void init() {
        int guiLeft = (width - GUI_WIDTH) / 2;
        int guiTop = (height - GUI_HEIGHT) / 2;

        initRandomFlags();
    }

    private void initRandomFlags() {
        cols = GUI_WIDTH / CELL_SIZE;
        rows = GUI_HEIGHT / CELL_SIZE;

        cellFlags = new int[cols][rows];

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                cellFlags[x][y] = random.nextInt(4);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int guiLeft = (width - GUI_WIDTH) / 2;
        int guiTop = (height - GUI_HEIGHT) / 2;

        renderBackground(guiGraphics);

        guiGraphics.fill(guiLeft, guiTop,
                guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT,
                0xFF222222);

        guiGraphics.blit(MAP_TEXTURE,
                guiLeft, guiTop,
                0, 0,
                GUI_WIDTH, GUI_HEIGHT,
                GUI_WIDTH, GUI_HEIGHT);

        for (int x = 0; x <= GUI_WIDTH; x += CELL_SIZE) {
            guiGraphics.fill(guiLeft + x, guiTop,
                    guiLeft + x + 1, guiTop + GUI_HEIGHT,
                    0xAA0000FF);
        }

        for (int y = 0; y <= GUI_HEIGHT; y += CELL_SIZE) {
            guiGraphics.fill(guiLeft, guiTop + y,
                    guiLeft + GUI_WIDTH, guiTop + y + 1,
                    0xAA0000FF);
        }

//        if (cellFlags == null) return;
//
//        for (int cx = 0; cx < cols; cx++) {
//            for (int cy = 0; cy < rows; cy++) {
//                int cellX = guiLeft + cx * CELL_SIZE;
//                int cellY = guiTop + cy * CELL_SIZE;
//
//                drawFlag(guiGraphics, cellX, cellY, CELL_SIZE, cellFlags[cx][cy]);
//            }
//        }

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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}