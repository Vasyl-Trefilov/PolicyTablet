package org.rusting.policytablet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.rusting.policytablet.network.ModMessages;
import org.rusting.policytablet.network.SyncPlayerDataPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Policytablet.MODID)
public class PlayerDataManager {

    private static final Map<UUID, PlayerData> DATA = new HashMap<>();
    private static final int INCOME_INTERVAL = 200;
    private static int tickCounter = 0;

    public static PlayerData get(Player player) {
        return DATA.computeIfAbsent(player.getUUID(), k -> new PlayerData(0, 10, "Russland"));
    }

    public static void addBalance(Player player, int amount) {
        PlayerData data = get(player);
        data.addBalance(amount);
        syncToClient(player);
    }

    public static void setBalance(Player player, int amount) {
        PlayerData data = get(player);
        data.setBalance(amount);
        syncToClient(player);
    }

    public static void setIncome(Player player, int amount) {
        PlayerData data = get(player);
        data.setIncome(amount);
        syncToClient(player);
    }

    public static void addIncome(Player player, int amount) {
        PlayerData data = get(player);
        data.addIncome(amount);
        syncToClient(player);
    }

    public static void setCountry(Player player, String country) {
        PlayerData data = get(player);
        data.setCountry(country);
        syncToClient(player);
    }

    public static void syncToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerData data = get(player);
            ModMessages.sendToPlayer(
                    new SyncPlayerDataPacket(data.getBalance(), data.getIncome(), data.getCountry()),
                    serverPlayer
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            get(player);
            syncToClient(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player original = event.getOriginal();
            Player newPlayer = event.getEntity();
            PlayerData oldData = DATA.get(original.getUUID());
            if (oldData != null) {
                DATA.put(newPlayer.getUUID(), new PlayerData(oldData.getBalance(), oldData.getIncome(), "Russland"));
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < INCOME_INTERVAL) return;
        tickCounter = 0;

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerData data = get(player);
            if (data.getIncome() > 0) {
                data.addBalance(data.getIncome());
                syncToClient(player);
            }
        }
    }
}
