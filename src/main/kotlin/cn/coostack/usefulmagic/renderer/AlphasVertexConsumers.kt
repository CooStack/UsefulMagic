package cn.coostack.usefulmagic.renderer

import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.util.math.MatrixStack
import org.joml.Matrix4f
import org.joml.Vector3f

class AlphasVertexConsumers(var alpha: Int, val consumer: VertexConsumer) : VertexConsumer {
    override fun vertex(
        x: Float,
        y: Float,
        z: Float
    ): VertexConsumer? {
        return consumer.vertex(x, y, z)
    }

    override fun color(
        red: Int,
        green: Int,
        blue: Int,
        alpha: Int
    ): VertexConsumer? {
        return consumer.color(red, green, blue, this.alpha)
    }

    override fun texture(u: Float, v: Float): VertexConsumer? {
        return consumer.texture(u, v)
    }

    override fun overlay(u: Int, v: Int): VertexConsumer? {
        return consumer.overlay(u, v)
    }

    override fun light(u: Int, v: Int): VertexConsumer? {
        return consumer.light(u, v)
    }

    override fun normal(
        x: Float,
        y: Float,
        z: Float
    ): VertexConsumer? {
        return consumer.normal(x, y, z)
    }
}