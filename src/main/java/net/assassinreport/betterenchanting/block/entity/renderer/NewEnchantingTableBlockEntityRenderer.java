package net.assassinreport.betterenchanting.block.entity.renderer;

import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class NewEnchantingTableBlockEntityRenderer implements BlockEntityRenderer<NewEnchantingTableBlockEntity, NewEnchantingTableBlockEntityRenderer.State> {

    public static final SpriteIdentifier BOOK_TEXTURE =
            TexturedRenderLayers.ENTITY_SPRITE_MAPPER.mapVanilla("enchanting_table_book");

    private final SpriteHolder spriteHolder;
    private final BookModel book;

    public NewEnchantingTableBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.spriteHolder = ctx.spriteHolder();
        this.book = new BookModel(ctx.getLayerModelPart(EntityModelLayers.BOOK));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(NewEnchantingTableBlockEntity entity, State state, float tickDelta,
                                  Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumblingOverlay);
        state.pageAngle = MathHelper.lerp(tickDelta, entity.bookAnimation.pageAngle, entity.bookAnimation.nextPageAngle);
        state.pageTurningSpeed = MathHelper.lerp(tickDelta, entity.bookAnimation.pageTurningSpeed, entity.bookAnimation.nextPageTurningSpeed);
        state.ticks = entity.bookAnimation.ticks + tickDelta;

        float rotationDiff = entity.bookAnimation.bookRotation - entity.bookAnimation.lastBookRotation;
        while (rotationDiff >= Math.PI) rotationDiff -= (float) (Math.PI * 2);
        while (rotationDiff < -Math.PI) rotationDiff += (float) (Math.PI * 2);
        state.bookRotationDegrees = entity.bookAnimation.lastBookRotation + rotationDiff * tickDelta;
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue,
                       CameraRenderState cameraRenderState) {
        matrices.push();
        matrices.translate(0.5F, 0.75F, 0.5F);
        matrices.translate(0.0F, 0.1F + MathHelper.sin(state.ticks * 0.1F) * 0.01F, 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-state.bookRotationDegrees));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(80.0F));

        float page1 = MathHelper.fractionalPart(state.pageAngle + 0.25F) * 1.6F - 0.3F;
        float page2 = MathHelper.fractionalPart(state.pageAngle + 0.75F) * 1.6F - 0.3F;
        BookModel.BookModelState bookState = new BookModel.BookModelState(
                state.ticks,
                MathHelper.clamp(page1, 0.0F, 1.0F),
                MathHelper.clamp(page2, 0.0F, 1.0F),
                state.pageTurningSpeed
        );

        queue.submitModel(
                this.book, bookState, matrices,
                BOOK_TEXTURE.getRenderLayer(RenderLayers::entitySolid),
                state.lightmapCoordinates, OverlayTexture.DEFAULT_UV,
                -1,
                this.spriteHolder.getSprite(BOOK_TEXTURE),
                0,
                state.crumblingOverlay
        );

        matrices.pop();
    }

    public static class State extends BlockEntityRenderState {
        float ticks;
        float bookRotationDegrees;
        float pageAngle;
        float pageTurningSpeed;
    }
}