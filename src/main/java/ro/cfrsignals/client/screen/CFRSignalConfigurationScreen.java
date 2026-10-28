package ro.cfrsignals.client.screen;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import ro.cfrsignals.config.CFRSignalConfiguration;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.config.AuxiliaryIndicator;
import ro.cfrsignals.config.IdentificationPlatePosition;
import ro.cfrsignals.config.SignalFinish;
import ro.cfrsignals.config.SignalHeadAlignment;
import ro.cfrsignals.config.SignalMount;
import ro.cfrsignals.config.SignalOptionCompatibility;
import ro.cfrsignals.network.ApplySignalConfigurationPayload;
import ro.cfrsignals.network.OpenSignalConfigurationPayload;

public final class CFRSignalConfigurationScreen extends Screen {
    private static final int MAX_PANEL_WIDTH = 436;
    private static final int PANEL_HEIGHT = 232;
    private static final int PANEL_MARGIN = 12;
    private static final int COLUMN_GAP = 8;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_SPACING = 20;

    private final OpenSignalConfigurationPayload initial;
    private SignalFamily selectedFamily;
    private SignalHeadAlignment selectedHeadAlignment;
    private IdentificationPlatePosition selectedPlatePosition;
    private SignalMount selectedMount;
    private SignalFinish selectedFinish;
    private AuxiliaryIndicator selectedAuxiliaryIndicator;
    private boolean ladderEnabled;
    private boolean electricalBoxEnabled;
    private boolean identificationPlateEnabled;
    private String identificationPlateText;
    private CycleButton<SignalFamily> familyButton;
    private CycleButton<SignalHeadAlignment> headAlignmentButton;
    private CycleButton<SignalMount> mountButton;
    private CycleButton<SignalFinish> finishButton;
    private CycleButton<AuxiliaryIndicator> auxiliaryIndicatorButton;
    private CycleButton<Boolean> ladderButton;
    private CycleButton<Boolean> electricalBoxButton;
    private CycleButton<Boolean> identificationPlateButton;
    private CycleButton<IdentificationPlatePosition> identificationPlatePositionButton;
    private EditBox identificationText;

    private CFRSignalConfigurationScreen(OpenSignalConfigurationPayload initial) {
        super(Component.translatable("screen.cfrsignals.signal_configuration"));
        this.initial = initial;
        selectedFamily = SignalFamily.fromNetworkId(initial.familyId());
        selectedHeadAlignment = SignalHeadAlignment.fromNetworkId(initial.headAlignmentId());
        selectedPlatePosition = IdentificationPlatePosition.fromNetworkId(
                initial.identificationPlatePositionId()
        );
        selectedMount = SignalMount.fromNetworkId(initial.mountId());
        selectedFinish = SignalFinish.fromNetworkId(initial.finishId());
        selectedAuxiliaryIndicator = AuxiliaryIndicator.fromNetworkId(initial.auxiliaryIndicatorId());
        ladderEnabled = SignalOptionCompatibility.supportsLadder(selectedFamily, selectedMount);
        electricalBoxEnabled = SignalOptionCompatibility.supportsElectricalBox(selectedFamily, selectedMount);
        identificationPlateEnabled = initial.identificationPlateEnabled();
        identificationPlateText = initial.identificationText();
        normalizeDraft();
    }

    public static void open(OpenSignalConfigurationPayload payload) {
        if (!SignalFamily.isValidNetworkId(payload.familyId())
                || !SignalHeadAlignment.isValidNetworkId(payload.headAlignmentId())
                || !IdentificationPlatePosition.isValidNetworkId(payload.identificationPlatePositionId())
                || !SignalMount.isValidNetworkId(payload.mountId())
                || !SignalFinish.isValidNetworkId(payload.finishId())
                || !AuxiliaryIndicator.isValidNetworkId(payload.auxiliaryIndicatorId())
                || payload.cantileverSignalCount() < CFRSignalConfiguration.MIN_CANTILEVER_SIGNAL_COUNT
                || payload.cantileverSignalCount() > CFRSignalConfiguration.MAX_CANTILEVER_SIGNAL_COUNT
                || !CFRSignalConfiguration.isValidIdentificationText(payload.identificationText())) {
            return;
        }
        Minecraft.getInstance().setScreen(new CFRSignalConfigurationScreen(payload));
    }

