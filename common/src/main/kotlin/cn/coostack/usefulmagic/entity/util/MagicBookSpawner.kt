package cn.coostack.usefulmagic.entity.util

import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.PhysicConstant
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.PresetLaserEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntity
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.extend.isOf
import cn.coostack.usefulmagic.particles.animation.EmitterAnimate
import cn.coostack.usefulmagic.particles.animation.EmittersAnimate
import cn.coostack.usefulmagic.particles.animation.ParticleAnimation
import cn.coostack.usefulmagic.particles.animation.StyleAnimate
import cn.coostack.usefulmagic.particles.emitters.DirectionShootEmitters
import cn.coostack.usefulmagic.particles.emitters.ParticleWaveEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionLineEmitters
import cn.coostack.usefulmagic.particles.style.EnchantLineStyle
import cn.coostack.usefulmagic.particles.style.entitiy.MagicBookSpawnStyle
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.random.Random

class MagicBookSpawner(val block: MagicCoreBlockEntity) : MobSpawner() {
    var animation = ParticleAnimation()
    override fun spawnCondition(): Boolean {
        val manaNeed = block.currentMana >= 30000
        val notCrafting = !block.crafting
        val centerEntity = block.getCenterAltarBlockEntity() ?: return false
        val centerBook = centerEntity.stack.isOf(Items.BOOK)
        return centerBook && manaNeed && notCrafting
    }

    override fun getSpawnTicks(): Int {
        return 20 * 30
    }


    override fun onSpawn(entity: LivingEntity) {
        entity as MagicBookEntity
        val centerEntity = block.getCenterAltarBlockEntity() ?: return
        centerEntity.setAltarStack(ItemStack.EMPTY)
        block.currentMana -= 30000
        animation.cancel()
        // 播放音效
        block.level!!.playSound(null, block.blockPos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 3F, 2F)
        val emitter = DirectionShootEmitters(block.blockPos.above(8).center, block.level).apply {
            templateData.also { it ->
                it.maxAge = 40
                it.speed = 0.8
                it.color = Math3DUtil.colorOf(255, 100, 80)
            }
            this.shootDirection = Vec3(0.0, 16.0, 0.0)
            count = 240
            randomX = 4.0
            randomY = 4.0
            randomZ = 4.0
            randomSpeedOffset = 0.3
            gravity = PhysicConstant.EARTH_GRAVITY
            shootType = EmittersShootTypes.box(HitBox.of(16.0, 16.0, 16.0))
            maxTick = 3
        }
        ParticleEmittersManager.spawnEmitters(emitter)
    }

    override fun getSpawnedEntity(): LivingEntity {
        return MagicBookEntity(block.level!!)
            .apply {
                this.setPos(getSpawnLocation())
            }
    }

    override fun getSpawnLocation(): Vec3 {
        return block.blockPos.above(8).center
    }

    val random = Random(System.currentTimeMillis())
    override fun onStartSpawn() {
        val r = 9.0
        // 设置animation
        animation = ParticleAnimation()
            .addAnimate(
                StyleAnimate(MagicBookSpawnStyle(), block.level as ServerLevel, block.blockPos.below(3).center, -1)
            ).addAnimate(
                EmittersAnimate(
                    {
                        val p = PointsBuilder()
                            .addBall(r, 1)
                            .rotateAsAxis(random.nextDouble(-PI, PI))
                            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
                            .create().random()
                        val spawnPos = block.blockPos.above(8).center.add(p.toVector())
                        ExplosionLineEmitters(spawnPos, block.level as ServerLevel)
                            .apply {
                                this.maxTick = 80
                                val targetPoint = block.blockPos.above(8).center
                                this.targetPoint = targetPoint
                                this.templateData.maxAge = 120
                                this.templateData.speed = 1.2
                                this.templateData.velocity = Vec3(
                                    random.nextDouble(-5.0, 5.0),
                                    random.nextDouble(-5.0, 5.0),
                                    random.nextDouble(-5.0, 5.0),
                                )
                            }
                    }, block.blockPos.center, 5, getSpawnTicks() - 40
                ) {}
            ).addAnimate(
                EmitterAnimate(
                    PresetLaserEmitters(block.blockPos.below(3).center, block.level).apply {
                        targetPoint = Vec3(0.0, 100.0, 0.0)
                        lineStartScale = 1f
                        lineScaleMin = 0.01f
                        lineScaleMax = 5f
                        particleCountPreBlock = 1
                        lineStartIncreaseTick = 1
                        lineStartDecreaseTick = 15
                        increaseAcceleration = 0.01f
                        defaultIncreaseSpeed = 0.1f
                        defaultDecreaseSpeed = 0.2f
                        decreaseAcceleration = 0.3f
                        maxDecreaseSpeed = 3f
                        lineMaxTick = 100
                        markDeadWhenArriveMinScale = true
                        particleAge = lineMaxTick / 6 + 1
                        templateData.color = Math3DUtil.colorOf(255, 100, 100)
                    }, 100
                )
            )
    }

    override fun doSpawnTick() {
        animation.doTick()
        if (currentTick % 2 == 0) {
            block.level!!.playSound(null, block.blockPos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 8F, 0.8F)
        }
        if (currentTick % 10 == 0) {
            ServerCameraUtil.sendShake(
                block.level as ServerLevel, block.blockPos.center, 64.0, 0.5, 20
            )
        }
        if (currentTick % 4 == 0 && currentTick > 20 * 10) {
            repeat(6) {
                val x = random.nextDouble(-20.0, 20.0)
                val y = random.nextDouble(-10.0, 10.0)
                val z = random.nextDouble(-20.0, 20.0)
                val pos = block.blockPos.center.add(x, y, z)
                val line = RelativeLocation(0.0, random.nextDouble(2.0, 4.0), 0.0)
                val count = (line.length() * 2).roundToInt()
                val style = EnchantLineStyle(line, count, random.nextInt(40, 60))
                style.apply {
                    particleRandomAgePreTick = true
                    fade = true
                    fadeInTick = 30
                    fadeOutTick = 30
                    speedDirection = RelativeLocation(0.0, random.nextDouble(-0.1, 0.1), 0.0)
                }
                ParticleStyleManager.spawnStyle(block.level!!, pos, style)
            }
        }
        if (currentTick % 20 == 0 && currentTick > 20 * 20) {
            val wave = ParticleWaveEmitters(block.blockPos.center, block.level).apply {
                templateData.also { it ->
                    it.maxAge = 30
                    it.effect = ControlableCloudEffect(it.uuid)
                }
                waveCircleCountMin = 120
                waveCircleCountMax = 240
                waveSize = 0.1
                waveSpeed = 3.0
                maxTick = 1
            }
            ParticleEmittersManager.spawnEmitters(wave)
            block.level!!.playSound(
                null,
                block.blockPos,
                SoundEvents.END_PORTAL_FRAME_FILL,
                SoundSource.BLOCKS,
                8F,
                1F
            )
        }
        if (animation.currentIndex < animation.animations.size) {
            if (currentTick == 1 || currentTick == 40 || currentTick == getSpawnTicks() - 20) {
                animation.spawnSingle()
            }
        }
    }

    override fun onCancelSpawn() {
        animation.cancel()
        // 其他爆炸
        block.level!!.playSound(
            null,
            block.blockPos,
            SoundEvents.BEACON_DEACTIVATE,
            SoundSource.BLOCKS,
            8F,
            1F
        )
    }
}