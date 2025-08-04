package cn.coostack.usefulmagic.renderer.shader

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.gl.GlProgramManager
import net.minecraft.client.gl.GlUniform
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL33
import org.lwjgl.opengl.GL33.*

class TestShader {
    var vertexShader: Identifier? = null
    var fragmentShader: Identifier? = null
    var programID = 0
    var vertexShaderID = 0
    var fragmentShaderID = 0

    var vao = 0
    var vbo = 0

    fun bind() {
        glBindVertexArray(vao)
        GlProgramManager.useProgram(programID)
    }

    fun unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
        GlProgramManager.useProgram(0)
    }

    fun getUniformLocation(name: String): Int {
        return GlUniform.getUniformLocation(programID, name)
    }


    fun loadShader() {
        programID = GlStateManager.glCreateProgram()
        vertexShaderID = compileShader(GL_VERTEX_SHADER, vertexShader)
        fragmentShaderID = compileShader(GL_FRAGMENT_SHADER, fragmentShader)
        GlStateManager.glAttachShader(programID, vertexShaderID)
        GlStateManager.glAttachShader(programID, fragmentShaderID)
        GlStateManager.glLinkProgram(programID)

        vao = glGenVertexArrays()
        vbo = glGenBuffers()
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        val w = 0.5f
        val h = 0.5f
        glBufferData(
            GL_ARRAY_BUFFER, floatArrayOf(
                -w, h, 0F,
                w, h, 0F,
                -w, -h, 0F,
                w, h, 0F,
                -w, -h, 0F,
                w, -h, 0F
            ), GL_STATIC_DRAW
        )

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
        glEnableVertexAttribArray(0)

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
        if (GlStateManager.glGetShaderi(id, GL33.GL_COMPILE_STATUS) == GL33.GL_FALSE) {
            throw RuntimeException("Shader compilation failed: " + GL33.glGetShaderInfoLog(id))
        }
    }

    private fun checkProgram() {
        if (GlStateManager.glGetProgrami(programID, GL33.GL_LINK_STATUS) == GL33.GL_FALSE) {
            throw RuntimeException("Program compilation failed: " + GL33.glGetProgramInfoLog(programID))
        }
    }
}