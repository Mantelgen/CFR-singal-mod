package ro.cfrsignals.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class CFRSignalWrenchItem extends Item {
    public CFRSignalWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.cfrsignals.signal_wrench.summary")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.cfrsignals.signal_wrench.details")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
