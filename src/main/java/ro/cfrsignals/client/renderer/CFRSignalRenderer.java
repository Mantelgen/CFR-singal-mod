package ro.cfrsignals.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import ro.cfrsignals.block.CFRSignalBlock;
import ro.cfrsignals.CFRSignals;
import ro.cfrsignals.blockentity.CFRSignalBlockEntity;
import ro.cfrsignals.client.model.CFRSignalModels;
import ro.cfrsignals.client.model.CFRSignalVisualLayout;
import ro.cfrsignals.config.CFRSignalConfiguration;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.config.AuxiliaryIndicator;
import ro.cfrsignals.config.SignalFinish;
import ro.cfrsignals.config.SignalHeadAlignment;
import ro.cfrsignals.config.SignalMount;
import ro.cfrsignals.signal.CFRSignalAspect;
import ro.cfrsignals.signal.CFRSemaphoreAnimation;

public final class CFRSignalRenderer implements BlockEntityRenderer<CFRSignalBlockEntity> {
    private static final RenderType MODEL_RENDER_TYPE = RenderType.cutout();
    private static final RenderType MODEL_VERTEX_RENDER_TYPE = RenderType.entityCutoutNoCull(
            InventoryMenu.BLOCK_ATLAS
    );
    private static final Set<ModelResourceLocation> REPORTED_MISSING_MODELS = ConcurrentHashMap.newKeySet();

    private final BlockRenderDispatcher blockRenderer;
    private final Font font;

