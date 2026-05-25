package org.rusting.policytablet.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.rusting.policytablet.client.ClientPlayerData;
import org.rusting.policytablet.client.screen.MapScreen;

public class TabletScreen extends Screen {

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    public TabletScreen() {
        super(Component.literal("Tablet"));
    }

    @Override
    protected void init() {
        int guiLeft = (width - GUI_WIDTH) / 2;
        int guiTop = (height - GUI_HEIGHT) / 2;

        addRenderableWidget(Button.builder(
                Component.literal("Map"),
                button -> {
                    if (Minecraft.getInstance().player != null) {
                        net.minecraft.client.Minecraft.getInstance().setScreen(new MapScreen());
                    }
                })
                .bounds(guiLeft + 38, guiTop + 10, 100, 20)
                .build());

        addRenderableWidget(Button.builder(
                Component.literal("Bank"),
                    button -> {
                    if (Minecraft.getInstance().player != null) {
                        net.minecraft.client.Minecraft.getInstance().setScreen(new BankScreen());
                    }
                })
                .bounds(guiLeft + 38, guiTop + 40, 100, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Auction"),
                        button -> {
                            if (Minecraft.getInstance().player != null) {
                                net.minecraft.client.Minecraft.getInstance().setScreen(new AuctionScreen());
                            }
                        })
                .bounds(guiLeft + 38, guiTop + 70, 100, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("Shop"),
                        button -> {
                            if (Minecraft.getInstance().player != null) {
                                net.minecraft.client.Minecraft.getInstance().setScreen(new ShopScreen());
                            }
                        })
                .bounds(guiLeft + 38, guiTop + 100, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int guiLeft = (width - GUI_WIDTH) / 2;
        int guiTop = (height - GUI_HEIGHT) / 2;

        renderBackground(guiGraphics);
        guiGraphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xFF222222);
        guiGraphics.renderOutline(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF888888);

//        guiGraphics.drawString(font, "Balance: $" + ClientPlayerData.BALANCE, guiLeft + 10, guiTop + 20, 0xFFFFFF);
//        guiGraphics.drawString(font, "Income: $" + ClientPlayerData.INCOME + "/s", guiLeft + 10, guiTop + 32, 0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
