package ro.cfrsignals.client.model;

import java.util.List;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import ro.cfrsignals.CFRSignals;

public final class CFRSignalModels {
    public static final ModelResourceLocation MAST_RED = standalone("block/mast/concrete_red_segment");
    public static final ModelResourceLocation MAST_WHITE = standalone("block/mast/concrete_white_segment");
    public static final ModelResourceLocation MECHANICAL_MAST_RED = standalone("block/mast/mechanical_red_segment");
    public static final ModelResourceLocation MECHANICAL_MAST_WHITE = standalone("block/mast/mechanical_white_segment");
    public static final ModelResourceLocation MODERN_GRAY_MAST = standalone("block/mast/modern_gray_segment");
    public static final ModelResourceLocation CONCRETE_BASE = standalone("block/mast/concrete_base");
    public static final ModelResourceLocation DWARF_BASE = standalone("block/mount/dwarf_base");
    public static final ModelResourceLocation OVERHEAD_BEAM_SEGMENT = standalone("block/mount/overhead_beam_segment");
    public static final ModelResourceLocation OVERHEAD_HANGER = standalone("block/mount/overhead_hanger");
    public static final ModelResourceLocation MECHANICAL_BRIDGE_PEDESTAL = standalone(
            "block/mount/mechanical_bridge_pedestal"
    );
    public static final ModelResourceLocation WALL_BRACKET = standalone("block/mount/wall_bracket");
    public static final ModelResourceLocation WALL_PLATE_BRACKET = standalone("block/mount/wall_plate_bracket");
    public static final ModelResourceLocation TWO_LIGHT_HEAD = standalone("block/head/two_light_backplate");
    public static final ModelResourceLocation TWO_LIGHT_HOODS = standalone("block/head/two_light_hoods");
    public static final ModelResourceLocation RED_LAMP_DARK = standalone("block/head/lamp_red_dark");
    public static final ModelResourceLocation RED_LAMP_LIT = standalone("block/head/lamp_red_lit");
    public static final ModelResourceLocation GREEN_LAMP_DARK = standalone("block/head/lamp_green_dark");
    public static final ModelResourceLocation GREEN_LAMP_LIT = standalone("block/head/lamp_green_lit");
    public static final ModelResourceLocation THREE_LIGHT_HEAD = standalone("block/head/three_light_backplate");
    public static final ModelResourceLocation THREE_LIGHT_HOODS = standalone("block/head/three_light_hoods");
    public static final ModelResourceLocation THREE_LIGHT_RED_DARK = standalone("block/head/three_light_lamp_red_dark");
    public static final ModelResourceLocation THREE_LIGHT_RED_LIT = standalone("block/head/three_light_lamp_red_lit");
    public static final ModelResourceLocation THREE_LIGHT_YELLOW_DARK = standalone("block/head/three_light_lamp_yellow_dark");
    public static final ModelResourceLocation THREE_LIGHT_YELLOW_LIT = standalone("block/head/three_light_lamp_yellow_lit");
    public static final ModelResourceLocation THREE_LIGHT_GREEN_DARK = standalone("block/head/three_light_lamp_green_dark");
    public static final ModelResourceLocation THREE_LIGHT_GREEN_LIT = standalone("block/head/three_light_lamp_green_lit");
    public static final ModelResourceLocation SHUNTING_BLUE_DARK = standalone("block/head/shunting_blue_dark");
    public static final ModelResourceLocation SHUNTING_BLUE_LIT = standalone("block/head/shunting_blue_lit");
    public static final ModelResourceLocation SHUNTING_LUNAR_DARK = standalone("block/head/shunting_lunar_dark");
    public static final ModelResourceLocation SHUNTING_LUNAR_LIT = standalone("block/head/shunting_lunar_lit");
    public static final ModelResourceLocation REPEATER_PANEL = standalone("block/head/repeater_panel");
    public static final ModelResourceLocation REPEATER_STOP = standalone("block/head/repeater_bar_stop");
    public static final ModelResourceLocation REPEATER_CAUTION = standalone("block/head/repeater_bar_caution");
    public static final ModelResourceLocation REPEATER_CLEAR = standalone("block/head/repeater_bar_clear");
    public static final ModelResourceLocation EMERGENCY_DIAMOND = standalone("block/head/emergency_diamond");
    public static final ModelResourceLocation EMERGENCY_LAMP_DARK = standalone("block/head/emergency_lamp_dark");
    public static final ModelResourceLocation EMERGENCY_LAMP_LIT = standalone("block/head/emergency_lamp_lit");
    public static final ModelResourceLocation AUXILIARY_INDICATOR_PANEL = standalone("block/head/auxiliary_indicator_panel");
    public static final ModelResourceLocation HEAD_SIDE_MOUNT_BRACKET = standalone("block/head/side_mount_bracket");
    public static final ModelResourceLocation HEAD_CENTER_MOUNT_BRACKET = standalone("block/head/center_mount_bracket");
    public static final ModelResourceLocation LADDER_SEGMENT = standalone("block/attachments/ladder_segment");
    public static final ModelResourceLocation MECHANICAL_LADDER_SEGMENT = standalone("block/mechanical/ladder_segment");
    public static final ModelResourceLocation ELECTRICAL_BOX = standalone("block/attachments/electrical_box");
    public static final ModelResourceLocation IDENTIFICATION_PLATE = standalone("block/attachments/identification_plate");
    public static final ModelResourceLocation SEMAPHORE_ARM = standalone("block/mechanical/semaphore_arm");
    public static final ModelResourceLocation SEMAPHORE_ARM_EXTENSION = standalone("block/mechanical/semaphore_arm_extension");
    public static final ModelResourceLocation SEMAPHORE_ARM_END = standalone("block/mechanical/semaphore_arm_end");
    public static final ModelResourceLocation SEMAPHORE_PIVOT = standalone("block/mechanical/semaphore_pivot");
    public static final ModelResourceLocation SEMAPHORE_LINKAGE = standalone("block/mechanical/semaphore_linkage");
    public static final ModelResourceLocation SEMAPHORE_COUNTERWEIGHT = standalone("block/mechanical/semaphore_counterweight");
    public static final ModelResourceLocation SEMAPHORE_MECHANISM_HOUSING = standalone("block/mechanical/semaphore_mechanism_housing");

