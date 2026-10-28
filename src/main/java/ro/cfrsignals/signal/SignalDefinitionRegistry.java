package ro.cfrsignals.signal;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import ro.cfrsignals.CFRSignals;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.config.SignalHeadAlignment;
import ro.cfrsignals.signal.SignalDefinition.Attachment;

public final class SignalDefinitionRegistry extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "cfr_signal_definitions";

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Map<SignalFamily, ResourceLocation> DEFAULT_IDS = createDefaultIds();
    private static final Map<ResourceLocation, SignalDefinition> FALLBACKS = createFallbacks();
    public static final SignalDefinitionRegistry INSTANCE = new SignalDefinitionRegistry();

    private volatile Map<ResourceLocation, SignalDefinition> definitions = FALLBACKS;

    private SignalDefinitionRegistry() {
        super(GSON, DIRECTORY);
    }

    public Optional<SignalDefinition> find(ResourceLocation id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public SignalDefinition defaultFor(SignalFamily family) {
        SignalFamily safeFamily = family == null ? SignalFamily.TWO_LIGHT : family;
        return definitions.getOrDefault(DEFAULT_IDS.get(safeFamily), FALLBACKS.get(DEFAULT_IDS.get(safeFamily)));
    }

    public Map<ResourceLocation, SignalDefinition> snapshot() {
        return definitions;
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resources,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        Map<ResourceLocation, SignalDefinition> loaded = loadDefinitions(
                resources,
                (id, message) -> CFRSignals.LOGGER.warn("Skipping signal definition {}: {}", id, message)
        );
        definitions = loaded;
        CFRSignals.LOGGER.info(
                "Loaded {} CFR signal definitions ({} built-in fallbacks available)",
                loaded.size(),
                FALLBACKS.size()
        );
    }

    static Map<ResourceLocation, SignalDefinition> loadDefinitions(
            Map<ResourceLocation, JsonElement> resources,
            BiConsumer<ResourceLocation, String> errorReporter
    ) {
        Map<ResourceLocation, SignalDefinition> loaded = new LinkedHashMap<>(FALLBACKS);
        resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    try {
                        loaded.put(entry.getKey(), SignalDefinition.parse(entry.getKey(), entry.getValue()));
                    } catch (RuntimeException exception) {
                        String message = exception.getMessage() == null
                                ? exception.getClass().getSimpleName()
                                : exception.getMessage();
                        errorReporter.accept(entry.getKey(), message);
                    }
                });
        return Map.copyOf(loaded);
    }

    private static Map<SignalFamily, ResourceLocation> createDefaultIds() {
        Map<SignalFamily, ResourceLocation> ids = new EnumMap<>(SignalFamily.class);
        ids.put(SignalFamily.TWO_LIGHT, id("two_light_classic"));
        ids.put(SignalFamily.THREE_LIGHT, id("three_light_classic"));
        ids.put(SignalFamily.SHUNTING, id("shunting_classic"));
        ids.put(SignalFamily.REPEATER, id("repeater_classic"));
        ids.put(SignalFamily.TMV, id("tmv_modular"));
        ids.put(SignalFamily.EMERGENCY, id("emergency_diamond"));
        ids.put(SignalFamily.LEGACY_DWARF_ENTRY, id("legacy_dwarf_entry"));
        ids.put(SignalFamily.MECHANICAL_SEMAPHORE, id("mechanical_semaphore_classic"));
        return Map.copyOf(ids);
    }

    private static Map<ResourceLocation, SignalDefinition> createFallbacks() {
        Set<Attachment> attachments = Set.of(
                Attachment.LADDER,
                Attachment.IDENTIFICATION_PLATE,
                Attachment.ELECTRICAL_BOX
        );
        Set<Attachment> mechanicalAttachments = Set.of(
                Attachment.LADDER,
                Attachment.IDENTIFICATION_PLATE
        );
        Map<ResourceLocation, SignalDefinition> fallbacks = new LinkedHashMap<>();
        addFallback(
                fallbacks,
                SignalFamily.TWO_LIGHT,
                "block/head/two_light_backplate",
                List.of(CFRSignalAspect.RED, CFRSignalAspect.GREEN),
                attachments,
                SignalHeadAlignment.CENTER
        );
        addFallback(
                fallbacks,
                SignalFamily.THREE_LIGHT,
                "block/head/three_light_backplate",
                List.of(CFRSignalAspect.RED, CFRSignalAspect.YELLOW, CFRSignalAspect.GREEN),
                attachments,
                SignalHeadAlignment.CENTER
        );
        addFallback(fallbacks, SignalFamily.SHUNTING, "block/head/shunting_backplate",
                List.of(CFRSignalAspect.RED, CFRSignalAspect.GREEN), attachments, SignalHeadAlignment.CENTER);
        addFallback(fallbacks, SignalFamily.REPEATER, "block/head/repeater_panel",
                List.of(CFRSignalAspect.RED, CFRSignalAspect.YELLOW, CFRSignalAspect.GREEN), attachments,
                SignalHeadAlignment.CENTER);
        addFallback(fallbacks, SignalFamily.TMV, "block/head/three_light_backplate",
                List.of(CFRSignalAspect.RED, CFRSignalAspect.YELLOW, CFRSignalAspect.GREEN), attachments,
                SignalHeadAlignment.CENTER);
        addFallback(fallbacks, SignalFamily.EMERGENCY, "block/head/emergency_diamond",
                List.of(CFRSignalAspect.RED, CFRSignalAspect.GREEN), Set.of(), SignalHeadAlignment.CENTER);
        addFallback(fallbacks, SignalFamily.LEGACY_DWARF_ENTRY, "block/head/shunting_backplate",
                List.of(CFRSignalAspect.RED, CFRSignalAspect.GREEN), Set.of(Attachment.IDENTIFICATION_PLATE),
                SignalHeadAlignment.CENTER);
        addFallback(
                fallbacks,
                SignalFamily.MECHANICAL_SEMAPHORE,
                "block/mechanical/semaphore_mechanism_housing",
                List.of(CFRSignalAspect.RED, CFRSignalAspect.GREEN),
                mechanicalAttachments,
                SignalHeadAlignment.RIGHT
        );
        return Map.copyOf(fallbacks);
    }

    private static void addFallback(
            Map<ResourceLocation, SignalDefinition> fallbacks,
            SignalFamily family,
            String headPath,
            List<CFRSignalAspect> lights,
            Set<Attachment> attachments,
            SignalHeadAlignment alignment
    ) {
        ResourceLocation definitionId = DEFAULT_IDS.get(family);
        fallbacks.put(definitionId, new SignalDefinition(
                definitionId,
                family,
                id(headPath),
                id("block/mast/concrete_red_segment"),
                lights,
                attachments,
                7,
                alignment,
                "definition.cfrsignals." + definitionId.getPath()
        ));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CFRSignals.MOD_ID, path);
    }
}
