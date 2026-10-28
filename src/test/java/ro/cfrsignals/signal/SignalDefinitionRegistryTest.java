package ro.cfrsignals.signal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Map;

import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import ro.cfrsignals.config.CFRSignalConfiguration.SignalFamily;
import ro.cfrsignals.config.SignalHeadAlignment;
import ro.cfrsignals.signal.SignalDefinition.Attachment;

class SignalDefinitionRegistryTest {
    private static final ResourceLocation CUSTOM_ID = ResourceLocation.fromNamespaceAndPath(
            "example",
            "two_light_test"
    );

    @Test
    void parsesValidatedDefinitionMetadata() {
        SignalDefinition definition = SignalDefinition.parse(CUSTOM_ID, JsonParser.parseString(validDefinition()));

        assertEquals(CUSTOM_ID, definition.id());
        assertEquals(SignalFamily.TWO_LIGHT, definition.family());
        assertEquals(SignalHeadAlignment.CENTER, definition.defaultAlignment());
        assertEquals(7, definition.defaultMastHeight());
        assertEquals(
                java.util.List.of(CFRSignalAspect.RED, CFRSignalAspect.GREEN),
                definition.lights()
        );
        assertTrue(definition.supports(Attachment.LADDER));
        assertFalse(definition.supports(CFRSignalAspect.YELLOW));
    }

    @Test
    void rejectsDeclaredIdThatDoesNotMatchResource() {
        String json = validDefinition().replace(
                "\"id\": \"example:two_light_test\"",
                "\"id\": \"example:wrong\""
        );

        assertThrows(
                com.google.gson.JsonParseException.class,
                () -> SignalDefinition.parse(CUSTOM_ID, JsonParser.parseString(json))
        );
    }

    @Test
    void rejectsFractionalMastHeight() {
        String json = validDefinition().replace(
                "\"default_mast_height\": 7",
                "\"default_mast_height\": 7.5"
        );

        assertThrows(
                com.google.gson.JsonParseException.class,
                () -> SignalDefinition.parse(CUSTOM_ID, JsonParser.parseString(json))
        );
    }

    @Test
    void invalidCanonicalResourceRetainsSafeFallback() {
        ResourceLocation canonical = ResourceLocation.fromNamespaceAndPath(
                "cfrsignals",
                "two_light_classic"
        );
        ArrayList<String> errors = new ArrayList<>();

        Map<ResourceLocation, SignalDefinition> loaded = SignalDefinitionRegistry.loadDefinitions(
                Map.of(canonical, JsonParser.parseString(validDefinition().replace(
                        "[\"red\", \"green\"]",
                        "[\"green\", \"red\"]"
                ))),
                (id, message) -> errors.add(id + ": " + message)
        );

        assertEquals(SignalFamily.TWO_LIGHT, loaded.get(canonical).family());
        assertEquals(
                java.util.List.of(CFRSignalAspect.RED, CFRSignalAspect.GREEN),
                loaded.get(canonical).lights()
        );
        assertEquals(1, errors.size());
    }

    @Test
    void validCustomDefinitionLoadsAlongsideAllFallbacks() {
        Map<ResourceLocation, SignalDefinition> loaded = SignalDefinitionRegistry.loadDefinitions(
                Map.of(CUSTOM_ID, JsonParser.parseString(validDefinition())),
                (id, message) -> {
                    throw new AssertionError(message);
                }
        );

        assertEquals(SignalFamily.values().length + 1, loaded.size());
        assertTrue(loaded.containsKey(CUSTOM_ID));
    }

    @Test
    void emptyAttachmentListIsValid() {
        String definition = validDefinition().replace(
                "\"attachments\": [\"ladder\", \"identification_plate\"]",
                "\"attachments\": []"
        );
        Map<ResourceLocation, SignalDefinition> loaded = SignalDefinitionRegistry.loadDefinitions(
                Map.of(CUSTOM_ID, JsonParser.parseString(definition)),
                (id, message) -> {
                    throw new AssertionError(message);
                }
        );

        assertTrue(loaded.get(CUSTOM_ID).attachments().isEmpty());
    }

    private static String validDefinition() {
        return """
                {
                  "id": "example:two_light_test",
                  "family": "two_light",
                  "head": "cfrsignals:block/head/two_light_backplate",
                  "mast": "cfrsignals:block/mast/concrete_red_segment",
                  "lights": ["red", "green"],
                  "attachments": ["ladder", "identification_plate"],
                  "default_mast_height": 7,
                  "default_alignment": "center",
                  "translation_key": "definition.example.two_light_test"
                }
                """;
    }
}
