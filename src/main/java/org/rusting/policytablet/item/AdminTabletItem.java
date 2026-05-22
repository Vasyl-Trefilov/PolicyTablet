package org.rusting.policytablet.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.rusting.policytablet.client.screen.AdminTabletScreen;

public class AdminTabletItem extends Item {

    public AdminTabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (level.isClientSide) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new AdminTabletScreen());
        }

        return InteractionResultHolder.pass(itemStack);
    }
}
