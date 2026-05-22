package org.rusting.policytablet.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.rusting.policytablet.Policytablet;

@Mod.EventBusSubscriber(modid = Policytablet.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PlayerDataOverlay {

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("player_data", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            int x = 5;
            int y = 5;

            guiGraphics.fill(x - 2, y - 2, x + 100, y + 24, 0x88000000);
            guiGraphics.drawString(mc.font, "Balance: $" + ClientPlayerData.BALANCE, x, y, 0xFFFFFF);
            guiGraphics.drawString(mc.font, "Income: $" + ClientPlayerData.INCOME + "/s", x, y + 10, 0xAAAAAA);
        });
    }
}