    public CFRSignalRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
        font = context.getFont();
    }

    @Override
    public void render(
            CFRSignalBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Direction facing = blockEntity.getBlockState().getValue(CFRSignalBlock.FACING);
        CFRSignalAspect aspect = blockEntity.getAspect();
        CFRSignalConfiguration configuration = blockEntity.getConfiguration();

        poseStack.pushPose();
        rotateToFacing(poseStack, facing);
        if (configuration.mount() == SignalMount.WALL) {
            renderModel(
                    CFRSignalModels.WALL_BRACKET,
                    blockEntity,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
        } else if (configuration.mount() == SignalMount.CANTILEVER) {
            renderModel(
                    configuration.family() == SignalFamily.MECHANICAL_SEMAPHORE
                            ? CFRSignalModels.MECHANICAL_BRIDGE_PEDESTAL
                            : CFRSignalModels.OVERHEAD_HANGER,
                    blockEntity,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
        } else {
            // Match the original mast connection: the head overlaps the top pole by half a block.
            poseStack.translate(0.0D, -0.5D, 0.0D);
        }
        renderConfiguredHead(
                configuration, aspect, 0.0D, 0.0D, blockEntity, partialTick,
                poseStack, buffer, packedLight, packedOverlay
        );
        renderHeadIdentificationPlate(
                configuration, blockEntity, poseStack, buffer, packedLight, packedOverlay
        );

        poseStack.popPose();
    }

    private void renderConfiguredHead(
            CFRSignalConfiguration configuration,
            CFRSignalAspect aspect,
            double headX,
            double headY,
            CFRSignalBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();
        poseStack.translate(headX, headY, 0.0D);
        if (configuration.family() == SignalFamily.MECHANICAL_SEMAPHORE) {
            renderSemaphore(
                    blockEntity,
                    configuration,
                    partialTick,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
            poseStack.popPose();
            return;
        }

        if (configuration.mount() == SignalMount.MAST) {
            renderHeadMountingBracket(
                    configuration.headAlignment(), blockEntity, poseStack, buffer, packedLight, packedOverlay
            );
        }
        poseStack.translate(
                configuration.mount() == SignalMount.MAST
                        ? CFRSignalVisualLayout.headOffsetX(configuration.headAlignment())
                        : 0.0D,
                0.0D,
                CFRSignalVisualLayout.headForwardOffsetZ()
        );
        poseStack.translate(0.5D, 0.0D, 0.5D);
        float headScale = headScale(configuration);
        poseStack.scale(headScale, headScale, headScale);
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        renderFamilyHead(configuration.family(), aspect, blockEntity, poseStack, buffer, packedLight, packedOverlay);
        renderAuxiliaryIndicator(configuration.auxiliaryIndicator(), blockEntity, poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private static double cantileverHeadOffsetX(SignalHeadAlignment alignment) {
        double direction = alignment == SignalHeadAlignment.RIGHT ? 1.0D : -1.0D;
        return direction;
    }

    private void renderMountStructure(
            CFRSignalConfiguration configuration,
            int mastHeight,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        switch (configuration.mount()) {
            case DWARF -> renderModel(
                    CFRSignalModels.DWARF_BASE, blockEntity, poseStack, buffer, packedLight, packedOverlay
            );
            case MAST -> {
                renderModel(
                        CFRSignalModels.CONCRETE_BASE,
                        blockEntity,
                        poseStack,
                        buffer,
                        packedLight,
                        packedOverlay
                );
                renderMastColumn(
                        0.0D, configuration, mastHeight, blockEntity, poseStack, buffer, packedLight, packedOverlay
                );
            }
            case CANTILEVER -> {
                renderModel(
                        CFRSignalModels.CONCRETE_BASE,
                        blockEntity,
                        poseStack,
                        buffer,
                        packedLight,
                        packedOverlay
                );
                renderMastColumn(
                        0.0D, configuration, mastHeight, blockEntity, poseStack, buffer, packedLight, packedOverlay
                );
                int beamSegments = 3;
                double firstBeamX = configuration.headAlignment() == SignalHeadAlignment.RIGHT
                        ? 0.0D
                        : -beamSegments + 1.0D;
                renderBeam(
                        firstBeamX, beamSegments, mastHeight,
                        blockEntity, poseStack, buffer, packedLight, packedOverlay
                );
            }
            case WALL -> renderModel(
                    CFRSignalModels.WALL_BRACKET,
                    blockEntity,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
        }
    }

    private void renderMastColumn(
            double offsetX,
            CFRSignalConfiguration configuration,
            int mastHeight,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        boolean mechanical = configuration.family() == SignalFamily.MECHANICAL_SEMAPHORE;
        for (int segment = 0; segment < mastHeight; segment++) {
            ModelResourceLocation model;
            if (configuration.finish() == SignalFinish.MODERN_GRAY) {
                model = CFRSignalModels.MODERN_GRAY_MAST;
            } else if (mechanical) {
                model = segment % 2 == 0
                        ? CFRSignalModels.MECHANICAL_MAST_RED
                        : CFRSignalModels.MECHANICAL_MAST_WHITE;
            } else {
                model = segment % 2 == 0 ? CFRSignalModels.MAST_RED : CFRSignalModels.MAST_WHITE;
            }
            poseStack.pushPose();
            poseStack.translate(offsetX, 0.25D + segment, 0.0D);
            renderModel(model, blockEntity, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private void renderBeam(
            double firstX,
            int segments,
            int mastHeight,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        for (int segment = 0; segment < segments; segment++) {
            poseStack.pushPose();
            poseStack.translate(firstX + segment, mastHeight - 1.0D, 0.0D);
            renderModel(
                    CFRSignalModels.OVERHEAD_BEAM_SEGMENT,
                    blockEntity,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
            poseStack.popPose();
        }
    }

    private void renderAccessories(
            CFRSignalConfiguration configuration,
            int mastHeight,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        double supportX = 0.0D;
        if (configuration.mount().hasElevatedSupport() && configuration.ladderEnabled()) {
            for (int segment = 0; segment < mastHeight; segment++) {
                poseStack.pushPose();
                poseStack.translate(supportX, 0.25D + segment, 0.0D);
                renderModel(
                        configuration.family() == SignalFamily.MECHANICAL_SEMAPHORE
                                ? CFRSignalModels.MECHANICAL_LADDER_SEGMENT
                                : CFRSignalModels.LADDER_SEGMENT,
                        blockEntity,
                        poseStack,
                        buffer,
                        packedLight,
                        packedOverlay
                );
                poseStack.popPose();
            }
        }

        if (configuration.mount().hasElevatedSupport() && configuration.electricalBoxEnabled()) {
            poseStack.pushPose();
            poseStack.translate(supportX, 0.3D, 0.0D);
            renderModel(CFRSignalModels.ELECTRICAL_BOX, blockEntity, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }

        if (configuration.identificationPlateEnabled()) {
            poseStack.pushPose();
            double plateY = switch (configuration.mount()) {
                case DWARF -> 0.05D;
                case WALL -> -0.35D;
                case MAST, CANTILEVER -> CFRSignalVisualLayout.identificationPlateOffsetY(
                        configuration.family(), mastHeight, configuration.identificationPlatePosition()
                );
            };
            poseStack.translate(supportX, plateY, 0.0D);
            if (configuration.mount() == SignalMount.WALL) {
                renderModel(
                        CFRSignalModels.WALL_PLATE_BRACKET,
                        blockEntity,
                        poseStack,
                        buffer,
                        packedLight,
                        packedOverlay
                );
            }
            poseStack.translate(0.5D, 0.0D, 0.5D);
            float plateScale = configuration.mount() == SignalMount.DWARF
                    || configuration.mount() == SignalMount.WALL
                    ? 0.85F
                    : CFRSignalVisualLayout.identificationPlateScale();
            poseStack.scale(plateScale, plateScale, plateScale);
            poseStack.translate(-0.5D, 0.0D, -0.5D);
            renderModel(
                    CFRSignalModels.IDENTIFICATION_PLATE,
                    blockEntity,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
            if (shouldRenderIdentificationText(blockEntity)) {
                renderIdentificationText(configuration.identificationText(), poseStack, buffer, packedLight);
            }
            poseStack.popPose();
        }
    }

    private void renderHeadIdentificationPlate(
            CFRSignalConfiguration configuration,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        if (!configuration.identificationPlateEnabled()) {
            return;
        }
        poseStack.pushPose();
        boolean wallMounted = configuration.mount() == SignalMount.WALL;
        poseStack.translate(
                0.0D,
                CFRSignalVisualLayout.identificationPlateOffsetFromHead(
                        configuration.mount(),
                        configuration.identificationPlatePosition()
                ),
                0.0D
        );
        if (wallMounted) {
            renderModel(
                    CFRSignalModels.WALL_PLATE_BRACKET,
                    blockEntity,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
        }
        poseStack.translate(0.5D, 0.0D, 0.5D);
        float plateScale = switch (configuration.mount()) {
            case WALL -> 0.85F;
            case CANTILEVER -> CFRSignalVisualLayout.bridgeIdentificationPlateScale();
            default -> CFRSignalVisualLayout.identificationPlateScale();
        };
        poseStack.scale(plateScale, plateScale, plateScale);
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        renderModel(
                CFRSignalModels.IDENTIFICATION_PLATE,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        if (shouldRenderIdentificationText(blockEntity)) {
            renderIdentificationText(configuration.identificationText(), poseStack, buffer, packedLight);
        }
        poseStack.popPose();
    }

    private static float headScale(CFRSignalConfiguration configuration) {
        float scale = CFRSignalVisualLayout.modernHeadScale();
        if (configuration.family() == SignalFamily.SHUNTING
                || configuration.family() == SignalFamily.LEGACY_DWARF_ENTRY) {
            scale *= 0.82F;
        } else if (configuration.family() == SignalFamily.REPEATER
                || configuration.family() == SignalFamily.EMERGENCY) {
            scale *= 0.9F;
        }
        return scale;
    }

    private void renderFamilyHead(
            SignalFamily family,
            CFRSignalAspect aspect,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        switch (family) {
            case TWO_LIGHT -> renderTwoLightHead(
                    aspect, blockEntity, poseStack, buffer, packedLight, packedOverlay
            );
            case THREE_LIGHT, TMV -> renderThreeLightHead(
                    aspect, blockEntity, poseStack, buffer, packedLight, packedOverlay
            );
            case SHUNTING -> renderShuntingHead(
                    aspect, false, blockEntity, poseStack, buffer, packedLight, packedOverlay
            );
            case LEGACY_DWARF_ENTRY -> renderShuntingHead(
                    aspect, true, blockEntity, poseStack, buffer, packedLight, packedOverlay
            );
            case REPEATER -> renderRepeaterHead(
                    aspect, blockEntity, poseStack, buffer, packedLight, packedOverlay
            );
            case EMERGENCY -> renderEmergencyHead(
                    aspect, blockEntity, poseStack, buffer, packedLight, packedOverlay
            );
            case MECHANICAL_SEMAPHORE -> {
                // Rendered by the animated semaphore path before this switch.
            }
        }
    }

    private void renderTwoLightHead(
            CFRSignalAspect aspect,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        renderModel(CFRSignalModels.TWO_LIGHT_HEAD, blockEntity, poseStack, buffer, packedLight, packedOverlay);
        renderModel(CFRSignalModels.TWO_LIGHT_HOODS, blockEntity, poseStack, buffer, packedLight, packedOverlay);
        renderLamp(aspect == CFRSignalAspect.RED, CFRSignalModels.RED_LAMP_LIT,
                CFRSignalModels.RED_LAMP_DARK, blockEntity, poseStack, buffer, packedLight, packedOverlay);
        renderLamp(aspect == CFRSignalAspect.GREEN, CFRSignalModels.GREEN_LAMP_LIT,
                CFRSignalModels.GREEN_LAMP_DARK, blockEntity, poseStack, buffer, packedLight, packedOverlay);
    }

    private void renderThreeLightHead(
            CFRSignalAspect aspect,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        renderModel(CFRSignalModels.THREE_LIGHT_HEAD, blockEntity, poseStack, buffer, packedLight, packedOverlay);
        renderModel(CFRSignalModels.THREE_LIGHT_HOODS, blockEntity, poseStack, buffer, packedLight, packedOverlay);
        renderLamp(aspect == CFRSignalAspect.RED, CFRSignalModels.THREE_LIGHT_RED_LIT,
                CFRSignalModels.THREE_LIGHT_RED_DARK, blockEntity, poseStack, buffer, packedLight, packedOverlay);
        renderLamp(aspect == CFRSignalAspect.YELLOW, CFRSignalModels.THREE_LIGHT_YELLOW_LIT,
                CFRSignalModels.THREE_LIGHT_YELLOW_DARK, blockEntity, poseStack, buffer, packedLight, packedOverlay);
        renderLamp(aspect == CFRSignalAspect.GREEN, CFRSignalModels.THREE_LIGHT_GREEN_LIT,
                CFRSignalModels.THREE_LIGHT_GREEN_DARK, blockEntity, poseStack, buffer, packedLight, packedOverlay);
    }

    private void renderShuntingHead(
            CFRSignalAspect aspect,
            boolean legacyEntry,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        renderModel(CFRSignalModels.TWO_LIGHT_HEAD, blockEntity, poseStack, buffer, packedLight, packedOverlay);
        renderModel(CFRSignalModels.TWO_LIGHT_HOODS, blockEntity, poseStack, buffer, packedLight, packedOverlay);
        renderLamp(
                aspect == CFRSignalAspect.RED,
                legacyEntry ? CFRSignalModels.RED_LAMP_LIT : CFRSignalModels.SHUNTING_BLUE_LIT,
                legacyEntry ? CFRSignalModels.RED_LAMP_DARK : CFRSignalModels.SHUNTING_BLUE_DARK,
                blockEntity, poseStack, buffer, packedLight, packedOverlay
        );
        renderLamp(
                aspect == CFRSignalAspect.GREEN,
                CFRSignalModels.SHUNTING_LUNAR_LIT,
                CFRSignalModels.SHUNTING_LUNAR_DARK,
                blockEntity, poseStack, buffer, packedLight, packedOverlay
        );
    }

    private void renderRepeaterHead(
            CFRSignalAspect aspect,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        renderModel(CFRSignalModels.REPEATER_PANEL, blockEntity, poseStack, buffer, packedLight, packedOverlay);
        ModelResourceLocation bars = switch (aspect) {
            case RED -> CFRSignalModels.REPEATER_STOP;
            case YELLOW -> CFRSignalModels.REPEATER_CAUTION;
            case GREEN -> CFRSignalModels.REPEATER_CLEAR;
            case OFF -> null;
        };
        if (bars != null) {
            renderModel(bars, blockEntity, poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay);
        }
    }

    private void renderEmergencyHead(
            CFRSignalAspect aspect,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        renderModel(CFRSignalModels.EMERGENCY_DIAMOND, blockEntity, poseStack, buffer, packedLight, packedOverlay);
        renderLamp(aspect == CFRSignalAspect.RED, CFRSignalModels.EMERGENCY_LAMP_LIT,
                CFRSignalModels.EMERGENCY_LAMP_DARK, blockEntity, poseStack, buffer, packedLight, packedOverlay);
    }

    private void renderAuxiliaryIndicator(
            AuxiliaryIndicator indicator,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        if (!indicator.visible()) {
            return;
        }
        renderModel(
                CFRSignalModels.AUXILIARY_INDICATOR_PANEL,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        String text = indicator.displayText();
        float scale = text.length() > 1 ? 0.022F : 0.027F;
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.275D, 0.395D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(scale, -scale, scale);
        font.drawInBatch(
                text,
                -font.width(text) / 2.0F,
                -font.lineHeight / 2.0F,
                indicator.argbColor(),
                false,
                poseStack.last().pose(),
                buffer,
                Font.DisplayMode.POLYGON_OFFSET,
                0,
                LightTexture.FULL_BRIGHT
        );
        poseStack.popPose();
    }

    private void renderSemaphore(
            CFRSignalBlockEntity blockEntity,
            CFRSignalConfiguration configuration,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        float armAngle = blockEntity.getClientSemaphoreArmAngle(partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.75D, 0.5D);
        float semaphoreScale = CFRSignalVisualLayout.mechanicalSemaphoreScale();
        poseStack.scale(semaphoreScale, semaphoreScale, semaphoreScale);
        poseStack.translate(-0.5D, -0.75D, -0.5D);

        renderModel(
                CFRSignalModels.SEMAPHORE_MECHANISM_HOUSING,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        renderSemaphoreLamps(
                blockEntity,
                configuration,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.75D, 0.5D);
        applySemaphoreMounting(poseStack, configuration.headAlignment());
        poseStack.translate(-0.5D, -0.75D, -0.5D);
        poseStack.translate(0.4375D, 0.71875D, 0.546875D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(CFRSemaphoreAnimation.linkageAngle(armAngle)));
        poseStack.translate(-0.4375D, -0.71875D, -0.546875D);
        renderModel(
                CFRSignalModels.SEMAPHORE_LINKAGE,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.75D, 0.5D);
        applySemaphoreMounting(poseStack, configuration.headAlignment());
        poseStack.mulPose(Axis.ZP.rotationDegrees(armAngle));
        poseStack.translate(-0.5D, -0.75D, -0.5D);
        renderModel(
                CFRSignalModels.SEMAPHORE_ARM,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.0D);
        renderModel(
                CFRSignalModels.SEMAPHORE_ARM_EXTENSION,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        renderModel(
                CFRSignalModels.SEMAPHORE_ARM_END,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
        renderModel(
                CFRSignalModels.SEMAPHORE_COUNTERWEIGHT,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();

        renderModel(
                CFRSignalModels.SEMAPHORE_PIVOT,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    private void renderSemaphoreLamps(
            CFRSignalBlockEntity blockEntity,
            CFRSignalConfiguration configuration,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.75D, 0.5D);
        applySemaphoreMounting(poseStack, configuration.headAlignment());
        poseStack.translate(-0.5D, -0.75D, -0.5D);
        poseStack.translate(0.18D, -0.05D, 0.0D);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.scale(0.62F, 0.62F, 0.62F);
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        renderModel(
                CFRSignalModels.TWO_LIGHT_HEAD,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        renderModel(
                CFRSignalModels.TWO_LIGHT_HOODS,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        renderLamp(
                blockEntity.getAspect() == CFRSignalAspect.RED,
                CFRSignalModels.RED_LAMP_LIT,
                CFRSignalModels.RED_LAMP_DARK,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        renderLamp(
                blockEntity.getAspect() == CFRSignalAspect.GREEN,
                CFRSignalModels.GREEN_LAMP_LIT,
                CFRSignalModels.GREEN_LAMP_DARK,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    private static void applySemaphoreMounting(
            PoseStack poseStack,
            SignalHeadAlignment headAlignment
    ) {
        if (headAlignment == SignalHeadAlignment.LEFT) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        }
    }

    private void renderHeadMountingBracket(
            SignalHeadAlignment alignment,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        if (!CFRSignalVisualLayout.needsSideBracket(alignment)) {
            renderModel(
                    CFRSignalModels.HEAD_CENTER_MOUNT_BRACKET,
                    blockEntity,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
            return;
        }

        poseStack.pushPose();
        if (CFRSignalVisualLayout.mirrorSideBracket(alignment)) {
            poseStack.translate(0.5D, 0.0D, 0.0D);
            poseStack.scale(-1.0F, 1.0F, 1.0F);
            poseStack.translate(-0.5D, 0.0D, 0.0D);
        }
        renderModel(
                CFRSignalModels.HEAD_SIDE_MOUNT_BRACKET,
                blockEntity,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    private void renderIdentificationText(
            String text,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        if (text.isEmpty()) {
            return;
        }
        float textScale = CFRSignalVisualLayout.identificationTextScale(font.width(text));
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.105D, CFRSignalVisualLayout.identificationTextOffsetZ());
        poseStack.mulPose(Axis.YP.rotationDegrees(CFRSignalVisualLayout.identificationTextYRotation()));
        poseStack.scale(textScale, -textScale, textScale);
        font.drawInBatch(
                text,
                -font.width(text) / 2.0F,
                -font.lineHeight / 2.0F,
                0xFF181818,
                false,
                poseStack.last().pose(),
                buffer,
                Font.DisplayMode.POLYGON_OFFSET,
                0,
                packedLight
        );
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CFRSignalBlockEntity blockEntity) {
        // Mechanical arms and side-aligned heads extend outside the head block.
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    private static boolean shouldRenderIdentificationText(CFRSignalBlockEntity blockEntity) {
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BlockPos position = blockEntity.getBlockPos();
        double deltaX = camera.x - (position.getX() + 0.5D);
        double deltaY = camera.y - (position.getY() + 0.5D);
        double deltaZ = camera.z - (position.getZ() + 0.5D);
        return CFRSignalVisualLayout.shouldRenderIdentificationText(
                deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ
        );
    }

    private void renderLamp(
            boolean lit,
            ModelResourceLocation litModel,
            ModelResourceLocation darkModel,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        renderModel(
                lit ? litModel : darkModel,
                blockEntity,
                poseStack,
                buffer,
                lit ? LightTexture.FULL_BRIGHT : packedLight,
                packedOverlay
        );
    }

    private static void rotateToFacing(PoseStack poseStack, Direction facing) {
        float rotation = switch (facing) {
            case EAST -> -90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            default -> 0.0F;
        };
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
    }

    private void renderModel(
            ModelResourceLocation location,
            CFRSignalBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        BakedModel model = modelManager.getModel(location);
        if (model == modelManager.getMissingModel()) {
            if (REPORTED_MISSING_MODELS.add(location)) {
                CFRSignals.LOGGER.error(
                        "CFR signal component {} was not baked; skipping the missing-model cube",
                        location
                );
            }
            return;
        }
        VertexConsumer vertices = buffer.getBuffer(MODEL_VERTEX_RENDER_TYPE);
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                vertices,
                blockEntity.getBlockState(),
                model,
                1.0F,
                1.0F,
                1.0F,
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                MODEL_RENDER_TYPE
        );
    }
}
