package ro.cfrsignals.client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

final class LocalizationIntegrityTest {
    private static final Path LANG_ROOT = Path.of(
            "src", "main", "resources", "assets", "cfrsignals", "lang"
    );

    @Test
    void romanianLocalizationMatchesEveryEnglishKey() throws IOException {
        JsonObject english = readLanguage("en_us.json");
        JsonObject romanian = readLanguage("ro_ro.json");

        assertEquals(Set.copyOf(english.keySet()), Set.copyOf(romanian.keySet()));
        for (String key : english.keySet()) {
            assertFalse(english.get(key).getAsString().isBlank(), key + " is blank in en_us");
            assertFalse(romanian.get(key).getAsString().isBlank(), key + " is blank in ro_ro");
        }
    }

    private static JsonObject readLanguage(String fileName) throws IOException {
        try (Reader reader = Files.newBufferedReader(LANG_ROOT.resolve(fileName), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
