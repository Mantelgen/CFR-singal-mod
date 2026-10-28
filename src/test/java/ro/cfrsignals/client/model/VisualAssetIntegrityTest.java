package ro.cfrsignals.client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

final class VisualAssetIntegrityTest {
    private static final Path ASSET_ROOT = Path.of(
            "src", "main", "resources", "assets", "cfrsignals"
    );
    private static final Path MODEL_ROOT = ASSET_ROOT.resolve("models/block");
    private static final Path TEXTURE_ROOT = ASSET_ROOT.resolve("textures");
    private static final Pattern STANDALONE_MODEL = Pattern.compile("standalone\\(\"([^\"]+)\"\\)");

    @Test
    void everyRegisteredModelResolvesItsParentAndEveryFaceTexture() throws IOException {
        Path registrySource = Path.of(
                "src", "main", "java", "ro", "cfrsignals", "client", "model", "CFRSignalModels.java"
        );
        var matcher = STANDALONE_MODEL.matcher(Files.readString(registrySource, StandardCharsets.UTF_8));
        while (matcher.find()) {
            String modelId = matcher.group(1);
            Path modelPath = ASSET_ROOT.resolve("models").resolve(modelId + ".json");
            assertTrue(Files.isRegularFile(modelPath), () -> "Missing registered model " + modelPath);
            ResolvedModel model = resolveModel(modelPath, new HashSet<>());
            assertNotNull(model.elements(), () -> modelPath + " has no inherited or local elements");
            for (JsonElement value : model.elements()) {
                JsonObject faces = value.getAsJsonObject().getAsJsonObject("faces");
                for (var face : faces.entrySet()) {
                    String reference = face.getValue().getAsJsonObject().get("texture").getAsString();
                    String resolved = resolveTextureReference(model.textures(), reference, new HashSet<>());
                    assertFalse(resolved.startsWith("#"), () -> modelPath + " has unresolved " + reference);
                    if (resolved.startsWith("cfrsignals:")) {
                        Path texturePath = TEXTURE_ROOT.resolve(
                                resolved.substring("cfrsignals:".length()) + ".png"
                        );
                        assertTrue(Files.isRegularFile(texturePath), () -> modelPath + " misses " + texturePath);
                    }
                }
            }
        }
    }

    @Test
    void modelElementsStayWithinMinecraftBoundsAndReferenceExistingTextures() throws IOException {
        for (Path modelPath : filesWithExtension(MODEL_ROOT, ".json")) {
            JsonObject model = readJson(modelPath);
            JsonObject textures = model.getAsJsonObject("textures");
            if (textures != null) {
                for (var texture : textures.entrySet()) {
                    String reference = texture.getValue().getAsString();
                    if (!reference.startsWith("cfrsignals:")) {
                        continue;
                    }
                    String relativePath = reference.substring("cfrsignals:".length()) + ".png";
                    assertTrue(
                            Files.isRegularFile(TEXTURE_ROOT.resolve(relativePath)),
                            () -> modelPath + " references missing texture " + reference
                    );
                }
            }

            JsonArray elements = model.getAsJsonArray("elements");
            if (elements == null) {
                continue;
            }
            for (JsonElement value : elements) {
                JsonObject element = value.getAsJsonObject();
                JsonArray from = element.getAsJsonArray("from");
                JsonArray to = element.getAsJsonArray("to");
                assertEquals(3, from.size(), modelPath.toString());
                assertEquals(3, to.size(), modelPath.toString());
                for (int axis = 0; axis < 3; axis++) {
                    double minimum = from.get(axis).getAsDouble();
                    double maximum = to.get(axis).getAsDouble();
                    assertTrue(minimum >= -16.0D, () -> modelPath + " extends below -16");
                    assertTrue(maximum <= 32.0D, () -> modelPath + " extends above 32");
                    assertTrue(maximum > minimum, () -> modelPath + " has a zero-size element");
                }
            }
        }
    }

    @Test
    void standaloneGeometryCannotBeDirectionallyCulled() throws IOException {
        for (Path modelPath : filesWithExtension(MODEL_ROOT, ".json")) {
            String json = Files.readString(modelPath, StandardCharsets.UTF_8);
            assertFalse(json.contains("\"cullface\""), () -> modelPath + " declares cullface");
        }
    }

