package cn.coostack.usefulmagic.items.weapon.wands

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.extend.ofFloored
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.extend.multiply
import cn.coostack.usefulmagic.formation.api.DefendCrystal
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.meteorite.impl.OptionMeteorite
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.StarryMeteoriteLocusEmitters
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionStarStyle
import cn.coostack.usefulmagic.particles.style.wand.starry.StarryMagicStyle
import cn.coostack.usefulmagic.particles.style.wand.starry.StarrySpellcasterMagicStyle
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.random.Random

/**
 * 群星法杖
 * 召唤群星
 */
class StarryWand(settings: Properties) : WandItem(settings, 1000, 50.0) {
    companion object {
        @JvmStatic
        fun getTargetLocation(user: Player): Vec3 {
            val vec = user.forward.normalize()
            val eyePos = user.eyePosition
            val range = 100
            val world = user.level()
            var currentPos = eyePos
            var count = 0
            while (count < range) {
                // 判断是否存在符合要求的点
                // 实体判断
                val box = AABB.ofSize(currentPos, 3.0, 3.0, 3.0)
                val entities = world.getEntitiesOfClass(LivingEntity::class.java, box) {
                    it.uuid != user.uuid
                }
                val entity = entities.minByOrNull { it.distanceTo(user) }

                if (entity != null) {
                    currentPos = entity.position()
                    break
                }
                val blockPos = ofFloored(currentPos)
                if (!world.shouldTickBlocksAt(blockPos)) {
                    currentPos = currentPos.add(0.0, 0.5, 0.0)
                    break
                }
                val block = world.getBlockState(blockPos)

                if (!block.isAir && !block.getCollisionShape(world, blockPos).isEmpty) {
                    currentPos = currentPos.add(0.0, 0.5, 0.0)
                    break
                }
                val cant = ServerFormationManager.getFormationFromPos(currentPos, world as ServerLevel)?.let {
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

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component?>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(
            Component.translatable("item.starry_wand.description")
        )
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }

    val options: Int
        get() = ParticleOption.getParticleCounts()
    val random = Random(System.currentTimeMillis())

    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 160
    }

    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        if (world.isClientSide) {
            return stack
        }
        if (user !is ServerPlayer) {
            return stack
        }
        world as ServerLevel

        // 寻找target
        val target = getTargetLocation(user)


        // 声音
        world.playSound(
            null,
            user.position().x, user.position().y, user.position().z,
            UsefulMagicSoundEvents.MAGIC_ACTIVATE.get(),
            SoundSource.PLAYERS,
            10.0f,
            2.0f
        )
        val random = Random(System.currentTimeMillis())
        val randomOffset = Vec3(
            random.nextDouble(-32.0, 32.0),
            100.0,
            random.nextDouble(-32.0, 32.0)
        )
        val randomSpawn = user.position().add(randomOffset)
        val style = StarryMagicStyle()
        val dir = randomSpawn.relativize(target)
        style.direction = dir
        style.player = user.uuid
        val maxAge = style.maxAge
        ParticleStyleManager.spawnStyle(world, randomSpawn, style)
        // 召唤弹幕
        val shape = PointsBuilder()
            .addRoundShape(40.0, 0.5, 18, 180)
        var tick = 0
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(3, maxAge) {
            tick += 3
            if (tick < 60) return@runTaskTimerMaxTick
            shape.rotateTo(style.direction)
            val create = shape.create()
            repeat(6) {
                val meteoritePos = randomSpawn.add(create.random().toVector())
                val emitters = StarryMeteoriteLocusEmitters(meteoritePos, world)
                    .apply {
                        templateData.also {
                            it.maxAge = 10
                            it.size = 0.5f
                            it.color = Math3DUtil.colorOf(255, 0, 0)
                        }
                    }
                ParticleEmittersManager.spawnEmitters(emitters)
                val optionMeteorite = OptionMeteorite(Blocks.MAGMA_BLOCK, 1) { loc ->
                    val box = AABB.ofSize(
                        loc, 8.0, 8.0, 8.0
                    )
                    val source = world.damageSources().playerAttack(user as Player)
                    world.getEntitiesOfClass(
                        LivingEntity::class.java, box
                    ) {
                        it != user
                    }.forEach {
                        it.hurt(source, damage.toFloat())
                        it.invulnerableTime = 0
                    }

                    val countDouble = ParticleOption.getParticleCounts()
                    val explosion = ExplodeMagicEmitters(loc, world).apply {
                        this.templateData.also {
                            it.color = Math3DUtil.colorOf(255, 170, 200)
                            it.size = 0.4f
                        }
                        randomParticleAgeMin = 10
                        randomParticleAgeMax = 15
                        precentDrag = 0.95
                        maxTick = 5
                        ballCountPow = countDouble * 15
                        minSpeed = 0.5
                        maxSpeed = 3.0
                        randomCountMin = 3 * countDouble
                        randomCountMax = 8 * countDouble
                    }
                    ParticleEmittersManager.spawnEmitters(explosion)

                    world.explode(
                        user,
                        source,
                        null,
                        loc,
                        8f,
                        false,
                        Level.ExplosionInteraction.TNT
                    )
                    emitters.cancelled = true
                }.withTick {
                    emitters.pos = this.origin
                }
                optionMeteorite.maxAge = 60
                optionMeteorite.direction = RelativeLocation.of(style.direction.normalize())
                optionMeteorite.speed = 70.0 / 20
                optionMeteorite.spawn(meteoritePos, world)
            }
        }
        // 扣除魔法
        cost(user)
        user.cooldowns.addCooldown(this, 60 * 20)
        return super.finishUsingItem(stack, world, user)
    }

    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        val tick = getUseDuration(stack, user) - remainingUseTicks
        val max = getUseDuration(stack, user)
        if (world.isClientSide) {
            return
        }
        if (tick > 40 && tick % 2 == 0) {
            repeat(4) {
                val style = ExplosionStarStyle(user.uuid)
                val r = random.nextDouble(5.0, 16.0)
                val p = PointsBuilder()
                    .addBall(r, 1)
                    .rotateAsAxis(random.nextDouble(-PI, PI))
                    .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
                    .create().random()
                ParticleStyleManager.spawnStyle(world, user.eyePosition.add(p.toVector()), style)
            }
        }

        if (tick % 10 == 0 && tick >= 40) {
            val randomPos = Vec3(
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0),
            ).multiply(random.nextDouble(-5.0, 5.0))
            val pos = user.position().add(randomPos)
            world.playSound(
                null,
                pos.x,
                pos.y,
                pos.z,
                UsefulMagicSoundEvents.STAR.get(),
                SoundSource.PLAYERS,
                3f,
                0.7f + random.nextFloat() * 0.6f
            )
        }

        if (tick % 3 == 0) {
            val rangeBall = PointsBuilder()
                .addBall(12.0, 6 * options)
                .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.yAxis())
                .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
                .create()
            repeat(if (tick < max / 2) 10 else 20) {
                val it = rangeBall.random()
                val pos = user.position().add(it.toVector())
                val dir = it.normalize().multiply(-0.6)
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.CLOUD, world as ServerLevel, pos, dir.toVector()
                )
            }
        }

        if (tick % 5 == 0) {
            world.playSound(
                null,
                user.x,
                user.y,
                user.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                3f,
                2f
            )
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?> {
        val res = super.use(world, user, hand)
        if (!res.result.consumesAction()) {
            return res
        }
        if (world.isClientSide) return res

        val style = StarrySpellcasterMagicStyle()
        style.player = user.uuid
        ParticleStyleManager.spawnStyle(world, user.position(), style)
        return super.use(world, user, hand)
    }

}