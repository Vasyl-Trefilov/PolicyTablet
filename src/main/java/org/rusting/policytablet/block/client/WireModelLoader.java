package org.rusting.policytablet.block.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.rusting.policytablet.Policytablet;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WireModelLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation MODEL_LOCATION =
            ResourceLocation.fromNamespaceAndPath(Policytablet.MODID, "geo/block/wire.glb");

    private static WireModelLoader instance;

    private final Map<String, MeshData> meshes = new HashMap<>();

    private WireModelLoader() {
        load();
    }

    public static WireModelLoader get() {
        if (instance == null) {
            instance = new WireModelLoader();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public MeshData getMesh(String name) {
        return meshes.get(name);
    }

    private void load() {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(MODEL_LOCATION);
            try (InputStream stream = resource.open()) {
                byte[] bytes = stream.readAllBytes();
                ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                meshes.putAll(parseGLB(buffer));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load wire model", e);
        }
    }

    public static Map<String, MeshData> load(ResourceLocation modelLocation) {
        Map<String, MeshData> loadedMeshes = new HashMap<>();
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(modelLocation);
            try (InputStream stream = resource.open()) {
                byte[] bytes = stream.readAllBytes();
                ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                loadedMeshes.putAll(parseGLB(buffer));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load GLB model '{}'", modelLocation, e);
        }
        return loadedMeshes;
    }

    private static Map<String, MeshData> parseGLB(ByteBuffer buffer) {
        Map<String, MeshData> loadedMeshes = new HashMap<>();
        int magic = buffer.getInt();
        int version = buffer.getInt();
        int totalLength = buffer.getInt();

        if (magic != 0x46546C67) {
            LOGGER.error("Invalid GLB magic: {}", Integer.toHexString(magic));
            return loadedMeshes;
        }
        if (version != 2 || totalLength > buffer.capacity()) {
            LOGGER.error("Unsupported GLB header: version={}, length={}, buffer={}", version, totalLength, buffer.capacity());
            return loadedMeshes;
        }

        int jsonChunkLength = buffer.getInt();
        int jsonChunkType = buffer.getInt();
        byte[] jsonBytes = new byte[jsonChunkLength];
        buffer.get(jsonBytes);
        String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);

        int binChunkLength = buffer.getInt();
        int binChunkType = buffer.getInt();
        byte[] binBytes = new byte[binChunkLength];
        buffer.get(binBytes);
        ByteBuffer binBuffer = ByteBuffer.wrap(binBytes).order(ByteOrder.LITTLE_ENDIAN);

        if (jsonChunkType != 0x4E4F534A || binChunkType != 0x004E4942) {
            LOGGER.error("Invalid GLB chunk types: json={}, bin={}", Integer.toHexString(jsonChunkType), Integer.toHexString(binChunkType));
            return loadedMeshes;
        }

        parseGltfJson(jsonString, binBuffer, loadedMeshes);
        return loadedMeshes;
    }

    private record GltfAccessor(int bufferView, int byteOffset, int count, int componentType, String typeStr, int typeCount) {}
    private record GltfBufferView(int byteOffset, int byteLength, int byteStride) {}

    private static void parseGltfJson(String json, ByteBuffer binBuffer, Map<String, MeshData> loadedMeshes) {
        try {
            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();

            var accessors = new ArrayList<GltfAccessor>();
            for (var accEl : root.getAsJsonArray("accessors")) {
                var acc = accEl.getAsJsonObject();
                int bv = acc.has("bufferView") ? acc.get("bufferView").getAsInt() : -1;
                int off = acc.has("byteOffset") ? acc.get("byteOffset").getAsInt() : 0;
                int cnt = acc.get("count").getAsInt();
                int ct = acc.get("componentType").getAsInt();
                String type = acc.get("type").getAsString();
                int tc = switch (type) {
                    case "SCALAR" -> 1;
                    case "VEC2" -> 2;
                    case "VEC3" -> 3;
                    case "VEC4" -> 4;
                    case "MAT2" -> 4;
                    case "MAT3" -> 9;
                    case "MAT4" -> 16;
                    default -> 0;
                };
                accessors.add(new GltfAccessor(bv, off, cnt, ct, type, tc));
            }

            var bufferViews = new ArrayList<GltfBufferView>();
            for (var bvEl : root.getAsJsonArray("bufferViews")) {
                var bv = bvEl.getAsJsonObject();
                int off = bv.has("byteOffset") ? bv.get("byteOffset").getAsInt() : 0;
                int len = bv.get("byteLength").getAsInt();
                int stride = bv.has("byteStride") ? bv.get("byteStride").getAsInt() : 0;
                bufferViews.add(new GltfBufferView(off, len, stride));
            }

            var nodes = root.getAsJsonArray("nodes");
            int unnamedNode = 0;
            for (var nodeEl : nodes) {
                var node = nodeEl.getAsJsonObject();
                String nodeName = node.has("name") ? node.get("name").getAsString() : "mesh_" + unnamedNode++;
                if (node.has("mesh")) {
                    int meshIdx = node.get("mesh").getAsInt();
                    var mesh = root.getAsJsonArray("meshes").get(meshIdx).getAsJsonObject();
                    MeshData nodeMesh = readMeshPrimitives(accessors, bufferViews, binBuffer, mesh);
                    loadedMeshes.put(nodeName, nodeMesh);
                    if (mesh.has("name")) {
                        loadedMeshes.putIfAbsent(mesh.get("name").getAsString(), nodeMesh);
                    }
                    float[] positions = nodeMesh.positions();
                    int[] indices = nodeMesh.indices();
                    LOGGER.debug("Loaded mesh '{}': {} positions, {} indices", nodeName, positions.length / 3, indices.length);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse GLTF JSON", e);
        }
    }

    private static MeshData readMeshPrimitives(ArrayList<GltfAccessor> accessors,
                                               ArrayList<GltfBufferView> bufferViews,
                                               ByteBuffer binBuffer,
                                               com.google.gson.JsonObject mesh) {
        float[] positions = new float[0];
        float[] normals = new float[0];
        float[] uvs = new float[0];
        int[] indices = new int[0];
        int vertexOffset = 0;

        for (var primEl : mesh.getAsJsonArray("primitives")) {
            var prim = primEl.getAsJsonObject();
            int mode = prim.has("mode") ? prim.get("mode").getAsInt() : 4;
            if (mode != 4) {
                LOGGER.warn("Skipping non-triangle GLB primitive mode {}", mode);
                continue;
            }

            var attrs = prim.get("attributes").getAsJsonObject();
            int posAcc = attrs.get("POSITION").getAsInt();
            int normAcc = attrs.has("NORMAL") ? attrs.get("NORMAL").getAsInt() : -1;
            int texAcc = attrs.has("TEXCOORD_0") ? attrs.get("TEXCOORD_0").getAsInt() : -1;
            int idxAcc = prim.has("indices") ? prim.get("indices").getAsInt() : -1;

            float[] primitivePositions = readFloatAccessor(accessors, bufferViews, binBuffer, posAcc);
            float[] primitiveNormals = normAcc >= 0 ? readFloatAccessor(accessors, bufferViews, binBuffer, normAcc) : new float[0];
            float[] primitiveUvs = texAcc >= 0 ? readFloatAccessor(accessors, bufferViews, binBuffer, texAcc) : new float[0];
            int primitiveVertexCount = primitivePositions.length / 3;
            int[] primitiveIndices = idxAcc >= 0
                    ? readIndexAccessor(accessors, bufferViews, binBuffer, idxAcc)
                    : sequentialIndices(primitiveVertexCount);

            for (int i = 0; i < primitiveIndices.length; i++) {
                primitiveIndices[i] += vertexOffset;
            }

            positions = append(positions, primitivePositions);
            normals = append(normals, primitiveNormals);
            uvs = append(uvs, primitiveUvs);
            indices = append(indices, primitiveIndices);
            vertexOffset += primitiveVertexCount;
        }

        return new MeshData(positions, normals, uvs, indices);
    }

    private static float[] readFloatAccessor(ArrayList<GltfAccessor> accessors,
                                             ArrayList<GltfBufferView> bufferViews,
                                             ByteBuffer binBuffer, int accIdx) {
        var acc = accessors.get(accIdx);
        var bv = bufferViews.get(acc.bufferView);
        int total = acc.count * acc.typeCount;
        int componentSize = componentSize(acc.componentType);
        int stride = bv.byteStride > 0 ? bv.byteStride : componentSize * acc.typeCount;

        float[] result = new float[total];
        for (int i = 0; i < acc.count; i++) {
            ByteBuffer slice = binBuffer.duplicate().order(ByteOrder.LITTLE_ENDIAN);
            slice.position(bv.byteOffset() + acc.byteOffset + i * stride);
            for (int j = 0; j < acc.typeCount; j++) {
                result[i * acc.typeCount + j] = readComponentAsFloat(slice, acc.componentType);
            }
        }
        return result;
    }

    private static int[] readIndexAccessor(ArrayList<GltfAccessor> accessors,
                                           ArrayList<GltfBufferView> bufferViews,
                                           ByteBuffer binBuffer, int accIdx) {
        var acc = accessors.get(accIdx);
        if (acc.bufferView < 0) {
            return new int[0];
        }
        var bv = bufferViews.get(acc.bufferView);
        int stride = bv.byteStride > 0 ? bv.byteStride : componentSize(acc.componentType);
        int[] result = new int[acc.count];
        for (int i = 0; i < acc.count; i++) {
            ByteBuffer slice = binBuffer.duplicate().order(ByteOrder.LITTLE_ENDIAN);
            slice.position(bv.byteOffset() + acc.byteOffset + i * stride);
            result[i] = readIndex(slice, acc.componentType);
        }
        return result;
    }

    private static int componentSize(int componentType) {
        return switch (componentType) {
            case 5120, 5121 -> 1;
            case 5122, 5123 -> 2;
            case 5125, 5126 -> 4;
            default -> throw new IllegalArgumentException("Unsupported GLB component type: " + componentType);
        };
    }

    private static float readComponentAsFloat(ByteBuffer buffer, int componentType) {
        return switch (componentType) {
            case 5120 -> buffer.get();
            case 5121 -> buffer.get() & 0xFF;
            case 5122 -> buffer.getShort();
            case 5123 -> buffer.getShort() & 0xFFFF;
            case 5125 -> Integer.toUnsignedLong(buffer.getInt());
            case 5126 -> buffer.getFloat();
            default -> throw new IllegalArgumentException("Unsupported GLB component type: " + componentType);
        };
    }

    private static int readIndex(ByteBuffer buffer, int componentType) {
        return switch (componentType) {
            case 5121 -> buffer.get() & 0xFF;
            case 5123 -> buffer.getShort() & 0xFFFF;
            case 5125 -> buffer.getInt();
            default -> throw new IllegalArgumentException("Unsupported GLB index component type: " + componentType);
        };
    }

    private static int[] sequentialIndices(int vertexCount) {
        int[] result = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            result[i] = i;
        }
        return result;
    }

    private static float[] append(float[] first, float[] second) {
        float[] result = new float[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static int[] append(int[] first, int[] second) {
        int[] result = new int[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public record MeshData(float[] positions, float[] normals, float[] uvs, int[] indices) {}
}
