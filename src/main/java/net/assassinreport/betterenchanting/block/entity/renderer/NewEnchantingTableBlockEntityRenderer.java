package net.assassinreport.betterenchanting.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

public class NewEnchantingTableBlockEntityRenderer implements BlockEntityRenderer<NewEnchantingTableBlockEntity, NewEnchantingTableBlockEntityRenderer.State> {

    public static final SpriteId BOOK_TEXTURE = Sheets.BLOCK_ENTITIES_MAPPER.defaultNamespaceApply("enchantment/enchanting_table_book");

    private final SpriteGetter sprites;
    private final BookModel book;

    public NewEnchantingTableBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.sprites = ctx.sprites();
        this.book = new BookModel(ctx.bakeLayer(ModelLayers.BOOK));
    }

    @NullMarked
    @Override
    public State createRenderState() {
        return new State();
    }

    @NullMarked
    @Override
    public void extractRenderState(NewEnchantingTableBlockEntity entity, State state, float tickDelta,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, tickDelta, cameraPos, crumblingOverlay);
        state.pageAngle = Mth.lerp(tickDelta, entity.bookAnimation.pageAngle, entity.bookAnimation.nextPageAngle);
        state.pageTurningSpeed = Mth.lerp(tickDelta, entity.bookAnimation.pageTurningSpeed, entity.bookAnimation.nextPageTurningSpeed);
        state.ticks = entity.bookAnimation.ticks + tickDelta;

        float rotationDiff = entity.bookAnimation.bookRotation - entity.bookAnimation.lastBookRotation;
        while (rotationDiff >= Math.PI) rotationDiff -= (float) (Math.PI * 2);
        while (rotationDiff < -Math.PI) rotationDiff += (float) (Math.PI * 2);
        state.bookRotationDegrees = entity.bookAnimation.lastBookRotation + rotationDiff * tickDelta;
    }

    @NullMarked
    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        poseStack.translate(0.0F, 0.1F + Mth.sin(state.ticks * 0.1F) * 0.01F, 0.0F);
        poseStack.mulPose(Axis.YP.rotation(-state.bookRotationDegrees));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));

        float page1 = Mth.frac(state.pageAngle + 0.25F) * 1.6F - 0.3F;
        float page2 = Mth.frac(state.pageAngle + 0.75F) * 1.6F - 0.3F;
        BookModel.State bookState = BookModel.State.forAnimation(
                state.ticks,
                Mth.clamp(page1, 0.0F, 1.0F),
                Mth.clamp(page2, 0.0F, 1.0F),
                state.pageTurningSpeed
        );

        submitNodeCollector.submitModel(
                this.book, bookState, poseStack,
                state.lightCoords, OverlayTexture.NO_OVERLAY,
                -1,
                BOOK_TEXTURE,
                this.sprites,
                0,
                state.breakProgress
        );

        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState {
        float ticks;
        float bookRotationDegrees;
        float pageAngle;
        float pageTurningSpeed;
    }
}