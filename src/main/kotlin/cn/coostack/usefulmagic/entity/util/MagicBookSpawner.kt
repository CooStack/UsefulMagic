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
import cn.coostack.usefulmagic.blocks.entitiy.MagicCoreBlockEntity
import cn.coostack.usefulmagic.entity.custom.MagicBookEntity
import cn.coostack.usefulmagic.particles.animation.EmitterAnimate
import cn.coostack.usefulmagic.particles.animation.EmittersAnimate
import cn.coostack.usefulmagic.particles.animation.ParticleAnimation
import cn.coostack.usefulmagic.particles.animation.StyleAnimate
import cn.coostack.usefulmagic.particles.emitters.DirectionShootEmitters
import cn.coostack.usefulmagic.particles.emitters.ParticleWaveEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionLineEmitters
import cn.coostack.usefulmagic.particles.style.EnchantLineStyle
import cn.coostack.usefulmagic.particles.style.entitiy.MagicBookSpawnStyle
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
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
        block.world!!.playSound(null, block.pos, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 3F, 2F)
        val emitter = DirectionShootEmitters(block.pos.up(8).toCenterPos(), block.world).apply {
            templateData.also { it ->
                it.maxAge = 40
                it.speed = 0.8
                it.color = Math3DUtil.colorOf(255, 100, 80)
            }
            this.shootDirection = Vec3d(0.0, 16.0, 0.0)
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
        return MagicBookEntity(block.world!!)
            .apply {
                this.setPosition(getSpawnLocation())
            }
    }

    override fun getSpawnLocation(): Vec3d {
        return block.pos.toCenterPos().add(0.0, 8.0, 0.0)
    }

    val random = Random(System.currentTimeMillis())
    override fun onStartSpawn() {
        val r = 9.0
        // 设置animation
        animation = ParticleAnimation()
            .addAnimate(
                StyleAnimate(MagicBookSpawnStyle(), block.world as ServerWorld, block.pos.down(3).toCenterPos(), -1)
            ).addAnimate(
                EmittersAnimate(
                    {
                        val p = PointsBuilder()
                            .addBall(r, 1)
                            .rotateAsAxis(random.nextDouble(-PI, PI))
                            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
                            .create().random()
                        val spawnPos = block.pos.up(8).toCenterPos().add(p.toVector())
                        ExplosionLineEmitters(spawnPos, block.world as ServerWorld)
                            .apply {
                                this.maxTick = 80
                                val targetPoint = block.pos.up(8).toCenterPos()
                                this.targetPoint = targetPoint
                                this.templateData.maxAge = 120
                                this.templateData.speed = 1.2
                                this.templateData.velocity = Vec3d(
                                    random.nextDouble(-5.0, 5.0),
                                    random.nextDouble(-5.0, 5.0),
                                    random.nextDouble(-5.0, 5.0),
                                )
                            }
                    }, block.pos.toCenterPos(), 5, getSpawnTicks() - 40
                ) {}
            ).addAnimate(
                EmitterAnimate(
                    PresetLaserEmitters(block.pos.down(3).toCenterPos(), block.world).apply {
                        targetPoint = Vec3d(0.0, 100.0, 0.0)
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
            block.world!!.playSound(null, block.pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 8F, 0.8F)
        }
        if (currentTick % 10 == 0) {
            ServerCameraUtil.sendShake(
                block.world as ServerWorld, block.pos.toCenterPos(), 64.0, 0.5, 20
            )
        }
        if (currentTick % 4 == 0 && currentTick > 20 * 10) {
            repeat(6) {
                val x = random.nextDouble(-20.0, 20.0)
                val y = random.nextDouble(-10.0, 10.0)
                val z = random.nextDouble(-20.0, 20.0)
                val pos = block.pos.toCenterPos().add(x, y, z)
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
                ParticleStyleManager.spawnStyle(block.world!!, pos, style)
            }
        }
        if (currentTick % 20 == 0 && currentTick > 20 * 20) {
            val wave = ParticleWaveEmitters(block.pos.toCenterPos(), block.world).apply {
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
            block.world!!.playSound(
                null,
                block.pos,
                SoundEvents.BLOCK_END_PORTAL_FRAME_FILL,
                SoundCategory.BLOCKS,
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
        block.world!!.playSound(
            null,
            block.pos,
            SoundEvents.BLOCK_BEACON_DEACTIVATE,
            SoundCategory.BLOCKS,
            8F,
            1F
        )
    }
}