    private static final List<ModelResourceLocation> STANDALONE_MODELS = List.of(
            MAST_RED,
            MAST_WHITE,
            MECHANICAL_MAST_RED,
            MECHANICAL_MAST_WHITE,
            MODERN_GRAY_MAST,
            CONCRETE_BASE,
            DWARF_BASE,
            OVERHEAD_BEAM_SEGMENT,
            OVERHEAD_HANGER,
            MECHANICAL_BRIDGE_PEDESTAL,
            WALL_BRACKET,
            WALL_PLATE_BRACKET,
            TWO_LIGHT_HEAD,
            TWO_LIGHT_HOODS,
            RED_LAMP_DARK,
            RED_LAMP_LIT,
            GREEN_LAMP_DARK,
            GREEN_LAMP_LIT,
            THREE_LIGHT_HEAD,
            THREE_LIGHT_HOODS,
            THREE_LIGHT_RED_DARK,
            THREE_LIGHT_RED_LIT,
            THREE_LIGHT_YELLOW_DARK,
            THREE_LIGHT_YELLOW_LIT,
            THREE_LIGHT_GREEN_DARK,
            THREE_LIGHT_GREEN_LIT,
            SHUNTING_BLUE_DARK,
            SHUNTING_BLUE_LIT,
            SHUNTING_LUNAR_DARK,
            SHUNTING_LUNAR_LIT,
            REPEATER_PANEL,
            REPEATER_STOP,
            REPEATER_CAUTION,
            REPEATER_CLEAR,
            EMERGENCY_DIAMOND,
            EMERGENCY_LAMP_DARK,
            EMERGENCY_LAMP_LIT,
            AUXILIARY_INDICATOR_PANEL,
            HEAD_SIDE_MOUNT_BRACKET,
            HEAD_CENTER_MOUNT_BRACKET,
            LADDER_SEGMENT,
            MECHANICAL_LADDER_SEGMENT,
            ELECTRICAL_BOX,
            IDENTIFICATION_PLATE,
            SEMAPHORE_ARM,
            SEMAPHORE_ARM_EXTENSION,
            SEMAPHORE_ARM_END,
            SEMAPHORE_PIVOT,
            SEMAPHORE_LINKAGE,
            SEMAPHORE_COUNTERWEIGHT,
            SEMAPHORE_MECHANISM_HOUSING
    );

    private CFRSignalModels() {
    }

    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        STANDALONE_MODELS.forEach(event::register);
    }

    private static ModelResourceLocation standalone(String path) {
        return ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(CFRSignals.MOD_ID, path)
        );
    }
}
