package org.rusting.policytablet.block.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.rusting.policytablet.Policytablet;
import org.rusting.policytablet.block.WireBlock;
import org.rusting.policytablet.block.WireBlockEntity;
import org.rusting.policytablet.block.client.WireModelLoader.MeshData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class WireBlockRenderer implements BlockEntityRenderer<WireBlockEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Policytablet.MODID, "textures/block/wire.png");
    private static final float CX = -0.088f, CY = 0.411f, CZ = -0.020f;

    public WireBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WireBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();

        boolean north = state.getValue(WireBlock.NORTH);
        boolean south = state.getValue(WireBlock.SOUTH);
        boolean east = state.getValue(WireBlock.EAST);
        boolean west = state.getValue(WireBlock.WEST);
        int count = (north ? 1 : 0) + (south ? 1 : 0) + (east ? 1 : 0) + (west ? 1 : 0);

        VertexConsumer consumer = bufferSource.getBuffer(GlbRenderTypes.texturedTriangles(TEXTURE));

        String meshName;
        double rotation = 0;

        if (count == 0) {
            meshName = "single";
        } else if (count == 1) {
            meshName = "end";
            if (east) rotation = 90;
            else if (south) rotation = 180;
            else if (west) rotation = 270;
        } else if (count == 2) {
            if ((north && south) || (east && west)) {
                meshName = "straight";
                if (east && west) rotation = 90;
            } else {
                meshName = "corner";
                if (east && south) rotation = 90;
                else if (south && west) rotation = 180;
                else if (west && north) rotation = 270;
            }
        } else if (count == 3) {
            meshName = "tee";
            if (!north) rotation = 180;
            else if (!east) rotation = 270;
            else if (!west) rotation = 90;
        } else {
            meshName = "cross";
        }

        renderSingleMesh(meshName, rotation, consumer, poseStack, packedLight, packedOverlay);
    }

    private void renderSingleMesh(String name, double rotation, VertexConsumer consumer, PoseStack poseStack,
                                   int packedLight, int packedOverlay) {
        MeshData mesh = WireModelLoader.get().getMesh(name);
        if (mesh == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        if (rotation != 0) {
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float)rotation));
        }
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.translate(-CX, -CY, -CZ);

        Matrix4f pose = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();

        float[] positions = mesh.positions();
        float[] normals = mesh.normals();
        float[] uvs = mesh.uvs();
        int[] indices = mesh.indices();
        int vertexCount = positions.length / 3;

        for (int i = 0; i + 2 < indices.length; i += 3) {
            int idx0 = indices[i];
            int idx1 = indices[i + 1];
            int idx2 = indices[i + 2];
            if (!validIndex(idx0, vertexCount) || !validIndex(idx1, vertexCount) || !validIndex(idx2, vertexCount)) {
                continue;
            }
            emitVertex(consumer, pose, normalMatrix, packedLight, packedOverlay, positions, normals, uvs, idx0);
            emitVertex(consumer, pose, normalMatrix, packedLight, packedOverlay, positions, normals, uvs, idx1);
            emitVertex(consumer, pose, normalMatrix, packedLight, packedOverlay, positions, normals, uvs, idx2);
        }

        poseStack.popPose();
    }

    private static boolean validIndex(int val, int max) {
        return val >= 0 && val < max;
    }

    private void emitVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normalMatrix,
                            int packedLight, int packedOverlay, float[] positions, float[] normals, float[] uvs, int idx) {
        float x = positions[idx * 3];
        float y = positions[idx * 3 + 1];
        float z = positions[idx * 3 + 2];
        float nx, ny, nz;
        if (normals.length > 0 && idx * 3 + 2 < normals.length) {
            nx = normals[idx * 3];
            ny = normals[idx * 3 + 1];
            nz = normals[idx * 3 + 2];
        } else {
            nx = 0; ny = 1; nz = 0;
        }
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
}
