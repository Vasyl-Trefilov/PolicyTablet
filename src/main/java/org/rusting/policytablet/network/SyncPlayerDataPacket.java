package org.rusting.policytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.rusting.policytablet.client.ClientPlayerData;

import java.util.function.Supplier;

public class SyncPlayerDataPacket {
    private final int balance;
    private final int income;
    private final String country;

    public SyncPlayerDataPacket(int balance, int income, String country) {
        this.balance = balance;
        this.income = income;
        this.country = country;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(balance);
        buf.writeInt(income);
        buf.writeUtf(country);
    }

    public static SyncPlayerDataPacket decode(FriendlyByteBuf buf) {
        return new SyncPlayerDataPacket(buf.readInt(), buf.readInt(), buf.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientPlayerData.BALANCE = balance;
            ClientPlayerData.INCOME = income;
            ClientPlayerData.COUNTRY = country;
        });
        context.setPacketHandled(true);
    }
}
