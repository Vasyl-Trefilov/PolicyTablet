package org.rusting.policytablet.world;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.rusting.policytablet.PlayerDataManager;
import org.rusting.policytablet.Policytablet;
import org.rusting.policytablet.client.Message;
import org.rusting.policytablet.network.ModMessages;
import org.rusting.policytablet.network.SyncAllZoneOwnersPacket;
import org.rusting.policytablet.network.SyncCaptureProgressPacket;
import org.rusting.policytablet.network.SyncZoneOwnerPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Policytablet.MODID)
public class GridTracker {

    private static final int CAPTURE_TICKS = 50;

    private static final Map<UUID, String> lastCells = new HashMap<>();
    private static final Map<UUID, Integer> throttles = new HashMap<>();
    private static final Map<UUID, CaptureProgress> captureProgress = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();

        int t = throttles.getOrDefault(uuid, 0);
        if (t > 0) {
            throttles.put(uuid, t - 1);
            return;
        }
        throttles.put(uuid, 5);

        String label = GridZone.getLabelAt(player.getX(), player.getZ());
        if (label == null) {
            handleCaptureProgress(player, uuid, null);
            return;
        }

        lastCells.put(uuid, label);
        handleCaptureProgress(player, uuid, label);
    }

    private static void handleCaptureProgress(ServerPlayer player, UUID uuid, String currentLabel) {
        CaptureProgress prog = captureProgress.get(uuid);

        if (currentLabel == null) {
            if (prog != null) {
                captureProgress.remove(uuid);
                sendCaptureReset(player);
            }
            return;
        }

        String owner = ZoneData.getOwner(currentLabel);
        String playerCountry = PlayerDataManager.get(player).getCountry();

        if (owner.equals(playerCountry)) {
            if (prog != null) {
                captureProgress.remove(uuid);
                sendCaptureReset(player);
            }
            return;
        }

        if (prog == null || !prog.cellLabel.equals(currentLabel)) {
            prog = new CaptureProgress(currentLabel, 0);
            captureProgress.put(uuid, prog);
        }

        prog.progress += 5;

        if (prog.progress >= CAPTURE_TICKS) {
            ZoneData.setOwner(currentLabel, playerCountry);
            captureProgress.remove(uuid);
            sendCaptureReset(player);

            ModMessages.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                    new SyncZoneOwnerPacket(currentLabel, playerCountry)
            );
            Message.send(playerCountry + "\u00a7a has Captured: " + currentLabel + "!", 200);
        } else {
            ModMessages.sendToPlayer(
                    new SyncCaptureProgressPacket(currentLabel, prog.progress),
                    player
            );
        }
    }

    private static void sendCaptureReset(ServerPlayer player) {
        ModMessages.sendToPlayer(
                new SyncCaptureProgressPacket("", 0),
                player
        );
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var owners = ZoneData.getAllOwners();
            if (!owners.isEmpty()) {
                ModMessages.sendToPlayer(new SyncAllZoneOwnersPacket(owners), player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        lastCells.remove(uuid);
        throttles.remove(uuid);
        captureProgress.remove(uuid);
    }

    private static class CaptureProgress {
        final String cellLabel;
        int progress;

        CaptureProgress(String cellLabel, int progress) {
            this.cellLabel = cellLabel;
            this.progress = progress;
        }
    }
}
