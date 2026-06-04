package cn.coostack.usefulmagic.entity.custom.model

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import net.minecraft.resources.ResourceLocation
import software.bernie.geckolib.animation.AnimationState
import software.bernie.geckolib.model.GeoModel

/**
 * `GeoModel` 只负责告诉 GeckoLib 三件事：
 * 1. 模型文件在哪
 * 2. 贴图文件在哪
 * 3. 动画文件在哪
 *
 * 如果你后面要换模型资源，只改这三个返回值即可。
 * 代码侧真正切动作的入口不在这里，而是在 `MagicDragonEntity.playSkillAnimation`
 * 和 `MagicDragonSkillAnimationState`。
 */
class MagicDragonModel : GeoModel<MagicDragonEntity>() {
    companion object {
        private var loggedAnimationSamples = 0
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getModelResource(animatable: MagicDragonEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(
            UsefulMagic.MOD_ID,
            "geo/entity/magic_dragon.geo.json"
        )
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getTextureResource(animatable: MagicDragonEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(
            UsefulMagic.MOD_ID,
            "textures/entity/magic_dragon.png"
        )
    }

    override fun getAnimationResource(animatable: MagicDragonEntity): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(
            UsefulMagic.MOD_ID,
            "animations/entity/magic_dragon.animation.json"
        )
    }

    override fun setCustomAnimations(
        animatable: MagicDragonEntity,
        instanceId: Long,
        animationState: AnimationState<MagicDragonEntity>
    ) {
        super.setCustomAnimations(animatable, instanceId, animationState)
        if (loggedAnimationSamples >= 8) {
            return
        }

        val rightWing = getBone("chibangyou").orElse(null)
        val leftWing = getBone("chibangzuo").orElse(null)
        val root = getBone("Dragon").orElse(null)
        UsefulMagic.logger.info(
            "MagicDragon geo sample #{}: entity={}, tick={}, rightWingRotZ={}, leftWingRotZ={}, rootPosY={}, rootRotX={}",
            loggedAnimationSamples + 1,
            animatable.id,
            "%.3f".format(animationState.animationTick),
            rightWing?.rotZ,
            leftWing?.rotZ,
            root?.posY,
            root?.rotX
        )
        loggedAnimationSamples++
    }
}