    @Test
    void texturesDecodeAndContainNoMissingTextureMagenta() throws IOException {
        for (Path texturePath : filesWithExtension(TEXTURE_ROOT.resolve("block"), ".png")) {
            BufferedImage image = ImageIO.read(texturePath.toFile());
            assertNotNull(image, () -> "Could not decode " + texturePath);
            assertTrue(isPowerOfTwo(image.getWidth()), () -> texturePath + " width is not power-of-two");
            assertTrue(isPowerOfTwo(image.getHeight()), () -> texturePath + " height is not power-of-two");
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = argb >>> 24;
                    int red = argb >>> 16 & 0xFF;
                    int green = argb >>> 8 & 0xFF;
                    int blue = argb & 0xFF;
                    boolean magentaFamily = alpha > 0
                            && red > green + 24
                            && blue > green + 24
                            && Math.abs(red - blue) < 64;
                    assertFalse(magentaFamily, () -> texturePath + " contains purple/magenta pixels");
                }
            }
        }
    }

    @Test
    void blackSignalSurfacesAreStrictlyNeutralGrayscale() throws IOException {
        List<Path> blackTextures = List.of(
                TEXTURE_ROOT.resolve("block/signal/backplate.png"),
                TEXTURE_ROOT.resolve("block/signal/hood.png"),
                TEXTURE_ROOT.resolve("block/mechanical/blackened_housing.png")
        );
        for (Path texturePath : blackTextures) {
            BufferedImage image = ImageIO.read(texturePath.toFile());
            assertNotNull(image, () -> "Could not decode " + texturePath);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int red = argb >>> 16 & 0xFF;
                    int green = argb >>> 8 & 0xFF;
                    int blue = argb & 0xFF;
                    assertTrue(
                            red == green && green == blue,
                            () -> texturePath + " contains a colored undertone"
                    );
                }
            }
        }
    }

    @Test
    void mechanicalModelsUseDedicatedMechanicalMaterials() throws IOException {
        List<Path> mechanicalModels = filesWithExtension(MODEL_ROOT.resolve("mechanical"), ".json");
        mechanicalModels = Stream.concat(
                mechanicalModels.stream(),
                Stream.of(
                        MODEL_ROOT.resolve("mast/mechanical_red_segment.json"),
                        MODEL_ROOT.resolve("mast/mechanical_white_segment.json")
                )
        ).toList();

        for (Path modelPath : mechanicalModels) {
            JsonObject textures = readJson(modelPath).getAsJsonObject("textures");
            assertNotNull(textures, () -> modelPath + " has no explicit mechanical textures");
            for (var texture : textures.entrySet()) {
                assertTrue(
                        texture.getValue().getAsString().startsWith("cfrsignals:block/mechanical/"),
                        () -> modelPath + " uses generic material " + texture.getValue()
                );
            }
        }
    }

    @Test
    void outOfBoundsSignalComponentsCannotSampleOutsideTheirTextureSprites() throws IOException {
        List<Path> modelPaths = List.of(
                MODEL_ROOT.resolve("head/three_light_backplate.json"),
                MODEL_ROOT.resolve("head/auxiliary_indicator_panel.json"),
                MODEL_ROOT.resolve("mechanical/semaphore_mechanism_housing.json"),
                MODEL_ROOT.resolve("mount/overhead_hanger.json"),
                MODEL_ROOT.resolve("mount/mechanical_bridge_pedestal.json")
        );
        for (Path modelPath : modelPaths) {
            JsonObject model = readJson(modelPath);
            for (JsonElement value : model.getAsJsonArray("elements")) {
                JsonObject element = value.getAsJsonObject();
                JsonArray from = element.getAsJsonArray("from");
                JsonArray to = element.getAsJsonArray("to");
                boolean outsideVanillaBounds = false;
                for (int axis = 0; axis < 3; axis++) {
                    outsideVanillaBounds |= from.get(axis).getAsDouble() < 0.0D;
                    outsideVanillaBounds |= to.get(axis).getAsDouble() > 16.0D;
                }
                if (!outsideVanillaBounds) {
                    continue;
                }
                JsonObject faces = element.getAsJsonObject("faces");
                for (var face : faces.entrySet()) {
                    assertTrue(
                            face.getValue().getAsJsonObject().has("uv"),
                            () -> modelPath + " has out-of-bounds geometry with implicit UVs"
                    );
                }
            }
        }
    }

    @Test
    void signalHousingsKeepEnoughDepthForVeryCloseObliqueViews() throws IOException {
        List<Path> housingModels = List.of(
                MODEL_ROOT.resolve("head/two_light_backplate.json"),
                MODEL_ROOT.resolve("head/three_light_backplate.json"),
                MODEL_ROOT.resolve("mechanical/semaphore_mechanism_housing.json")
        );
        for (Path modelPath : housingModels) {
            JsonObject model = readJson(modelPath);
            double front = coordinate(model, 0, "from", 2);
            double rear = coordinate(model, 0, "to", 2);
            assertTrue(
                    rear - front >= 4.0D,
                    () -> modelPath + " is too thin for stable close-angle rendering"
            );
        }
    }

    @Test
    void modularSemaphoreArmHasNoSegmentGap() throws IOException {
        JsonObject root = readJson(MODEL_ROOT.resolve("mechanical/semaphore_arm.json"));
        JsonObject extension = readJson(MODEL_ROOT.resolve("mechanical/semaphore_arm_extension.json"));
        JsonObject end = readJson(MODEL_ROOT.resolve("mechanical/semaphore_arm_end.json"));

        double rootBodyEnd = coordinate(root, 0, "to", 0);
        double extensionBodyStart = coordinate(extension, 0, "from", 0) + 8.0D;
        double extensionBodyEnd = coordinate(extension, 0, "to", 0) + 8.0D;
        double roundEndStart = minimumCoordinate(end, "from", 0) + 8.0D;

        assertEquals(rootBodyEnd, extensionBodyStart, 0.000001D);
        assertTrue(roundEndStart <= extensionBodyEnd, "Round end must overlap the arm extension");
    }

    @Test
    void identificationPlateFaceIsForwardOfBodyAndMountReachesMast() throws IOException {
        JsonObject plate = readJson(MODEL_ROOT.resolve("attachments/identification_plate.json"));
        double bodyFront = coordinate(plate, 0, "from", 2);
        double faceFront = coordinate(plate, 1, "from", 2);
        double mountRear = maximumCoordinate(plate, "to", 2);

        assertTrue(faceFront < bodyFront, "Painted plate face must sit in front of its body");
        assertTrue(CFRSignalVisualLayout.identificationTextOffsetZ() < faceFront / 16.0D);
        assertTrue(mountRear >= 9.25D, "Plate bracket must reach the mast depth");
    }

    @Test
    void wallBracketTouchesWallAndReachesSignalHead() throws IOException {
        JsonObject bracket = readJson(MODEL_ROOT.resolve("mount/wall_bracket.json"));

        assertEquals(16.0D, maximumCoordinate(bracket, "to", 2));
        double shiftedHeadSpineRear = 11.25D
                + CFRSignalVisualLayout.headForwardOffsetZ() * 16.0D;
        assertTrue(minimumCoordinate(bracket, "from", 2) <= shiftedHeadSpineRear);
    }

    @Test
    void cantileverHangerOverlapsScaledTwoLightHeadAndBeam() throws IOException {
        JsonObject hanger = readJson(MODEL_ROOT.resolve("mount/overhead_hanger.json"));
        JsonObject head = readJson(MODEL_ROOT.resolve("head/two_light_backplate.json"));
        JsonObject beam = readJson(MODEL_ROOT.resolve("mount/overhead_beam_segment.json"));

        double collarBottom = coordinate(hanger, 1, "from", 1);
        double scaledHeadTop = maximumCoordinate(head, "to", 1) * 0.9D;
        assertTrue(collarBottom <= scaledHeadTop, "Hanger collar must overlap the scaled head");

        double hangerTop = maximumCoordinate(hanger, "to", 1);
        double beamBottomInHeadCoordinates = minimumCoordinate(beam, "from", 1) + 16.0D;
        assertTrue(hangerTop >= beamBottomInHeadCoordinates, "Hanger must reach the overhead beam");
    }

    @Test
    void mechanicalBridgePedestalConnectsBeamAndSemaphoreAboveIt() throws IOException {
        JsonObject pedestal = readJson(MODEL_ROOT.resolve("mount/mechanical_bridge_pedestal.json"));
        JsonObject housing = readJson(MODEL_ROOT.resolve("mechanical/semaphore_mechanism_housing.json"));
        JsonObject beam = readJson(MODEL_ROOT.resolve("mount/overhead_beam_segment.json"));

        double housingPivotY = 12.0D;
        double scaledHousingBottom = housingPivotY
                + (minimumCoordinate(housing, "from", 1) - housingPivotY)
                * CFRSignalVisualLayout.mechanicalSemaphoreScale();
        assertTrue(maximumCoordinate(pedestal, "to", 1) >= scaledHousingBottom,
                "Mechanical pedestal must overlap the semaphore housing");

        double beamTopInHeadCoordinates = maximumCoordinate(beam, "to", 1) - 16.0D;
        assertTrue(minimumCoordinate(pedestal, "from", 1) <= beamTopInHeadCoordinates,
                "Mechanical pedestal must meet the bridge beam below the signal");
    }

    @Test
    void cantileverIdentificationPlateMountReachesTheSignalHousing() throws IOException {
        JsonObject plate = readJson(MODEL_ROOT.resolve("attachments/identification_plate.json"));
        JsonObject head = readJson(MODEL_ROOT.resolve("head/two_light_backplate.json"));

        double plateMountTop = 8.0D
                + (maximumCoordinate(plate, "to", 1) - 8.0D)
                * CFRSignalVisualLayout.bridgeIdentificationPlateScale();
        double lowPlateTop = plateMountTop
                + CFRSignalVisualLayout.identificationPlateOffsetFromHead(
                        ro.cfrsignals.config.SignalMount.CANTILEVER,
                        ro.cfrsignals.config.IdentificationPlatePosition.LOW
                ) * 16.0D;
        assertTrue(lowPlateTop >= minimumCoordinate(head, "from", 1),
                "Even the low bridge plate position must remain attached to the signal");
    }

    @Test
    void signalWrenchUsesTheCompleteCreateWrenchModel() throws IOException {
        JsonObject wrench = readJson(ASSET_ROOT.resolve("models/item/signal_wrench.json"));
        assertEquals("create:item/wrench/gear", wrench.get("parent").getAsString());
    }

    @Test
    void wallIdentificationPlateHasContinuousSupport() throws IOException {
        JsonObject plate = readJson(MODEL_ROOT.resolve("attachments/identification_plate.json"));
        JsonObject support = readJson(MODEL_ROOT.resolve("mount/wall_plate_bracket.json"));

        double scaledPlateRear = 8.0D + (maximumCoordinate(plate, "to", 2) - 8.0D) * 0.85D;
        assertTrue(minimumCoordinate(support, "from", 2) <= scaledPlateRear);
        assertEquals(16.0D, maximumCoordinate(support, "to", 2));
        assertTrue(
                maximumCoordinate(support, "to", 1) >= 4.5D,
                "Wall support must reach the scaled identification-plate mount"
        );
    }

    private static List<Path> filesWithExtension(Path root, String extension) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(extension))
                    .sorted()
                    .toList();
        }
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static ResolvedModel resolveModel(Path modelPath, Set<Path> resolving) throws IOException {
        Path normalized = modelPath.normalize();
        assertTrue(resolving.add(normalized), () -> "Model parent cycle at " + normalized);
        JsonObject model = readJson(normalized);
        Map<String, String> textures = new HashMap<>();
        JsonArray elements = null;
        if (model.has("parent")) {
            String parent = model.get("parent").getAsString();
            if (parent.startsWith("cfrsignals:")) {
                Path parentPath = ASSET_ROOT.resolve("models").resolve(
                        parent.substring("cfrsignals:".length()) + ".json"
                );
                assertTrue(Files.isRegularFile(parentPath), () -> normalized + " misses parent " + parentPath);
                ResolvedModel inherited = resolveModel(parentPath, resolving);
                textures.putAll(inherited.textures());
                elements = inherited.elements();
            }
        }
        JsonObject localTextures = model.getAsJsonObject("textures");
        if (localTextures != null) {
            for (var texture : localTextures.entrySet()) {
                textures.put(texture.getKey(), texture.getValue().getAsString());
            }
        }
        if (model.has("elements")) {
            elements = model.getAsJsonArray("elements");
        }
        resolving.remove(normalized);
        return new ResolvedModel(Map.copyOf(textures), elements);
    }

    private static String resolveTextureReference(
            Map<String, String> textures,
            String reference,
            Set<String> resolving
    ) {
        String resolved = reference;
        while (resolved.startsWith("#")) {
            String key = resolved.substring(1);
            assertTrue(resolving.add(key), () -> "Texture variable cycle at #" + key);
            String next = textures.get(key);
            assertNotNull(next, () -> "Missing texture variable #" + key);
            resolved = next;
        }
        return resolved;
    }

    private record ResolvedModel(Map<String, String> textures, JsonArray elements) {
    }

    private static double coordinate(JsonObject model, int element, String endpoint, int axis) {
        return model.getAsJsonArray("elements")
                .get(element)
                .getAsJsonObject()
                .getAsJsonArray(endpoint)
                .get(axis)
                .getAsDouble();
    }

    private static double minimumCoordinate(JsonObject model, String endpoint, int axis) {
        double minimum = Double.POSITIVE_INFINITY;
        for (JsonElement element : model.getAsJsonArray("elements")) {
            minimum = Math.min(
                    minimum,
                    element.getAsJsonObject().getAsJsonArray(endpoint).get(axis).getAsDouble()
            );
        }
        return minimum;
    }

    private static double maximumCoordinate(JsonObject model, String endpoint, int axis) {
        double maximum = Double.NEGATIVE_INFINITY;
        for (JsonElement element : model.getAsJsonArray("elements")) {
            maximum = Math.max(
                    maximum,
                    element.getAsJsonObject().getAsJsonArray(endpoint).get(axis).getAsDouble()
            );
        }
        return maximum;
    }

    private static boolean isPowerOfTwo(int value) {
        return value > 0 && (value & value - 1) == 0;
    }
}
