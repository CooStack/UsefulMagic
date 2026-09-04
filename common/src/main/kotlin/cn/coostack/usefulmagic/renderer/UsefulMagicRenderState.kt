package cn.coostack.usefulmagic.renderer

import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL33.GL_ACTIVE_TEXTURE
import org.lwjgl.opengl.GL33.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL33.GL_ARRAY_BUFFER_BINDING
import org.lwjgl.opengl.GL33.GL_BLEND
import org.lwjgl.opengl.GL33.GL_BLEND_DST_ALPHA
import org.lwjgl.opengl.GL33.GL_BLEND_DST_RGB
import org.lwjgl.opengl.GL33.GL_BLEND_EQUATION_ALPHA
import org.lwjgl.opengl.GL33.GL_BLEND_EQUATION_RGB
import org.lwjgl.opengl.GL33.GL_BLEND_SRC_ALPHA
import org.lwjgl.opengl.GL33.GL_BLEND_SRC_RGB
import org.lwjgl.opengl.GL33.GL_CURRENT_PROGRAM
import org.lwjgl.opengl.GL33.GL_CULL_FACE
import org.lwjgl.opengl.GL33.GL_DEPTH_FUNC
import org.lwjgl.opengl.GL33.GL_DEPTH_TEST
import org.lwjgl.opengl.GL33.GL_DEPTH_WRITEMASK
import org.lwjgl.opengl.GL33.GL_TEXTURE0
import org.lwjgl.opengl.GL33.GL_TEXTURE_2D
import org.lwjgl.opengl.GL33.GL_TEXTURE_BINDING_2D
import org.lwjgl.opengl.GL33.GL_VERTEX_ARRAY_BINDING
import org.lwjgl.opengl.GL33.glActiveTexture
import org.lwjgl.opengl.GL33.glBindBuffer
import org.lwjgl.opengl.GL33.glBindTexture
import org.lwjgl.opengl.GL33.glBindVertexArray
import org.lwjgl.opengl.GL33.glBlendEquationSeparate
import org.lwjgl.opengl.GL33.glBlendFuncSeparate
import org.lwjgl.opengl.GL33.glDepthFunc
import org.lwjgl.opengl.GL33.glDepthMask
import org.lwjgl.opengl.GL33.glDisable
import org.lwjgl.opengl.GL33.glEnable
import org.lwjgl.opengl.GL33.glGetBoolean
import org.lwjgl.opengl.GL33.glGetInteger
import org.lwjgl.opengl.GL33.glIsEnabled
import org.lwjgl.opengl.GL33.glUseProgram

/**
 * 保存一次实体绘制会修改的 RenderSystem 与 OpenGL 状态。
 *
 * @property depthEnabled 捕获时的深度测试开关
 * @property cullEnabled 捕获时的面剔除开关
 * @property blendEnabled 捕获时的混合开关
 * @property depthMask 捕获时的深度写入开关
 * @property depthFunc 捕获时的深度比较函数
 * @property blendSrcRgb RGB 混合源因子
 * @property blendDstRgb RGB 混合目标因子
 * @property blendSrcAlpha Alpha 混合源因子
 * @property blendDstAlpha Alpha 混合目标因子
 * @property blendEquationRgb RGB 混合方程
 * @property blendEquationAlpha Alpha 混合方程
 * @property program 捕获时绑定的 shader program
 * @property activeTexture 捕获时的活动纹理单元
 * @property vertexArray 捕获时绑定的 VAO
 * @property arrayBuffer 捕获时绑定的数组顶点缓冲
 * @property shaderTextures 前两个 shader 纹理槽的缓存纹理 ID
 * @property textureBindings 前两个纹理单元实际绑定的 2D 纹理 ID
 */
