package org.rusting.policytablet.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.rusting.policytablet.client.ClientPlayerData;
import org.rusting.policytablet.client.Message;
import org.rusting.policytablet.network.BuyZonePacket;
import org.rusting.policytablet.network.ModMessages;
import org.rusting.policytablet.world.GridZone;
import org.rusting.policytablet.world.ZoneData;

public class AuctionScreen extends Screen {

    private static final int CELLS = 11;
    private static final int CELL_SIZE = 20;
    private static final int GUI_SIZE = CELLS * CELL_SIZE;

    private int guiLeft;
    private int guiTop;

    public AuctionScreen() {
        super(Component.literal("Auction"));
    }

    @Override
    protected void init() {
        guiLeft = (width - GUI_SIZE) / 2;
        guiTop = (height - GUI_SIZE) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.fill(guiLeft - 2, guiTop - 2, guiLeft + GUI_SIZE + 2, guiTop + GUI_SIZE + 30, 0xFF222222);
        guiGraphics.renderOutline(guiLeft - 2, guiTop - 2, GUI_SIZE + 4, GUI_SIZE + 32, 0xFF888888);

        guiGraphics.drawString(font, "Auction - $" + BuyZonePacket.PRICE + " per zone", guiLeft + 2, guiTop - 12, 0xFFFFFF);
        guiGraphics.drawString(font, "Balance: $" + ClientPlayerData.BALANCE, guiLeft + 2, guiTop + GUI_SIZE + 12, 0xFFFF55);

        for (int col = 0; col < CELLS; col++) {
            for (int row = 0; row < CELLS; row++) {
                String label = (col + 1) + "" + (char) ('A' + row);
                String owner = ZoneData.getOwner(label);
                int x = guiLeft + col * CELL_SIZE;
                int y = guiTop + row * CELL_SIZE;

                int color = switch (owner) {
                    case "neutral" -> 0xFF334433;
                    case "Russland" -> 0xFF003399;
                    case "USA" -> 0xFF224488;
                    case "Germany" -> 0xFF333333;
                    case "China" -> 0xFF992200;
                    default -> 0xFF444444;
                };

                guiGraphics.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, color);
                guiGraphics.renderOutline(x, y, CELL_SIZE, CELL_SIZE, 0xFF444444);

                if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                    guiGraphics.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0x44FFFFFF);
                    guiGraphics.renderOutline(x, y, CELL_SIZE, CELL_SIZE, 0xFFFFFFFF);
                }

                if (owner.equals("neutral")) {
                    String priceStr = "$" + BuyZonePacket.PRICE;
                    guiGraphics.drawString(font, priceStr, x + 1, y + CELL_SIZE - 10, 0x88FF88);
                } else {
                    guiGraphics.drawString(font, label, x + 1, y + 1, 0xFFFFFF);
                }
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int col = 0; col < CELLS; col++) {
                for (int row = 0; row < CELLS; row++) {
                    int x = guiLeft + col * CELL_SIZE;
                    int y = guiTop + row * CELL_SIZE;
                    if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                        String label = (col + 1) + "" + (char) ('A' + row);
                        String owner = ZoneData.getOwner(label);
                        if (owner.equals("neutral")) {
                            ModMessages.sendToServer(new BuyZonePacket(label));
                            Message.send("\u00a7aBuying " + label + "...", 60);
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
