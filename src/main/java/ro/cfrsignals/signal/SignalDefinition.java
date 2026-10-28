package ro.cfrsignals.signal;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import ro.cfrsignals.config.CFRSignalConfiguration;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.config.SignalHeadAlignment;

/**
 * Data-driven visual metadata for one CFR signal variant.
 *
 * <p>Railway behavior, networking, and semaphore animation deliberately do not
 * live in this record.</p>
 */
public record SignalDefinition(
        ResourceLocation id,
        SignalFamily family,
        ResourceLocation head,
        ResourceLocation mast,
        List<CFRSignalAspect> lights,
        Set<Attachment> attachments,
        int defaultMastHeight,
        SignalHeadAlignment defaultAlignment,
        String translationKey
) {
    private static final int MAX_LIST_ENTRIES = 16;

    public SignalDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(family, "family");
        Objects.requireNonNull(head, "head");
        Objects.requireNonNull(mast, "mast");
        lights = List.copyOf(Objects.requireNonNull(lights, "lights"));
        attachments = Set.copyOf(Objects.requireNonNull(attachments, "attachments"));
        Objects.requireNonNull(defaultAlignment, "defaultAlignment");
        translationKey = Objects.requireNonNull(translationKey, "translationKey").strip();

        if (defaultMastHeight < CFRSignalConfiguration.MIN_MAST_HEIGHT
                || defaultMastHeight > CFRSignalConfiguration.MAX_MAST_HEIGHT) {
            throw new IllegalArgumentException("default_mast_height must be between "
                    + CFRSignalConfiguration.MIN_MAST_HEIGHT + " and "
                    + CFRSignalConfiguration.MAX_MAST_HEIGHT);
        }
        if (translationKey.isEmpty() || translationKey.length() > 128) {
            throw new IllegalArgumentException("translation_key must contain 1-128 characters");
        }
        validateLights(family, lights);
    }

    public boolean supports(CFRSignalAspect aspect) {
        return lights.contains(aspect);
    }

    public boolean supports(Attachment attachment) {
        return attachments.contains(attachment);
    }

    public static SignalDefinition parse(ResourceLocation resourceId, JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            throw new JsonParseException("definition root must be a JSON object");
        }

        JsonObject json = element.getAsJsonObject();
        if (json.has("id")) {
            ResourceLocation declaredId = parseResourceLocation(json, "id");
            if (!resourceId.equals(declaredId)) {
                throw new JsonParseException("declared id " + declaredId
                        + " does not match resource id " + resourceId);
            }
        }

        SignalFamily family = parseEnum(
                requiredString(json, "family"),
                SignalFamily.values(),
                SignalFamily::getSerializedName,
                "family"
        );
        ResourceLocation head = parseResourceLocation(json, "head");
        ResourceLocation mast = parseResourceLocation(json, "mast");
        List<CFRSignalAspect> lights = parseLights(requiredArray(json, "lights"));
        Set<Attachment> attachments = parseAttachments(requiredArray(json, "attachments"));
        int defaultMastHeight = requiredInt(json, "default_mast_height");
        SignalHeadAlignment alignment = parseEnum(
                requiredString(json, "default_alignment"),
                SignalHeadAlignment.values(),
                SignalHeadAlignment::getSerializedName,
                "default_alignment"
        );
        String translationKey = requiredString(json, "translation_key");

        try {
            return new SignalDefinition(
                    resourceId,
                    family,
                    head,
                    mast,
                    lights,
                    attachments,
                    defaultMastHeight,
                    alignment,
                    translationKey
            );
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException(exception.getMessage(), exception);
        }
    }

    private static List<CFRSignalAspect> parseLights(JsonArray array) {
        ensureReasonableSize(array, "lights");
        LinkedHashSet<CFRSignalAspect> parsed = new LinkedHashSet<>();
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("lights must contain only strings");
            }
            CFRSignalAspect aspect = parseEnum(
                    element.getAsString(),
                    CFRSignalAspect.values(),
                    CFRSignalAspect::getSerializedName,
                    "lights"
            );
            if (aspect == CFRSignalAspect.OFF) {
                throw new JsonParseException("OFF is not a physical lamp in a signal definition");
            }
            if (!parsed.add(aspect)) {
                throw new JsonParseException("duplicate light " + aspect.getSerializedName());
            }
        }
        return List.copyOf(parsed);
    }

    private static Set<Attachment> parseAttachments(JsonArray array) {
        if (array.size() > MAX_LIST_ENTRIES) {
            throw new JsonParseException("attachments must contain 0-" + MAX_LIST_ENTRIES + " entries");
        }
        LinkedHashSet<Attachment> parsed = new LinkedHashSet<>();
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("attachments must contain only strings");
            }
            Attachment attachment = parseEnum(
                    element.getAsString(),
                    Attachment.values(),
                    Attachment::serializedName,
                    "attachments"
            );
            if (!parsed.add(attachment)) {
                throw new JsonParseException("duplicate attachment " + attachment.serializedName());
            }
        }
        return Set.copyOf(parsed);
    }

    private static void validateLights(SignalFamily family, List<CFRSignalAspect> lights) {
        List<CFRSignalAspect> expected = family.supportsYellow()
                ? List.of(CFRSignalAspect.RED, CFRSignalAspect.YELLOW, CFRSignalAspect.GREEN)
                : List.of(CFRSignalAspect.RED, CFRSignalAspect.GREEN);
        if (!lights.equals(expected)) {
            throw new IllegalArgumentException("lights for " + family.getSerializedName()
                    + " must be " + expected.stream().map(CFRSignalAspect::getSerializedName).toList());
        }
    }

    private static ResourceLocation parseResourceLocation(JsonObject json, String key) {
        String value = requiredString(json, key);
        ResourceLocation parsed = ResourceLocation.tryParse(value);
        if (parsed == null) {
            throw new JsonParseException(key + " is not a valid resource location: " + value);
        }
        return parsed;
    }

    private static String requiredString(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new JsonParseException(key + " must be a string");
        }
        String value = element.getAsString().strip();
        if (value.isEmpty()) {
            throw new JsonParseException(key + " must not be empty");
        }
        return value;
    }

    private static int requiredInt(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException(key + " must be an integer");
        }
        try {
            return element.getAsBigDecimal().intValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new JsonParseException(key + " must be an integer", exception);
        }
    }

    private static JsonArray requiredArray(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonArray()) {
            throw new JsonParseException(key + " must be an array");
        }
        return element.getAsJsonArray();
    }

    private static void ensureReasonableSize(JsonArray array, String key) {
        if (array.isEmpty() || array.size() > MAX_LIST_ENTRIES) {
            throw new JsonParseException(key + " must contain 1-" + MAX_LIST_ENTRIES + " entries");
        }
    }

    private static <E> E parseEnum(
            String value,
            E[] values,
            java.util.function.Function<E, String> serializedName,
            String key
    ) {
        String normalized = value.toLowerCase(Locale.ROOT);
        for (E candidate : values) {
            if (serializedName.apply(candidate).equals(normalized)) {
                return candidate;
            }
        }
        throw new JsonParseException("unknown " + key + " value: " + value);
    }

    public enum Attachment {
        LADDER,
        IDENTIFICATION_PLATE,
        ELECTRICAL_BOX;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}
