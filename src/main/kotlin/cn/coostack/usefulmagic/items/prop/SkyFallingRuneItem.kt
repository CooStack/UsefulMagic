package cn.coostack.usefulmagic.items.prop

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.PresetLaserEmitters
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.formation.api.DefendCrystal
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionAnimateLaserMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionWaveEmitters
import cn.coostack.usefulmagic.particles.fall.style.GuildCircleStyle
import cn.coostack.usefulmagic.particles.fall.style.SkyFallingStyle
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.ExplosionUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion
import java.util.*

/**
 * 天空坠落魔法
 *
 * 消耗品
 * 使用时会在视线前方(最多)100 方块进行爆破
 * 展开时会周围(r <= 10) 的实体进行缓速
 */
class SkyFallingRuneItem : Item(Settings().maxCount(16)) {
    companion object {
        private val playerGuildStyles = HashMap<UUID, GuildCircleStyle>()
        const val knockbackHitDamage = 100f
        const val hitDamage = 200f

        @JvmStatic
        fun getTargetLocation(user: PlayerEntity): Vec3d {
            val vec = user.rotationVector.normalize()
            val eyePos = user.eyePos
            val range = 100
            val world = user.world
            var currentPos = eyePos
            var count = 0
            while (count < range) {
                // 判断是否存在符合要求的点
                // 实体判断
                val box = Box.of(currentPos, 3.0, 3.0, 3.0)
                val entities = world.getEntitiesByClass(LivingEntity::class.java, box) {
                    it.uuid != user.uuid
                }
                val entity = entities.minByOrNull { it.distanceTo(user) }

                if (entity != null) {
                    currentPos = entity.pos
                    break
                }
                val blockPos = BlockPos.ofFloored(currentPos)
                if (!world.shouldTickBlockPos(blockPos)) {
                    currentPos = currentPos.add(0.0, 0.5, 0.0)
                    break
                }
                val block = world.getBlockState(blockPos)

                if (!block.isAir && !block.getCollisionShape(world, blockPos).isEmpty) {
                    currentPos = currentPos.add(0.0, 0.5, 0.0)
                    break
                }
                val cant = ServerFormationManager.getFormationFromPos(currentPos, world as ServerWorld)?.let {
                    val option = LivingEntityTargetOption(user, false)
                    it.hasCrystalType(DefendCrystal::class.java) && !it.isFriendly(option)
                } ?: false
                if (cant) break
                currentPos = currentPos.add(vec)
                count++
            }
            return currentPos
        }
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(Text.of("§7花费巨大的代价,迅速击溃敌人"))
        tooltip.add(Text.of("§7超位魔法-天空坠落"))
        tooltip.add(Text.of("§f右键使用"))
        tooltip.add(Text.of("§7消耗品"))
        tooltip.add(Text.of("§7不消耗魔力值"))
        super.appendTooltip(stack, context, tooltip, type)
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (!selected) return
        if (world.isClient) return
        if (entity !is PlayerEntity) return
        val world = world as ServerWorld
        val entity = entity as ServerPlayerEntity

        var style = playerGuildStyles.getOrPut(entity.uuid) {
            GuildCircleStyle()
        }
        style.bindPlayer = entity.uuid
        if (!style.valid) {
            style = GuildCircleStyle()
            playerGuildStyles[entity.uuid] = style
            ParticleStyleManager.spawnStyle(world, getTargetLocation(entity), style)
            return
        }
        if (!style.displayed) {
            ParticleStyleManager.spawnStyle(world, getTargetLocation(entity), style)
            return
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack?> {
        user.itemCooldownManager.set(this, 20 * 60 * 20)
        val stack = user.getStackInHand(hand)
        if (!user.isCreative) {
            stack.decrement(1)
        }
        if (world.isClient) return super.use(world, user, hand)
        world.playSound(
            null, user.x, user.y, user.z,
            UsefulMagicSoundEvents.SKY_FALLING_MAGIC_START,
            SoundCategory.PLAYERS,
            10f, 1f
        )
        val world = world as ServerWorld
        val style = SkyFallingStyle()
        style.bindPlayer = user.uuid
        ParticleStyleManager.spawnStyle(world, user.pos, style)
        // 设置target
        val target = getTargetLocation(user)
        CooParticleAPI.scheduler.runTask(12 * 20) {
            handleExplodeParticle(world, user as ServerPlayerEntity, target)
            handleExplode(world, user, target)
        }

        val data = UsefulMagic.state.getDataFromServer(user.uuid)
        CooParticleAPI.scheduler.runTaskTimerMaxTick(1, 12 * 20) {
            val r = 12.0
            val box = Box.of(target, r * 2, r * 2, r * 2)
            val entities = world.getEntitiesByClass(LivingEntity::class.java, box) {
                !data.isFriend(it.uuid) && it.uuid != user.uuid
            }
            entities.forEach {
                it.addStatusEffect(
                    StatusEffectInstance(
                        StatusEffects.SLOWNESS, 20, 10
                    )
                )
                it.velocity = Vec3d.ZERO
                it.velocityModified = true
                it.movementSpeed = 0f
            }
            user.velocity = Vec3d.ZERO
            user.velocityModified = true
            user.movementSpeed = 0f
            user.abilities.allowFlying = true
            user.abilities.flying = true
        }.setFinishCallback {
            user.abilities.allowFlying = true
            user.abilities.flying = true
        }
        return super.use(world, user, hand)
    }


    private fun handleExplode(world: ServerWorld, user: ServerPlayerEntity, target: Vec3d) {
        // 有效攻击范围  r <= 24
        // 攻击性击退范围 r <= 36
        // 击退范围 r <= 48
        val r = 48.0
        val data = UsefulMagic.state.getDataFromServer(user.uuid)
        val entities = world.getEntitiesByClass(LivingEntity::class.java, Box.of(target, r * 2, r * 2, r * 2)) {
            !data.isFriend(it.uuid) && it.uuid != user.uuid
        }

        val knockbackEntity = entities.filter {
            val d = it.pos.distanceTo(target)
            d > 36.0 && d <= r
        }

        val knockbackAndHitEntity = entities.filter {
            val d = it.pos.distanceTo(target)
            d <= 36.0 && d > 24.0
        }

        val hitEntity = entities.filter {
            val d = it.pos.distanceTo(target)
            d <= 24.0
        }
        // 第一轮的伤害
        knockbackEntity.forEach {
            val dir = target.relativize(it.pos).normalize().multiply(3.0)
            // 3.0
            it.velocity = dir
            it.velocityModified = true
        }

        knockbackAndHitEntity.forEach {
            val dir = target.relativize(it.pos).normalize().multiply(2.0)
            // 4.0
            it.velocity = dir
            it.velocityModified = true
            val source = world.damageSources.playerAttack(user)
            it.damage(source, knockbackHitDamage)
        }
        hitEntity.forEach {
            val dir = target.relativize(it.pos).normalize().multiply(0.5)
            // 5.0
            it.velocity = dir
            it.velocityModified = true
            val source = world.damageSources.playerAttack(user)
            it.damage(source, 2048f)
        }

        val formation = ServerFormationManager.getFormationFromPos(target, world)
        formation?.attack(128f, LivingEntityTargetOption(user), target)
        var currentRadius = 1
        CooParticleAPI.scheduler.runTaskTimerMaxTick(5, 5 * 20) {
            ExplosionUtil.createHollowSphereExplosion(
                currentRadius++,
                world,
                target,
                user
            )
            ExplosionUtil.createHollowSphereExplosion(
                currentRadius++,
                world,
                target,
                user
            )
            formation?.attack(64f, LivingEntityTargetOption(user), target)
            world.getEntitiesByClass(LivingEntity::class.java, Box.of(target, 24.0, 24.0, 24.0)) {
                !data.isFriend(it.uuid) && it.uuid != user.uuid
            }.forEach {
                val playerAttack = it.damageSources.playerAttack(user)
                it.damage(playerAttack, hitDamage / 2)
                it.timeUntilRegen = 0
                it.hurtTime = 0
            }
        }
    }

    private fun handleExplodeParticle(world: ServerWorld, user: ServerPlayerEntity, target: Vec3d) {
        val line = PresetLaserEmitters(target, world).apply {
            targetPoint = Vec3d(0.0, 200.0, 0.0)
            lineStartScale = 1f
            lineScaleMin = 0.01f
            lineScaleMax = 50f
            particleCountPreBlock = 1
            lineStartIncreaseTick = 1
            lineStartDecreaseTick = 140
            increaseAcceleration = 0.5f
            defaultIncreaseSpeed = 1f
            defaultDecreaseSpeed = 0.2f
            decreaseAcceleration = 0.5f
            maxDecreaseSpeed = 3f
            lineMaxTick = 180
            markDeadWhenArriveMinScale = true
            particleAge = lineMaxTick / 6 + 1
            templateData.color = Math3DUtil.colorOf(120, 200, 200)
        }
        ParticleEmittersManager.spawnEmitters(line)
        // 爆炸粒子
        CooParticleAPI.scheduler.runTaskTimerMaxTick(
            10, 180
        ) {
            ServerCameraUtil.sendShake(world, target, 128.0, 2.0, 40)
        }
        val explosion = ExplodeMagicEmitters(target.add(0.0, 5.0, 0.0), world).apply {
            this.templateData.also {
                it.size = 0.4f
            }
            randomParticleAgeMin = 60
            randomParticleAgeMax = 120
            precentDrag = 0.95
            maxTick = 4
            ballCountPow = 3 * 15
            minSpeed = 10.0
            maxSpeed = 40.0
            randomCountMin = 120 * 3
            randomCountMax = 400 * 3
            wind.direction = Vec3d(0.0, 0.8, 0.0)
        }
        ParticleEmittersManager.spawnEmitters(explosion)

        val explosionAnimate = ExplosionAnimateLaserMagicEmitters(target, world)
            .apply {
                maxTick = 2
                heightStep = 5.0
                minDiscrete = 5.0
                maxDiscrete = 15.0
                radiusStep = 2.0
                maxRadius = 20.0
                minCount = 20
                maxCount = 60
                templateData.maxAge = 80
                templateData.size = 0.3f
                templateData.effect = ControlableCloudEffect(templateData.uuid)
            }
        ParticleEmittersManager.spawnEmitters(explosionAnimate)

        fun genWave(yOffset: Double, speed: Double, drag: Double, size: Double): ExplosionWaveEmitters {
            return ExplosionWaveEmitters(target.add(0.0, yOffset, 0.0), world)
                .apply {
                    maxTick = 1
                    waveSpeed = speed
                    waveSize = size
                    waveCircleCountMax = 660
                    waveCircleCountMin = 120
                    speedDrag = drag
                    this.templateData.also {
                        it.effect = ControlableCloudEffect(it.uuid)
                        it.size = 1f
                        it.maxAge = 140
                        it.velocity
                    }
                    discrete = 0.1
                    randomVector = true
                    randomSpeed = 0.02
                }
        }

        // 冲击波云
        ParticleEmittersManager.spawnEmitters(genWave(20.0, 6.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(40.0, 10.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(60.0, 15.5, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(80.0, 19.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(90.0, 10.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(110.0, 18.0, 0.85, 1.0))


        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.BLOCK_BEACON_ACTIVATE,
            SoundCategory.PLAYERS,
            10f,
            2f
        )
        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.ENTITY_GENERIC_EXPLODE,
            SoundCategory.PLAYERS,
            10f,
            1.5f
        )
    }

}