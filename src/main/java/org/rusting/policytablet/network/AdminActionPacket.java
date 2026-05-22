package org.rusting.policytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.rusting.policytablet.PlayerData;
import org.rusting.policytablet.PlayerDataManager;

import java.util.function.Supplier;

public class AdminActionPacket {
    public static final int ADD_INCOME = 0;
    public static final int ADD_BALANCE = 1;
    public static final int SET_INCOME = 2;
    public static final int SET_BALANCE = 3;
    public static final int SET_COUNTRY = 4;

    private final int action;
    private final int amount;

    public AdminActionPacket(int action, int amount) {
        this.action = action;
        this.amount = amount;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(action);
        buf.writeInt(amount);
    }

    public static AdminActionPacket decode(FriendlyByteBuf buf) {
        return new AdminActionPacket(buf.readInt(), buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player == null) return;

            switch (action) {
                case ADD_INCOME -> PlayerDataManager.addIncome(player, amount);
                case ADD_BALANCE -> PlayerDataManager.addBalance(player, amount);
                case SET_INCOME -> PlayerDataManager.setIncome(player, amount);
                case SET_BALANCE -> PlayerDataManager.setBalance(player, amount);
                case SET_COUNTRY -> PlayerDataManager.setCountry(player, PlayerData.countryByIndex(amount));
            }
        });
        context.setPacketHandled(true);
    }
}