    @Override
    protected void init() {
        int panelWidth = panelWidth();
        int left = (width - panelWidth) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        int columnWidth = (panelWidth - PANEL_MARGIN * 2 - COLUMN_GAP) / 2;
        int controlX = left + PANEL_MARGIN;
        int secondaryX = controlX + columnWidth + COLUMN_GAP;
        int controlY = top + 36;

        familyButton = addRenderableWidget(CycleButton
                .builder(CFRSignalConfigurationScreen::familyName)
                .withValues(List.of(SignalFamily.values()))
                .withInitialValue(selectedFamily)
                .create(
                        controlX,
                        controlY,
                        columnWidth,
                        ROW_HEIGHT,
                        Component.translatable("screen.cfrsignals.signal_configuration.head"),
                        (button, family) -> {
                            selectedFamily = family;
                            normalizeDraft();
                            rebuildWidgets();
                        }
                ));
        familyButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.cfrsignals.signal_configuration.head.tooltip"
        )));
        mountButton = addRenderableWidget(CycleButton
                .builder(CFRSignalConfigurationScreen::mountName)
                .withValues(SignalOptionCompatibility.allowedMounts(selectedFamily))
                .withInitialValue(selectedMount)
                .create(
                        controlX,
                        controlY + ROW_SPACING,
                        columnWidth,
                        ROW_HEIGHT,
                        Component.translatable("screen.cfrsignals.signal_configuration.mount"),
                        (button, mount) -> {
                            selectedMount = mount;
                            normalizeDraft();
                            rebuildWidgets();
                        }
                ));
        mountButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.cfrsignals.signal_configuration.mount.tooltip"
        )));
        finishButton = addRenderableWidget(CycleButton
                .builder(CFRSignalConfigurationScreen::finishName)
                .withValues(SignalOptionCompatibility.allowedFinishes(selectedFamily))
                .withInitialValue(selectedFinish)
                .create(
                        controlX,
                        controlY + ROW_SPACING * 2,
                        columnWidth,
                        ROW_HEIGHT,
                        Component.translatable("screen.cfrsignals.signal_configuration.finish"),
                        (button, finish) -> selectedFinish = finish
                ));
        finishButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.cfrsignals.signal_configuration.finish.tooltip"
        )));
        headAlignmentButton = addRenderableWidget(CycleButton
                .builder(CFRSignalConfigurationScreen::alignmentName)
                .withValues(List.of(SignalHeadAlignment.values()))
                .withInitialValue(selectedHeadAlignment)
                .create(
                        controlX,
                        controlY + ROW_SPACING * 3,
                        columnWidth,
                        ROW_HEIGHT,
                        Component.translatable("screen.cfrsignals.signal_configuration.head_alignment"),
                        (button, alignment) -> selectedHeadAlignment = alignment
                ));
        headAlignmentButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.cfrsignals.signal_configuration.head_alignment.tooltip"
        )));
        auxiliaryIndicatorButton = addRenderableWidget(CycleButton
                .builder(CFRSignalConfigurationScreen::indicatorName)
                .withValues(SignalOptionCompatibility.allowedIndicators(selectedFamily))
                .withInitialValue(selectedAuxiliaryIndicator)
                .create(
                        controlX,
                        controlY + ROW_SPACING * 4,
                        columnWidth,
                        ROW_HEIGHT,
                        Component.translatable("screen.cfrsignals.signal_configuration.auxiliary_indicator"),
                        (button, indicator) -> selectedAuxiliaryIndicator = indicator
                ));
        auxiliaryIndicatorButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.cfrsignals.signal_configuration.auxiliary_indicator.tooltip"
        )));
        ladderButton = addRenderableWidget(CycleButton.onOffBuilder(ladderEnabled).create(
                secondaryX,
                controlY,
                columnWidth,
                ROW_HEIGHT,
                Component.translatable("screen.cfrsignals.signal_configuration.ladder"),
                (button, enabled) -> ladderEnabled = enabled
        ));
        ladderButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.cfrsignals.signal_configuration.ladder.tooltip"
        )));
        electricalBoxButton = addRenderableWidget(CycleButton.onOffBuilder(electricalBoxEnabled).create(
                secondaryX,
                controlY + ROW_SPACING,
                columnWidth,
                ROW_HEIGHT,
                Component.translatable("screen.cfrsignals.signal_configuration.electrical_box"),
                (button, enabled) -> electricalBoxEnabled = enabled
        ));
        electricalBoxButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.cfrsignals.signal_configuration.electrical_box.tooltip"
        )));
        identificationPlateButton = addRenderableWidget(CycleButton.onOffBuilder(
                identificationPlateEnabled
        ).create(
                secondaryX,
                controlY + ROW_SPACING * 2,
                columnWidth,
                ROW_HEIGHT,
                Component.translatable("screen.cfrsignals.signal_configuration.identification_plate"),
                (button, enabled) -> {
                    identificationPlateEnabled = enabled;
                    updateControlState();
                }
        ));
        identificationPlateButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.cfrsignals.signal_configuration.identification_plate.tooltip"
        )));

        identificationPlatePositionButton = addRenderableWidget(CycleButton
                .builder(CFRSignalConfigurationScreen::platePositionName)
                .withValues(List.of(IdentificationPlatePosition.values()))
                .withInitialValue(selectedPlatePosition)
                .create(
                        secondaryX,
                        controlY + ROW_SPACING * 4,
                        columnWidth,
                        ROW_HEIGHT,
                        Component.translatable("screen.cfrsignals.signal_configuration.plate_position"),
                        (button, position) -> selectedPlatePosition = position
                ));
        identificationPlatePositionButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.cfrsignals.signal_configuration.plate_position.tooltip"
        )));

        identificationText = new EditBox(
                font,
                secondaryX,
                controlY + 115,
                columnWidth,
                ROW_HEIGHT,
                Component.translatable("screen.cfrsignals.signal_configuration.plate_text")
        );
        identificationText.setMaxLength(CFRSignalConfiguration.MAX_IDENTIFICATION_TEXT_LENGTH);
        identificationText.setFilter(CFRSignalConfiguration::isValidIdentificationText);
        identificationText.setValue(identificationPlateText);
        identificationText.setResponder(text -> identificationPlateText = text);
        identificationText.setTooltip(Tooltip.create(Component.translatable(
                "screen.cfrsignals.signal_configuration.plate_text.tooltip",
                CFRSignalConfiguration.MAX_IDENTIFICATION_TEXT_LENGTH
        )));
        addRenderableWidget(identificationText);

        int buttonWidth = Math.min(106, columnWidth);
        int buttonY = top + PANEL_HEIGHT - ROW_HEIGHT - 8;
        Button applyButton = addRenderableWidget(Button.builder(
                Component.translatable("screen.cfrsignals.signal_configuration.apply"),
                button -> applyChanges()
        ).bounds(controlX, buttonY, buttonWidth, ROW_HEIGHT).build());
        applyButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.cfrsignals.signal_configuration.apply.tooltip"
        )));
        Button cancelButton = addRenderableWidget(Button.builder(
                Component.translatable("screen.cfrsignals.signal_configuration.cancel"),
                button -> onClose()
        ).bounds(left + panelWidth - PANEL_MARGIN - buttonWidth, buttonY, buttonWidth, ROW_HEIGHT).build());
        cancelButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.cfrsignals.signal_configuration.cancel.tooltip"
        )));

        updateControlState();
        setInitialFocus(familyButton);
    }

    private void updateControlState() {
        if (headAlignmentButton != null) {
            boolean alignmentSupported = SignalOptionCompatibility.supportsHeadAlignment(
                    selectedFamily,
                    selectedMount
            );
            headAlignmentButton.active = alignmentSupported;
            headAlignmentButton.setTooltip(Tooltip.create(Component.translatable(
                    alignmentSupported
                            ? "screen.cfrsignals.signal_configuration.head_alignment.tooltip"
                            : "screen.cfrsignals.signal_configuration.head_alignment.unavailable_tooltip"
            )));
        }
        if (identificationPlatePositionButton != null) {
            identificationPlatePositionButton.active = identificationPlateEnabled
                    && SignalOptionCompatibility.supportsIdentificationPlate(selectedFamily);
        }
        if (identificationText != null) {
            identificationText.active = identificationPlateEnabled
                    && SignalOptionCompatibility.supportsIdentificationPlate(selectedFamily);
        }
        if (ladderButton != null) {
            ladderButton.active = false;
        }
        if (electricalBoxButton != null) {
            electricalBoxButton.active = false;
        }
        if (identificationPlateButton != null) {
            identificationPlateButton.active = SignalOptionCompatibility.supportsIdentificationPlate(selectedFamily);
        }
    }

    private void normalizeDraft() {
        selectedMount = SignalOptionCompatibility.normalizeMount(selectedFamily, selectedMount);
        selectedFinish = SignalOptionCompatibility.normalizeFinish(selectedFamily, selectedFinish);
        selectedAuxiliaryIndicator = SignalOptionCompatibility.normalizeIndicator(
                selectedFamily,
                selectedAuxiliaryIndicator
        );
        if (!SignalOptionCompatibility.supportsHeadAlignment(selectedFamily, selectedMount)) {
            selectedHeadAlignment = CFRSignalConfiguration.defaultHeadAlignment(selectedFamily);
        }
        ladderEnabled = SignalOptionCompatibility.supportsLadder(selectedFamily, selectedMount);
        electricalBoxEnabled = SignalOptionCompatibility.supportsElectricalBox(selectedFamily, selectedMount);
        if (!SignalOptionCompatibility.supportsIdentificationPlate(selectedFamily)) {
            identificationPlateEnabled = false;
        }
    }

    private void applyChanges() {
        PacketDistributor.sendToServer(new ApplySignalConfigurationPayload(
                initial.position(),
                selectedFamily,
                ladderEnabled,
                electricalBoxEnabled,
                identificationPlateEnabled,
                identificationPlateText,
                selectedHeadAlignment,
                selectedPlatePosition,
                selectedMount,
                selectedFinish,
                selectedAuxiliaryIndicator,
                CFRSignalConfiguration.DEFAULT_CANTILEVER_SIGNAL_COUNT
        ));
        onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            applyChanges();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int panelWidth = panelWidth();
        int columnWidth = (panelWidth - PANEL_MARGIN * 2 - COLUMN_GAP) / 2;
        int left = (width - panelWidth) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        int primaryX = left + PANEL_MARGIN;
        int secondaryX = primaryX + columnWidth + COLUMN_GAP;
        graphics.fill(left, top, left + panelWidth, top + PANEL_HEIGHT, 0xE0101010);
        graphics.renderOutline(left, top, panelWidth, PANEL_HEIGHT, 0xFF777777);
        graphics.drawCenteredString(font, title, width / 2, top + 10, 0xFFFFFF);
        drawSectionHeading(
                graphics,
                Component.translatable("screen.cfrsignals.signal_configuration.section.signal"),
                primaryX,
                top + 25,
                columnWidth
        );
        drawSectionHeading(
                graphics,
                Component.translatable("screen.cfrsignals.signal_configuration.section.attachments"),
                secondaryX,
                top + 25,
                columnWidth
        );
        drawSectionHeading(
                graphics,
                Component.translatable("screen.cfrsignals.signal_configuration.section.identification"),
                secondaryX,
                top + 105,
                columnWidth
        );
        graphics.drawString(
                font,
                Component.translatable("screen.cfrsignals.signal_configuration.plate_text"),
                secondaryX,
                top + 139,
                identificationPlateEnabled ? 0xFFFFFF : 0xFFA0A0A0,
                false
        );
        String textLength = identificationPlateText.length()
                + "/"
                + CFRSignalConfiguration.MAX_IDENTIFICATION_TEXT_LENGTH;
        graphics.drawString(
                font,
                textLength,
                secondaryX + columnWidth - font.width(textLength),
                top + 139,
                identificationPlateEnabled ? 0xFFB0B0B0 : 0xFF707070,
                false
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private int panelWidth() {
        return Math.min(MAX_PANEL_WIDTH, Math.max(1, width - 8));
    }

    private void drawSectionHeading(GuiGraphics graphics, Component heading, int x, int y, int sectionWidth) {
        graphics.drawString(font, heading, x, y, 0xFFD9B45C, false);
        int lineStart = x + font.width(heading) + 6;
        int lineEnd = x + sectionWidth;
        if (lineStart < lineEnd) {
            graphics.fill(lineStart, y + 4, lineEnd, y + 5, 0xFF6A5A38);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static Component familyName(SignalFamily family) {
        return Component.translatable("family.cfrsignals." + family.getSerializedName());
    }

    private static Component alignmentName(SignalHeadAlignment alignment) {
        return Component.translatable("alignment.cfrsignals." + alignment.getSerializedName());
    }

    private static Component platePositionName(IdentificationPlatePosition position) {
        return Component.translatable("plate_position.cfrsignals." + position.getSerializedName());
    }

    private static Component mountName(SignalMount mount) {
        return Component.translatable("mount.cfrsignals." + mount.getSerializedName());
    }

    private static Component finishName(SignalFinish finish) {
        return Component.translatable("finish.cfrsignals." + finish.getSerializedName());
    }

    private static Component indicatorName(AuxiliaryIndicator indicator) {
        return Component.translatable("indicator.cfrsignals." + indicator.getSerializedName());
    }
}
