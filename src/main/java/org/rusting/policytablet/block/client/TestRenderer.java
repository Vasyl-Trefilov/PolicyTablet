package org.rusting.policytablet.block.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.rusting.policytablet.Policytablet;
import org.rusting.policytablet.block.TestBlockEntity;
import org.rusting.policytablet.block.client.WireModelLoader.MeshData;

import java.util.LinkedHashSet;
import java.util.Map;

public class TestRenderer implements BlockEntityRenderer<TestBlockEntity> {
    private static final ResourceLocation MODEL_LOCATION =
            ResourceLocation.fromNamespaceAndPath(Policytablet.MODID, "geo/block/test.glb");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Policytablet.MODID, "textures/block/wire.png");

    private final Map<String, MeshData> meshes;

    public TestRenderer(BlockEntityRendererProvider.Context context) {
        meshes = WireModelLoader.load(MODEL_LOCATION);
    }

    @Override
    public void render(TestBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (meshes.isEmpty()) {
            return;
        }

        VertexConsumer consumer = bufferSource.getBuffer(GlbRenderTypes.texturedTriangles(TEXTURE));

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        Matrix4f pose = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();
        for (MeshData mesh : new LinkedHashSet<>(meshes.values())) {
            renderMesh(mesh, consumer, pose, normalMatrix, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private static void renderMesh(MeshData mesh, VertexConsumer consumer, Matrix4f pose, Matrix3f normalMatrix,
                                   int packedLight, int packedOverlay) {
        float[] positions = mesh.positions();
        float[] normals = mesh.normals();
        float[] uvs = mesh.uvs();
        int[] indices = mesh.indices();
        int vertexCount = positions.length / 3;

        for (int i = 0; i + 2 < indices.length; i += 3) {
            int a = indices[i];
            int b = indices[i + 1];
            int c = indices[i + 2];
            if (!validIndex(a, vertexCount) || !validIndex(b, vertexCount) || !validIndex(c, vertexCount)) {
                continue;
            }

            emit(consumer, pose, normalMatrix, packedLight, packedOverlay, positions, normals, uvs, a);
            emit(consumer, pose, normalMatrix, packedLight, packedOverlay, positions, normals, uvs, b);
            emit(consumer, pose, normalMatrix, packedLight, packedOverlay, positions, normals, uvs, c);
        }
    }

    private static void emit(VertexConsumer consumer, Matrix4f pose, Matrix3f normalMatrix,
                             int packedLight, int packedOverlay, float[] positions, float[] normals, float[] uvs, int idx) {
        float x = positions[idx * 3];
        float y = positions[idx * 3 + 1];
        float z = positions[idx * 3 + 2];
        float nx = normals.length > 0 && idx * 3 + 2 < normals.length ? normals[idx * 3] : 0;
        float ny = normals.length > 0 && idx * 3 + 2 < normals.length ? normals[idx * 3 + 1] : 1;
        float nz = normals.length > 0 && idx * 3 + 2 < normals.length ? normals[idx * 3 + 2] : 0;
        float u = uvs.length > 0 && idx * 2 + 1 < uvs.length ? uvs[idx * 2] : 0;
        float v = uvs.length > 0 && idx * 2 + 1 < uvs.length ? uvs[idx * 2 + 1] : 0;
        consumer.vertex(pose, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();
    }

    private static boolean validIndex(int val, int max) {
        return val >= 0 && val < max;
    }
}
