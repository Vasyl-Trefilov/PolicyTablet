package org.rusting.policytablet.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.rusting.policytablet.Policytablet;
import org.rusting.policytablet.world.GridZone;
import org.rusting.policytablet.world.ZoneData;

@Mod.EventBusSubscriber(modid = Policytablet.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PlayerDataOverlay {

    private static String lastZone = "";

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("player_data", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            int x = 5;
            int y = 5;

            guiGraphics.fill(x - 2, y - 2, x + 130, y + 68, 0x88000000);
            guiGraphics.drawString(mc.font, "Balance: $" + ClientPlayerData.BALANCE, x, y, 0xFFFFFF);
            guiGraphics.drawString(mc.font, "Income: $" + ClientPlayerData.INCOME + "/s", x, y + 10, 0xAAAAAA);

            String zoneLabel = GridZone.getLabelAt(mc.player.getX(), mc.player.getZ());
            String zoneOwner = zoneLabel != null ? ZoneData.getOwner(zoneLabel) : null;
            guiGraphics.drawString(mc.font, "Zone: " + (zoneLabel != null ? zoneLabel : "\u2014") + " [" + (zoneOwner != null ? zoneOwner : "\u2014") + "]", x, y + 21, zoneLabel != null ? 0x55FF55 : 0x666666);

            if (zoneLabel != null && !zoneLabel.equals(lastZone)) {
                lastZone = zoneLabel;
                Message.send("\u00a7aYou have entered " + zoneLabel, 200);
            }

            if (!ClientCaptureData.cellLabel.isEmpty() && ClientCaptureData.progress > 0) {
                int barWidth = 130;
                int barHeight = 4;
                int barX = x;
                int barY = y + 51;
                float fraction = (float) ClientCaptureData.progress / ClientCaptureData.MAX_PROGRESS;

                guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xAA444444);
                guiGraphics.fill(barX, barY, barX + (int) (barWidth * fraction), barY + barHeight, 0xAA00FF00);
                guiGraphics.drawString(mc.font, "Capturing " + ClientCaptureData.cellLabel, x, barY - 9, 0xFFFF55);
            }
        });

        event.registerAboveAll("flash_message", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
            String text = Message.getText();
            if (text.isEmpty()) return;

            int textWidth = Minecraft.getInstance().font.width(text);
            int y = screenHeight / 2 - 10;
            int x = 4;

            guiGraphics.fill(0, y - 2, x + textWidth + 4, y + 10, 0x88000000);
            guiGraphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFF);

            Message.tick();
        });
    }
}
