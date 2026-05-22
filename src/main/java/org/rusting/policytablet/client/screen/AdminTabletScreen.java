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

public class AdminTabletScreen extends Screen {

    private static final int GUI_WIDTH = 180;
    private static final int GUI_HEIGHT = 240;

    private EditBox amountField;

    public AdminTabletScreen() {
        super(Component.literal("AdminTablet"));
    }

    @Override
    protected void init() {
        int guiLeft = (width - GUI_WIDTH) / 2;
        int guiTop = (height - GUI_HEIGHT) / 2;

        amountField = new EditBox(font, guiLeft + 10, guiTop + 50, 100, 16, Component.literal("Amount"));
        amountField.setFilter(s -> s.matches("\\d*"));
        amountField.setValue("10");
        addRenderableWidget(amountField);

        addRenderableWidget(Button.builder(
                Component.literal("Add to Income"),
                button -> send(AdminActionPacket.ADD_INCOME))
                .bounds(guiLeft + 10, guiTop + 72, 100, 20)
                .build());

        addRenderableWidget(Button.builder(
                Component.literal("Set Income"),
                button -> send(AdminActionPacket.SET_INCOME))
                .bounds(guiLeft + 10, guiTop + 94, 100, 20)
                .build());

        addRenderableWidget(Button.builder(
                Component.literal("Add to Balance"),
                button -> send(AdminActionPacket.ADD_BALANCE))
                .bounds(guiLeft + 10, guiTop + 116, 100, 20)
                .build());

        addRenderableWidget(Button.builder(
                Component.literal("Set Balance"),
                button -> send(AdminActionPacket.SET_BALANCE))
                .bounds(guiLeft + 10, guiTop + 138, 100, 20)
                .build());

        addRenderableWidget(Button.builder(
                Component.literal("Switch Country"),
                button -> {
                    int next = PlayerData.indexOfCountry(ClientPlayerData.COUNTRY) + 1;
                    ModMessages.sendToServer(new AdminActionPacket(AdminActionPacket.SET_COUNTRY, next));
                })
                .bounds(guiLeft + 10, guiTop + 162, 100, 20)
                .build());

        addRenderableWidget(Button.builder(
                Component.literal("Open Map"),
                button -> {
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().setScreen(new MapScreen());
                    }
                })
                .bounds(guiLeft + 10, guiTop + 186, 100, 20)
                .build());
    }

    private void send(int action) {
        String text = amountField.getValue();
        if (text.isEmpty()) return;
        int amount = Integer.parseInt(text);
        ModMessages.sendToServer(new AdminActionPacket(action, amount));
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
        guiGraphics.drawString(font, "Country: " + ClientPlayerData.COUNTRY, guiLeft + 10, guiTop + 210, 0x55FF55);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
