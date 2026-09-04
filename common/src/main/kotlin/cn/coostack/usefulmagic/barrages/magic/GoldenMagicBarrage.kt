package cn.coostack.usefulmagic.barrages.magic

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableFallingDustEffect
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.display.magic.attack.GoldenMagicBarrageDisplay
import cn.coostack.usefulmagic.particles.emitters.magic.BarrageTailEmitter
import cn.coostack.usefulmagic.particles.emitters.magic.BlockFragmentEmitters
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*

class GoldenMagicBarrage(loc: Vec3, world: ServerLevel, damage: Double, shooter: LivingEntity) :
    cn.coostack.usefulmagic.barrages.api.EntityMagicDamagedBarrage(
        loc, world,
        BarrageOption()
            .enableSpeedWithOptions(0.0)
            .enableAccelerationWithOptions(0.03)
            .accelerationMaxSpeed(3.0)
            .maxLivingTick(200), damage, shooter
    ) {
    // 弹幕拖尾粒子发射器
    val tailEmitter = BarrageTailEmitter(loc, world).apply {
        left = Math3DUtil.colorOf(230, 230, 170)
        template.setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
    }
    var gen = false

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return livingEntity.uuid != shooter?.uuid && FriendFilterHelper.filterNotFriend(shooter!!, livingEntity)
    }

    override fun createHitBox(): HitBox {
        return HitBox.of(1.0, 1.0, 1.0)
    }

    override fun createControler(): ServerControler<*> {
        return GoldenMagicBarrageDisplay(loc, world).apply {
            scale = 0.5f
            prevScale = 0.5f
        }
    }

    override fun onHitDamaged(result: BarrageHitResult) {
        // 移除拖尾粒子
        tailEmitter.remove()
        result.entities.forEach {
            it.invulnerableTime = 0
        }
        world.playSound(null, BlockPos.containing(loc), SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.PLAYERS, 5f, 1f)
        // 生成金块碎片粒子
        val emitter = BlockFragmentEmitters(loc, world)
            .apply {
                template.apply {
                    effect = ControlableFallingDustEffect(
                        uuid, Blocks.GOLD_BLOCK.defaultBlockState()
                    )
                }
                maxTick = 1
            }
        ParticleEmittersManager.spawnEmitters(emitter)
    }

    override fun tick() {
        if (!gen) {
            gen = true
            ParticleEmittersManager.spawnEmitters(tailEmitter)
        }
        super.tick()
        tailEmitter.pos = loc
        bindControl.get().rotateToPoint(direction.asRelative())
    }
}

