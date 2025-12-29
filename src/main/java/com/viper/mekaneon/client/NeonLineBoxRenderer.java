package com.viper.mekaneon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class NeonLineBoxRenderer {
    private NeonLineBoxRenderer() {
    }

    public static void renderBoxLines(PoseStack poseStack, MultiBufferSource buffers, Vec3 camPos, BlockPos pos,
                                      float r, float g, float b, float a) {
        poseStack.pushPose();
        poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);

        float expand = 0.01f;
        float min = -expand;
        float max = 1.0f + expand;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        VertexConsumer vc = buffers.getBuffer(RenderType.lines());
        int fullBright = 0xF000F0;

        addLine(vc, mat, min, min, min, max, min, min, r, g, b, a, fullBright);
        addLine(vc, mat, min, max, min, max, max, min, r, g, b, a, fullBright);
        addLine(vc, mat, min, min, max, max, min, max, r, g, b, a, fullBright);
        addLine(vc, mat, min, max, max, max, max, max, r, g, b, a, fullBright);

        addLine(vc, mat, min, min, min, min, max, min, r, g, b, a, fullBright);
        addLine(vc, mat, max, min, min, max, max, min, r, g, b, a, fullBright);
        addLine(vc, mat, min, min, max, min, max, max, r, g, b, a, fullBright);
        addLine(vc, mat, max, min, max, max, max, max, r, g, b, a, fullBright);

        addLine(vc, mat, min, min, min, min, min, max, r, g, b, a, fullBright);
        addLine(vc, mat, max, min, min, max, min, max, r, g, b, a, fullBright);
        addLine(vc, mat, min, max, min, min, max, max, r, g, b, a, fullBright);
        addLine(vc, mat, max, max, min, max, max, max, r, g, b, a, fullBright);

        poseStack.popPose();
    }

    private static void addLine(VertexConsumer vc, Matrix4f mat,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float r, float g, float b, float a,
                                int light) {
        vc.addVertex(mat, x1, y1, z1)
                .setColor(r, g, b, a)
                .setNormal(0.0f, 0.0f, 0.0f)
                .setLight(light);
        vc.addVertex(mat, x2, y2, z2)
                .setColor(r, g, b, a)
                .setNormal(0.0f, 0.0f, 0.0f)
                .setLight(light);
    }
}
