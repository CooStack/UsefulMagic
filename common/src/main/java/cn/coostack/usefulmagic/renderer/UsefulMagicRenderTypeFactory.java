package cn.coostack.usefulmagic.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

/**
 * 构造需要自定义状态分片顺序和轮廓 RenderType 的客户端渲染类型。
 */
public final class UsefulMagicRenderTypeFactory extends RenderType {
    /** 当前渲染类型对应的轮廓渲染类型。 */
    private final Optional<RenderType> outline;

    /**
     * 创建按给定顺序设置和清理状态分片的渲染类型。
     *
     * @param states 每次绘制前后按顺序处理的状态分片
     */
    private UsefulMagicRenderTypeFactory(
        String name,
        VertexFormat format,
        VertexFormat.Mode mode,
        int bufferSize,
        boolean affectsCrumbling,
        boolean sortOnUpload,
        Optional<RenderType> outline,
        RenderStateShard[] states
    ) {
        super(
            name,
            format,
            mode,
            bufferSize,
            affectsCrumbling,
            sortOnUpload,
            () -> setupStates(states),
            () -> clearStates(states)
        );
        this.outline = outline;
    }

    /**
     * 创建龙法阵眼睛边框使用的半透明四边形渲染类型。
     *
     * <p>示例：{@code dragonCircleEyeBorder("eye_border", texture, shaderSupplier)}。
     *
     * @param name RenderType 调试名称
     * @param texture 边框纹理资源
     * @param shader 提供已注册 shader 的客户端 supplier
     * @return 带深度测试、轮廓和透明混合状态的 RenderType
     */
    public static RenderType dragonCircleEyeBorder(
        String name,
        ResourceLocation texture,
        Supplier<ShaderInstance> shader
    ) {
        return new UsefulMagicRenderTypeFactory(
            name,
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            1536,
            true,
            true,
            Optional.of(RenderType.outline(texture)),
            new RenderStateShard[] {
                new TextureStateShard(texture, false, false),
                new ShaderStateShard(shader),
                TRANSLUCENT_TRANSPARENCY,
                LEQUAL_DEPTH_TEST,
                NO_CULL,
                NO_LIGHTMAP,
                OVERLAY,
                NO_LAYERING,
                MAIN_TARGET,
                DEFAULT_TEXTURING,
                COLOR_DEPTH_WRITE,
                NO_COLOR_LOGIC,
                DEFAULT_LINE
            }
        );
    }

    /** 按声明顺序启用全部状态分片。 */
    private static void setupStates(RenderStateShard[] states) {
        for (RenderStateShard state : states) {
            state.setupRenderState();
        }
    }

    /** 按声明顺序清理全部状态分片。 */
    private static void clearStates(RenderStateShard[] states) {
        for (RenderStateShard state : states) {
            state.clearRenderState();
        }
    }

    @Override
    public Optional<RenderType> outline() {
        return outline;
    }
}
