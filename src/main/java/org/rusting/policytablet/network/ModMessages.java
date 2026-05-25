package org.rusting.policytablet.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.rusting.policytablet.Policytablet;

public class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Policytablet.MODID, "main"),
            () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
    );

    public static void register() {
        INSTANCE.registerMessage(id++, SyncPlayerDataPacket.class,
                SyncPlayerDataPacket::encode, SyncPlayerDataPacket::decode, SyncPlayerDataPacket::handle);
        INSTANCE.registerMessage(id++, AdminActionPacket.class,
                AdminActionPacket::encode, AdminActionPacket::decode, AdminActionPacket::handle);
        INSTANCE.registerMessage(id++, SyncZoneOwnerPacket.class,
                SyncZoneOwnerPacket::encode, SyncZoneOwnerPacket::decode, SyncZoneOwnerPacket::handle);
        INSTANCE.registerMessage(id++, SyncAllZoneOwnersPacket.class,
                SyncAllZoneOwnersPacket::encode, SyncAllZoneOwnersPacket::decode, SyncAllZoneOwnersPacket::handle);
        INSTANCE.registerMessage(id++, BuyZonePacket.class,
                BuyZonePacket::encode, BuyZonePacket::decode, BuyZonePacket::handle);
        INSTANCE.registerMessage(id++, SyncCaptureProgressPacket.class,
                SyncCaptureProgressPacket::encode, SyncCaptureProgressPacket::decode, SyncCaptureProgressPacket::handle);
    }

    public static void sendToPlayer(SyncPlayerDataPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(AdminActionPacket packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToServer(SyncZoneOwnerPacket packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToServer(BuyZonePacket packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToPlayer(SyncZoneOwnerPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToPlayer(SyncAllZoneOwnersPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToPlayer(SyncCaptureProgressPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
