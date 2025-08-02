package cn.coostack.usefulmagic.renderer.shader

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.gl.GlProgramManager
import net.minecraft.client.gl.GlUniform
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL20
import org.spongepowered.asm.launch.GlobalProperties

class TestShader {
    var vertexShader: Identifier? = null
    var fragmentShader: Identifier? = null
    var programID = 0
    var vertexShaderID = 0
    var fragmentShaderID = 0
    fun bind() {
        GlProgramManager.useProgram(programID)
    }

    fun unbind() {
        GlProgramManager.useProgram(0)
    }

    fun getUniformLocation(name: String): Int {
        return GlUniform.getUniformLocation(programID, name)
    }


    fun loadShader() {
        programID = GlStateManager.glCreateProgram()
        vertexShaderID = compileShader(GL20.GL_VERTEX_SHADER, vertexShader)
        fragmentShaderID = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentShader)
        GlStateManager.glAttachShader(programID, vertexShaderID)
        GlStateManager.glAttachShader(programID, fragmentShaderID)
        GlStateManager.glLinkProgram(programID)
        checkProgram()
    }


    private fun compileShader(type: Int, shader: Identifier?): Int {
        shader ?: return 0
        val shaderID = GlStateManager.glCreateShader(type)
        val shaderCode = loadResourcesFromID(shader)
        GlStateManager.glShaderSource(shaderID, listOf(shaderCode))
        GlStateManager.glCompileShader(shaderID)
        checkShader(shaderID)
        return shaderID
    }

    private fun loadResourcesFromID(id: Identifier?): String {
        id ?: return ""
        val filePath = "${id.namespace}/${id.path}"
        val stream = this::class.java.classLoader.getResourceAsStream("assets/$filePath") ?: return ""
        return stream.readAllBytes().decodeToString()
    }

    private fun checkShader(id: Int) {
        if (GlStateManager.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw RuntimeException("Shader compilation failed: " + GL20.glGetShaderInfoLog(id))
        }
    }

    private fun checkProgram() {
        if (GlStateManager.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw RuntimeException("Program compilation failed: " + GL20.glGetProgramInfoLog(programID))
        }
    }
}