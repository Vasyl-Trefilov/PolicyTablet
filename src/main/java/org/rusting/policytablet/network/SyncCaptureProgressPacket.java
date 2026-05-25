package org.rusting.policytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.rusting.policytablet.client.ClientCaptureData;

import java.util.function.Supplier;

public class SyncCaptureProgressPacket {
    private final String cellLabel;
    private final int progress;

    public SyncCaptureProgressPacket(String cellLabel, int progress) {
        this.cellLabel = cellLabel;
        this.progress = progress;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(cellLabel);
        buf.writeInt(progress);
    }

    public static SyncCaptureProgressPacket decode(FriendlyByteBuf buf) {
        return new SyncCaptureProgressPacket(buf.readUtf(), buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientCaptureData.cellLabel = cellLabel;
            ClientCaptureData.progress = progress;
        });
        context.setPacketHandled(true);
    }
}
