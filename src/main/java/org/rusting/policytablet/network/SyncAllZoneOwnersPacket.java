package org.rusting.policytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.rusting.policytablet.world.ZoneData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncAllZoneOwnersPacket {
    private final Map<String, String> owners;

    public SyncAllZoneOwnersPacket(Map<String, String> owners) {
        this.owners = owners;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(owners.size());
        for (var entry : owners.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeUtf(entry.getValue());
        }
    }

    public static SyncAllZoneOwnersPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<String, String> owners = new HashMap<>();
        for (int i = 0; i < size; i++) {
            owners.put(buf.readUtf(), buf.readUtf());
        }
        return new SyncAllZoneOwnersPacket(owners);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            for (var entry : owners.entrySet()) {
                ZoneData.setOwner(entry.getKey(), entry.getValue());
            }
        });
        context.setPacketHandled(true);
    }
}
