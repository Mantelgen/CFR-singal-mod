package ro.cfrsignals.block;

import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.content.trains.signal.SignalBlockEntity.SignalState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import ro.cfrsignals.blockentity.CFRSignalBlockEntity;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.railway.CFRSignalAspectResolver.Resolution;
import ro.cfrsignals.railway.CFRSignalLinkSnapshot;
import ro.cfrsignals.network.CFRNetwork;
import ro.cfrsignals.registry.CFRBlockEntities;
import ro.cfrsignals.registry.CFRItems;
import ro.cfrsignals.signal.CFRSignalAspect;
import ro.cfrsignals.signal.CFRSignalAssembly;

public class CFRSignalBlock extends Block implements IBE<CFRSignalBlockEntity> {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CFRSignalBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public Class<CFRSignalBlockEntity> getBlockEntityClass() {
        return CFRSignalBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CFRSignalBlockEntity> getBlockEntityType() {
        return CFRBlockEntities.SIGNAL_CONTROLLER.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        BlockState above = context.getLevel().getBlockState(context.getClickedPos().above());
        Direction facing;
        if (above.is(ro.cfrsignals.registry.CFRBlocks.SIGNAL_BRIDGE_BEAM.get())) {
            facing = above.getValue(ro.cfrsignals.block.CFRSignalBridgeBeamBlock.FACING);
        } else if (below.is(ro.cfrsignals.registry.CFRBlocks.SIGNAL_BRIDGE_BEAM.get())) {
            facing = below.getValue(ro.cfrsignals.block.CFRSignalBridgeBeamBlock.FACING);
        } else if (below.is(ro.cfrsignals.registry.CFRBlocks.SIGNAL_POLE.get())) {
            facing = below.getValue(ro.cfrsignals.block.CFRSignalPoleBlock.FACING);
        } else {
            facing = context.getHorizontalDirection().getOpposite();
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        return onBlockEntityUse(level, pos, blockEntity -> {
            CFRSignalAssembly.Result assembly = blockEntity.getAssemblyResult();
            if (!assembly.complete()) {
                player.displayClientMessage(
                        Component.translatable(
                                "message.cfrsignals.incomplete_assembly",
                                Component.translatable(assembly.translationKey())
                        ),
                        true
                );
                return InteractionResult.SUCCESS;
            }
            if (player.isShiftKeyDown()) {
                player.displayClientMessage(
                        createLinkDebugMessage(
                                blockEntity.getLinkSnapshot(),
                                blockEntity.getSignalResolution()
                        ),
                        false
                );
                return InteractionResult.SUCCESS;
            }

            if (blockEntity.isCreateControlled() || blockEntity.hasConfiguredTrackTarget()) {
                player.displayClientMessage(
                        Component.translatable(
                                "message.cfrsignals.automatic_aspect",
                                Component.translatable(blockEntity.getAspect().getTranslationKey())
                        ),
                        true
                );
                return InteractionResult.SUCCESS;
            }

            var aspect = blockEntity.cycleDebugAspect();
            player.displayClientMessage(
                    Component.translatable(
                            "message.cfrsignals.debug_aspect",
                            Component.translatable(aspect.getTranslationKey())
                    ),
                    true
            );
            return InteractionResult.SUCCESS;
        });
    }

    private static Component createLinkDebugMessage(
            CFRSignalLinkSnapshot snapshot,
            Resolution resolution
    ) {
        SignalState createState = resolution.createState();
        CFRSignalAspect aspect = resolution.aspect();
        Component cfrAspect = Component.translatable(aspect.getTranslationKey());
        if (snapshot.status() == CFRSignalLinkSnapshot.Status.UNLINKED) {
            return Component.translatable("message.cfrsignals.debug_link.unlinked", cfrAspect);
        }

        String trackPosition = String.format(
                "%d %d %d",
                snapshot.trackPosition().getX(),
                snapshot.trackPosition().getY(),
                snapshot.trackPosition().getZ()
        );
        Component trackShape = Component.translatable(
                snapshot.curve()
                        ? "railway.cfrsignals.track_shape.curve"
                        : "railway.cfrsignals.track_shape.straight"
        );

        if (snapshot.status() == CFRSignalLinkSnapshot.Status.TRACK_MISSING) {
            return appendResolution(Component.translatable(
                    "message.cfrsignals.debug_link.missing",
                    trackPosition,
                    snapshot.targetDirection().name().toLowerCase(),
                    trackShape,
                    cfrAspect
            ), resolution);
        }

        if (snapshot.status() == CFRSignalLinkSnapshot.Status.TARGET_PENDING) {
            return appendResolution(Component.translatable(
                    "message.cfrsignals.debug_link.pending",
                    trackPosition,
                    snapshot.targetDirection().name().toLowerCase(),
                    trackShape,
                    Component.translatable(aspect.getTranslationKey())
            ), resolution);
        }

        return appendResolution(Component.translatable(
                "message.cfrsignals.debug_link.linked",
                trackPosition,
                snapshot.targetDirection().name().toLowerCase(),
                trackShape,
                snapshot.boundaryId().toString(),
                createState.name(),
                cfrAspect
        ), resolution);
    }

    private static Component appendResolution(Component link, Resolution resolution) {
        var message = Component.empty()
                .append(link)
                .append("\n")
                .append(Component.translatable(
                        "message.cfrsignals.debug_resolution",
                        Component.translatable(resolution.reason().getTranslationKey()),
                        resolution.followingSignals(),
                        resolution.restrictiveSignals()
                ));
        if (resolution.firstRestrictiveBoundary() != null) {
            message.append(Component.translatable(
                    "message.cfrsignals.debug_resolution.first_restrictive",
                    resolution.firstRestrictiveBoundary().toString(),
                    resolution.firstRestrictiveState().name()
            ));
        }
        return message;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (stack.is(CFRItems.SIGNAL_WRENCH.get())) {
            if (player instanceof ServerPlayer serverPlayer) {
                onBlockEntityUse(level, pos, blockEntity -> {
                    CFRNetwork.openConfiguration(serverPlayer, blockEntity);
                    return InteractionResult.SUCCESS;
                });
            }
            return ItemInteractionResult.SUCCESS;
        }

        if (!player.isShiftKeyDown()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(Items.YELLOW_DYE)) {
            if (!level.isClientSide) {
                onBlockEntityUse(level, pos, blockEntity -> {
                    SignalFamily family = blockEntity.toggleDebugFamily();
                    player.displayClientMessage(
                            Component.translatable(
                                    "message.cfrsignals.debug_family",
                                    Component.translatable(
                                            "family.cfrsignals." + family.getSerializedName()
                                    )
                            ),
                            true
                    );
                    return InteractionResult.SUCCESS;
                });
            }
            return ItemInteractionResult.SUCCESS;
        }

        DebugAttachment attachment = DebugAttachment.fromItem(stack);
        if (attachment == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide) {
            onBlockEntityUse(level, pos, blockEntity -> {
                boolean enabled = attachment.toggle(blockEntity);
                player.displayClientMessage(
                        Component.translatable(
                                "message.cfrsignals.debug_attachment",
                                Component.translatable(attachment.translationKey),
                                Component.translatable(
                                        enabled
                                                ? "state.cfrsignals.enabled"
                                                : "state.cfrsignals.disabled"
                                )
                        ),
                        true
                );
                return InteractionResult.SUCCESS;
            });
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, level, pos, newState);
    }

    private enum DebugAttachment {
        IDENTIFICATION_PLATE("attachment.cfrsignals.identification_plate") {
            @Override
            boolean toggle(CFRSignalBlockEntity blockEntity) {
                return blockEntity.toggleDebugIdentificationPlate();
            }
        };

        private final String translationKey;

        DebugAttachment(String translationKey) {
            this.translationKey = translationKey;
        }

        abstract boolean toggle(CFRSignalBlockEntity blockEntity);

        static DebugAttachment fromItem(ItemStack stack) {
            if (stack.is(Items.NAME_TAG)) {
                return IDENTIFICATION_PLATE;
            }
            return null;
        }
    }
}
