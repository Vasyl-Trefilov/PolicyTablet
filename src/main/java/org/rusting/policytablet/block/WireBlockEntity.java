package org.rusting.policytablet.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.rusting.policytablet.Policytablet;

public class WireBlockEntity extends BlockEntity {
    public WireBlockEntity(BlockPos pos, BlockState state) {
        super(Policytablet.WIRE_BLOCK_ENTITY.get(), pos, state);
    }
}
