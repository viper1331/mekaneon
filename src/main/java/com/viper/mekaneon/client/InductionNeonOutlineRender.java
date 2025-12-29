package com.viper.mekaneon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.viper.mekaneon.MekaNeon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

@EventBusSubscriber(modid = MekaNeon.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class InductionNeonOutlineRender {
    private static final double MAX_DISTANCE_SQ = 64.0 * 64.0;

    private InductionNeonOutlineRender() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        for (BlockEntity blockEntity : level.blockEntityList) {
            BlockPos pos = blockEntity.getBlockPos();
            double dx = pos.getX() + 0.5 - camPos.x;
            double dy = pos.getY() + 0.5 - camPos.y;
            double dz = pos.getZ() + 0.5 - camPos.z;
            if (dx * dx + dy * dy + dz * dz > MAX_DISTANCE_SQ) {
                continue;
            }

            BlockState state = blockEntity.getBlockState();
            if (!isInductionCellOrProvider(state)) {
                continue;
            }

            float ratio = getFillRatio(level, pos);
            if (ratio <= 0.001f) {
                continue;
            }

            float alpha = Mth.clamp((float) Math.pow(ratio, 1.7f), 0.0f, 1.0f);
            if (ratio >= 0.90f) {
                float pulse01 = 0.5f + 0.5f * Mth.sin(((float) level.getGameTime() + event.getPartialTick()) * 0.25f);
                float k = Mth.clamp((ratio - 0.90f) / 0.10f, 0.0f, 1.0f);
                float amp = Mth.lerp(k, 0.03f, 0.12f);
                alpha = Mth.clamp(alpha + pulse01 * amp, 0.0f, 1.0f);
            }

            int tint = mc.getBlockColors().getColor(state, level, pos, 0);
            float r = ((tint >> 16) & 0xFF) / 255.0f;
            float g = ((tint >> 8) & 0xFF) / 255.0f;
            float b = (tint & 0xFF) / 255.0f;

            NeonLineBoxRenderer.renderBoxLines(poseStack, buffers, camPos, pos, r, g, b, alpha);
        }

        buffers.endBatch();
    }

    private static boolean isInductionCellOrProvider(BlockState state) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (key == null || !"mekanism".equals(key.getNamespace())) {
            return false;
        }
        String path = key.getPath();
        return "induction_cell".equals(path) || "induction_provider".equals(path);
    }

    private static float getFillRatio(Level level, BlockPos pos) {
        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        if (storage == null) {
            return 0.0f;
        }
        int max = storage.getMaxEnergyStored();
        if (max <= 0) {
            return 0.0f;
        }
        return Mth.clamp(storage.getEnergyStored() / (float) max, 0.0f, 1.0f);
    }
}
