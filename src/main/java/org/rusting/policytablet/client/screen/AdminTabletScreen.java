package org.rusting.policytablet.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.rusting.policytablet.PlayerData;
import org.rusting.policytablet.client.ClientPlayerData;
import org.rusting.policytablet.network.AdminActionPacket;
import org.rusting.policytablet.network.ModMessages;
import org.rusting.policytablet.network.SyncZoneOwnerPacket;

public class AdminTabletScreen extends Screen {

    private static final int GUI_WIDTH = 200;
    private static final int GUI_HEIGHT = 230;

    private EditBox amountField;
    private EditBox zoneField;

    public AdminTabletScreen() {
        super(Component.literal("AdminTablet"));
    }

    @Override
    protected void init() {
        int guiLeft = (width - GUI_WIDTH) / 2;
        int guiTop = (height - GUI_HEIGHT) / 2;
        int left = guiLeft + 10;

        amountField = new EditBox(font, left, guiTop + 50, 80, 16, Component.literal("Amount"));
        amountField.setFilter(s -> s.matches("\\d*"));
        amountField.setValue("10");
        addRenderableWidget(amountField);

        zoneField = new EditBox(font, left + 94, guiTop + 50, 50, 16, Component.literal("Zone"));
        zoneField.setValue("1A");
        addRenderableWidget(zoneField);

        addRenderableWidget(Button.builder(
                Component.literal("+Income"), button -> send(AdminActionPacket.ADD_INCOME))
                .bounds(left, guiTop + 72, 60, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("=Income"), button -> send(AdminActionPacket.SET_INCOME))
                .bounds(left + 64, guiTop + 72, 60, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("+Balance"), button -> send(AdminActionPacket.ADD_BALANCE))
                .bounds(left + 128, guiTop + 72, 60, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("=Balance"), button -> send(AdminActionPacket.SET_BALANCE))
                .bounds(left, guiTop + 94, 60, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Switch Country"),
                button -> {
                    int next = PlayerData.indexOfCountry(ClientPlayerData.COUNTRY) + 1;
                    ModMessages.sendToServer(new AdminActionPacket(AdminActionPacket.SET_COUNTRY, next));
                })
                .bounds(left, guiTop + 118, 130, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Set Zone \u2192 " + ClientPlayerData.COUNTRY),
                button -> {
                    String label = zoneField.getValue();
                    if (!label.isEmpty()) {
                        ModMessages.sendToServer(new SyncZoneOwnerPacket(label, ClientPlayerData.COUNTRY));
                    }
                })
                .bounds(left, guiTop + 142, 130, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Set Neutral"),
                button -> {
                    String label = zoneField.getValue();
                    if (!label.isEmpty()) {
                        ModMessages.sendToServer(new SyncZoneOwnerPacket(label, "neutral"));
                    }
                })
                .bounds(left + 134, guiTop + 142, 56, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("Open Map"),
                button -> Minecraft.getInstance().setScreen(new MapScreen()))
                .bounds(left, guiTop + 166, 100, 20).build());
    }

    private void send(int action) {
        String text = amountField.getValue();
        if (text.isEmpty()) return;
        ModMessages.sendToServer(new AdminActionPacket(action, Integer.parseInt(text)));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int guiLeft = (width - GUI_WIDTH) / 2;
        int guiTop = (height - GUI_HEIGHT) / 2;

        renderBackground(guiGraphics);
        guiGraphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xFF222222);
        guiGraphics.renderOutline(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF888888);

        guiGraphics.drawString(font, "Balance: $" + ClientPlayerData.BALANCE, guiLeft + 10, guiTop + 20, 0xFFFFFF);
        guiGraphics.drawString(font, "Income: $" + ClientPlayerData.INCOME + "/s", guiLeft + 10, guiTop + 32, 0xAAAAAA);
        guiGraphics.drawString(font, "Country: " + ClientPlayerData.COUNTRY, guiLeft + 10, guiTop + 196, 0x55FF55);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
