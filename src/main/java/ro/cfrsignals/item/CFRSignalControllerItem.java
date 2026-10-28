package ro.cfrsignals.item;

import java.util.List;

import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.track.TrackTargetingBlockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class CFRSignalControllerItem extends TrackTargetingBlockItem {
    public CFRSignalControllerItem(Block block, Item.Properties properties) {
        super(block, properties, EdgePointType.SIGNAL);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.cfrsignals.signal_controller.summary")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.cfrsignals.signal_controller.configure")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
