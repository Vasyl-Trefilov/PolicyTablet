package org.rusting.policytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.rusting.policytablet.PlayerDataManager;
import org.rusting.policytablet.world.ZoneData;

import java.util.function.Supplier;

public class BuyZonePacket {
    public static final int PRICE = 10;

    private final String cellLabel;

    public BuyZonePacket(String cellLabel) {
        this.cellLabel = cellLabel;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(cellLabel);
    }

    public static BuyZonePacket decode(FriendlyByteBuf buf) {
        return new BuyZonePacket(buf.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) return;

            if (!ZoneData.getOwner(cellLabel).equals("neutral")) return;

            var data = PlayerDataManager.get(player);
            if (data.getBalance() < PRICE) return;

            data.addBalance(-PRICE);
            String country = data.getCountry();
            ZoneData.setOwner(cellLabel, country);
            PlayerDataManager.syncToClient(player);

            ModMessages.INSTANCE.send(
                    PacketDistributor.ALL.noArg(),
                    new SyncZoneOwnerPacket(cellLabel, country)
            );
        });
        context.setPacketHandled(true);
    }
}
