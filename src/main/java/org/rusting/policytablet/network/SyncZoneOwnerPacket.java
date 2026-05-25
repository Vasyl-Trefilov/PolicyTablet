package org.rusting.policytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.rusting.policytablet.world.ZoneData;

import java.util.function.Supplier;

public class SyncZoneOwnerPacket {
    private final String cellLabel;
    private final String country;

    public SyncZoneOwnerPacket(String cellLabel, String country) {
        this.cellLabel = cellLabel;
        this.country = country;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(cellLabel);
        buf.writeUtf(country);
    }

    public static SyncZoneOwnerPacket decode(FriendlyByteBuf buf) {
        return new SyncZoneOwnerPacket(buf.readUtf(), buf.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ZoneData.setOwner(cellLabel, country);

            if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ModMessages.INSTANCE.send(PacketDistributor.ALL.noArg(),
                        new SyncZoneOwnerPacket(cellLabel, country));
            }
        });
        context.setPacketHandled(true);
    }
}
