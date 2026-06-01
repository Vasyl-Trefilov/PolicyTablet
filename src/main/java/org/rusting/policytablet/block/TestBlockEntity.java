package org.rusting.policytablet.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.rusting.policytablet.Policytablet;

public class TestBlockEntity extends BlockEntity {
    public TestBlockEntity(BlockPos pos, BlockState state) {
        super(Policytablet.TEST_BLOCK_ENTITY.get(), pos, state);
    }
}
