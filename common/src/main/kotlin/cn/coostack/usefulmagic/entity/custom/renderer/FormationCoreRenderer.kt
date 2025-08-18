package cn.coostack.usefulmagic.entity.custom.renderer

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.custom.formation.FormationCoreEntity
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f
import org.lwjgl.opengl.GL33

class FormationCoreRenderer(ctx: EntityRendererProvider.Context) : EntityRenderer<FormationCoreEntity>(ctx) {


    override fun getTextureLocation(p0: FormationCoreEntity): ResourceLocation? {
        return null
    }
}