internal class UsefulMagicRenderState private constructor(
    private val depthEnabled: Boolean,
    private val cullEnabled: Boolean,
    private val blendEnabled: Boolean,
    private val depthMask: Boolean,
    private val depthFunc: Int,
    private val blendSrcRgb: Int,
    private val blendDstRgb: Int,
    private val blendSrcAlpha: Int,
    private val blendDstAlpha: Int,
    private val blendEquationRgb: Int,
    private val blendEquationAlpha: Int,
    private val program: Int,
    private val activeTexture: Int,
    private val vertexArray: Int,
    private val arrayBuffer: Int,
    private val shaderTextures: IntArray,
    private val textureBindings: IntArray,
) {
    /** 恢复捕获时的 RenderSystem 缓存和真实 OpenGL 状态。 */
    fun restore() {
        RenderSystem.blendFuncSeparate(blendSrcRgb, blendDstRgb, blendSrcAlpha, blendDstAlpha)
        glBlendFuncSeparate(blendSrcRgb, blendDstRgb, blendSrcAlpha, blendDstAlpha)
        glBlendEquationSeparate(blendEquationRgb, blendEquationAlpha)
        RenderSystem.depthMask(depthMask)
        glDepthMask(depthMask)
        RenderSystem.depthFunc(depthFunc)
        glDepthFunc(depthFunc)
        glUseProgram(program)
        glBindVertexArray(vertexArray)
        glBindBuffer(GL_ARRAY_BUFFER, arrayBuffer)
        shaderTextures.forEachIndexed { index, texture ->
            RenderSystem.setShaderTexture(index, texture)
            RenderSystem.activeTexture(GL_TEXTURE0 + index)
            glActiveTexture(GL_TEXTURE0 + index)
            RenderSystem.bindTexture(textureBindings[index])
            glBindTexture(GL_TEXTURE_2D, textureBindings[index])
        }
        RenderSystem.activeTexture(activeTexture)
        glActiveTexture(activeTexture)
        restoreCapability(GL_DEPTH_TEST, depthEnabled, RenderSystem::enableDepthTest, RenderSystem::disableDepthTest)
        restoreCapability(GL_CULL_FACE, cullEnabled, RenderSystem::enableCull, RenderSystem::disableCull)
        restoreCapability(GL_BLEND, blendEnabled, RenderSystem::enableBlend, RenderSystem::disableBlend)
    }

    /** 根据捕获值同时恢复 RenderSystem 缓存与底层 OpenGL capability。 */
    private fun restoreCapability(
        capability: Int,
        enabled: Boolean,
        enableRenderSystem: () -> Unit,
        disableRenderSystem: () -> Unit,
    ) {
        if (enabled) {
            enableRenderSystem()
            glEnable(capability)
        } else {
            disableRenderSystem()
            glDisable(capability)
        }
    }

    /** 提供渲染状态捕获入口。 */
    companion object {
        /**
         * 在操作结束或失败后恢复进入操作前的 OpenGL 状态。
         *
         * @return [block] 的执行结果
         */
        fun <T> preserve(block: () -> T): T {
            val state = capture()
            return try {
                block()
            } finally {
                state.restore()
            }
        }

        /**
         * 读取实体 renderer 会触碰的当前 OpenGL 状态。
         *
         * @return 可在本次实体绘制结束后调用 `restore` 的状态快照
         */
        fun capture(): UsefulMagicRenderState {
            val previousActiveTexture = glGetInteger(GL_ACTIVE_TEXTURE)
            val previousShaderTextures = IntArray(2) { RenderSystem.getShaderTexture(it) }
            val previousTextureBindings = IntArray(previousShaderTextures.size) { index ->
                glActiveTexture(GL_TEXTURE0 + index)
                glGetInteger(GL_TEXTURE_BINDING_2D)
            }
            glActiveTexture(previousActiveTexture)
            return UsefulMagicRenderState(
                depthEnabled = glIsEnabled(GL_DEPTH_TEST),
                cullEnabled = glIsEnabled(GL_CULL_FACE),
                blendEnabled = glIsEnabled(GL_BLEND),
                depthMask = glGetBoolean(GL_DEPTH_WRITEMASK),
                depthFunc = glGetInteger(GL_DEPTH_FUNC),
                blendSrcRgb = glGetInteger(GL_BLEND_SRC_RGB),
                blendDstRgb = glGetInteger(GL_BLEND_DST_RGB),
                blendSrcAlpha = glGetInteger(GL_BLEND_SRC_ALPHA),
                blendDstAlpha = glGetInteger(GL_BLEND_DST_ALPHA),
                blendEquationRgb = glGetInteger(GL_BLEND_EQUATION_RGB),
                blendEquationAlpha = glGetInteger(GL_BLEND_EQUATION_ALPHA),
                program = glGetInteger(GL_CURRENT_PROGRAM),
                activeTexture = previousActiveTexture,
                vertexArray = glGetInteger(GL_VERTEX_ARRAY_BINDING),
                arrayBuffer = glGetInteger(GL_ARRAY_BUFFER_BINDING),
                shaderTextures = previousShaderTextures,
                textureBindings = previousTextureBindings,
            )
        }
    }
}
