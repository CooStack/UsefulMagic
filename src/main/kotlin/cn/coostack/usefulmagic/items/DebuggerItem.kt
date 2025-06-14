package cn.coostack.usefulmagic.items

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.PresetLaserEmitters
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.entitiy.AltarEntity
import cn.coostack.usefulmagic.blocks.entitiy.MagicCoreBlockEntity
import cn.coostack.usefulmagic.managers.ClientManaManager
import cn.coostack.usefulmagic.meteorite.impl.TestMeteorite
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionAnimateLaserMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionLineEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionWaveEmitters
import cn.coostack.usefulmagic.particles.style.LightStyle
import cn.coostack.usefulmagic.particles.style.TestStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionMagicBallStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionMagicStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionStarStyle
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class DebuggerItem : Item(Settings()) {
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack?>? {
        val res = super.use(world, user, hand)
        // 数学测试
//        testLight(world, user)
        // 陨石测试
//        testMeteorite(world, user, hand)
        // 魔力树形测试
//        testMana(world, user, hand)
        if (world.isClient) {
            return res
        }
//        testStar(world as ServerWorld, user as ServerPlayerEntity)
        world as ServerWorld
        user as ServerPlayerEntity
//        CooParticleAPI.scheduler.runTaskTimerMaxTick(5, 60) {
//            repeat(3) {
//        testExplosion(world, user)
        testMagic(world, user)
//        testExplosionMagicStyle(world, user, RelativeLocation.yAxis())
//            }
//        }
//        CooParticleAPI.scheduler.runTaskTimerMaxTick(120) {
//            repeat(2) {
//                testStar(world, user)
//            }
//        }
//        testExplosionMagicStyle(world, user)
//        testStar(world, user)
//        testImpact(world, user)

        return res
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult? {
        val block = context.blockPos
        val user = context.player ?: return ActionResult.PASS
        val world = context.world
        testBlock(world, user, block)
        return super.useOnBlock(context)
    }

    val random = Random(System.currentTimeMillis())


    fun testMagic(world: ServerWorld, user: ServerPlayerEntity) {
        CooParticleAPI.scheduler.runTaskTimerMaxTick(5, 240) {
            repeat(2) {
                testEmitters(world, user)
            }
        }
        testExplosionMagicBallStyle(world, user)
        val scheduler = CooParticleAPI.scheduler
        scheduler.runTask(120) {
            CooParticleAPI.scheduler.runTaskTimerMaxTick(120) {
                repeat(2) {
                    testStar(world, user)
                }
            }
        }

        scheduler.runTask(240) {
            testExplosionMagicStyle(world, user, RelativeLocation.yAxis())
            scheduler.runTask(120) {
                testImpact(world, user, Vec3d(0.0, 100.0, 0.0))
            }
        }

    }

    fun testExplosionMagicBallStyle(world: ServerWorld, user: ServerPlayerEntity) {
        val style = ExplosionMagicBallStyle(user.uuid)
        ParticleStyleManager.spawnStyle(world, user.pos, style)
    }

    fun testExplosionMagicStyle(world: ServerWorld, user: ServerPlayerEntity, rotate: RelativeLocation) {
        val style = ExplosionMagicStyle()
        style.rotateDirection = rotate
        ParticleStyleManager.spawnStyle(world, user.pos, style)
    }

    fun testImpact(world: ServerWorld, user: ServerPlayerEntity, toPoint: Vec3d) {
        val emitters = PresetLaserEmitters(user.eyePos, world)
        emitters.targetPoint = toPoint
        emitters.apply {
            lineStartScale = 1f
            lineScaleMin = 0.01f
            lineScaleMax = 5f
            particleCountPreBlock = 1
            lineStartIncreaseTick = 10
            lineStartDecreaseTick = 120
            increaseAcceleration = 0.01f
            defaultIncreaseSpeed = 0.1f
            defaultDecreaseSpeed = 0.2f
            decreaseAcceleration = 0.3f
            maxDecreaseSpeed = 3f
            lineMaxTick = 260
            markDeadWhenArriveMinScale = true
            particleAge = lineMaxTick / 6 + 1
            templateData.color = Math3DUtil.colorOf(255, 100, 100)
        }
        ParticleEmittersManager.spawnEmitters(emitters)
    }

    fun testStar(world: ServerWorld, user: ServerPlayerEntity) {
        val style = ExplosionStarStyle(user.uuid)
        val r = random.nextDouble(2.0, 5.0)
        val p = PointsBuilder()
            .addBall(r, 1)
            .rotateAsAxis(random.nextDouble(-PI, PI))
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
            .create().random()
        ParticleStyleManager.spawnStyle(world, user.eyePos.add(p.toVector()), style)
    }

    fun testEmitters(world: ServerWorld, user: ServerPlayerEntity) {
        val r = 45.0
        val p = PointsBuilder()
            .addBall(r, 1)
            .rotateAsAxis(random.nextDouble(-PI, PI))
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
            .create().random()
        val spawnPos = user.eyePos.add(p.toVector())
        val emitters = ExplosionLineEmitters(spawnPos, world)
//        val k = -1 / tan(theta)
//        val vAngle = atan(k)
//        val vx = cos(vAngle)
//        val vz = sin(vAngle)
//        emitters.emittersVelocity = Vec3d(vx, 0.0, vz)
//        emitters.maxTick = 120
        val targetPoint = user.eyePos.add(user.rotationVector.normalize().multiply(5.0))
        emitters.targetPoint = targetPoint
        emitters.templateData.maxAge = 120
        emitters.templateData.speed = 1.2
        emitters.templateData.velocity = Vec3d(
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
        )
        ParticleEmittersManager.spawnEmitters(emitters)
        CooParticleAPI.scheduler.runTaskTimerMaxTick(1, 160) {
            emitters.targetPoint = user.eyePos.add(user.rotationVector.normalize().multiply(3.0))
        }
    }

    fun testBlock(world: World, user: PlayerEntity, block: BlockPos) {
        val entity = world.getBlockEntity(block) ?: return
        if (!world.isClient) {
            return
        }
        if (entity is AltarEntity) {
            user.sendMessage(
                Text.of(
                    """
                        祭坛方块属性: 
                        获取到的魔力最大值${entity.getDownActiveBlocksMaxMana()}
                        获取到的魔力恢复速度${entity.getDownActiveBlocksManaReviveSpeed()}
                        祭坛物品:${entity.getAltarStack()}
                    """.trimIndent()
                )
            )
        }

        if (entity is MagicCoreBlockEntity) {
            user.sendMessage(
                Text.of(
                    """
                        祭坛核心方块属性: 
                        当前魔力值${entity.currentMana}
                        获取到的魔力最大值${entity.maxMana}
                        获取到的魔力恢复速度${entity.currentReviveSpeed}
                        合成进度${entity.craftingTick}
                        是否正在合成${entity.crafting}
                    """.trimIndent()
                )
            )
        }

    }

    fun testLight(world: World, user: PlayerEntity) {
        if (world.isClient) return
        val style = LightStyle(
            Vec3d(210.0, 120.0, 200.0), 40.0, 0.4f, 2f, 1f, 120
        )
        ParticleStyleManager.spawnStyle(world as ServerWorld, user.pos, style)
    }

    fun testMath(world: World, user: PlayerEntity) {
        if (world.isClient) return
        user as ServerPlayerEntity
        world as ServerWorld
        val random = Random(System.currentTimeMillis())
        val randomDirection = RelativeLocation(
            0.0,
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
        )
        randomDirection.multiply(3.0 / randomDirection.length())
        val style = TestStyle(user.uuid)
        ParticleStyleManager.spawnStyle(world, user.eyePos, style)
    }

    fun testMeteorite(world: World, user: PlayerEntity, hand: Hand) {
        if (world.isClient) {
            return
        }
        user as ServerPlayerEntity
        val random = Random(System.currentTimeMillis())
        CooParticleAPI.scheduler.runTaskTimerMaxTick(5, 60) {
            val origin = user.eyePos.add(random.nextDouble(-16.0, 16.0), 50.0, random.nextDouble(-16.0, 16.0))
            val meteorite = TestMeteorite().apply {
                shooter = user
                direction = RelativeLocation.yAxis().multiply(-1)
            }
            meteorite.spawn(origin, world as ServerWorld)
        }
    }

    fun testMana(world: World, user: PlayerEntity, hand: Hand) {
        if (!world.isClient) {
            val data = UsefulMagic.state.getDataFromServer(user.uuid)
            user.sendMessage(
                Text.of(
                    """
                    玩家 ${user.name.string} 的魔力属性值
                    魔力: ${data.mana}
                    最大魔力值: ${data.maxMana}
                    魔力恢复/秒 :${data.manaRegeneration}
                    当前视图 服务器
                """.trimIndent()
                )
            )
            UsefulMagic.state.playerManaData.forEach {
                val player = world.server!!.playerManager.getPlayer(it.key) ?: return@forEach
                val data = it.value
                user.sendMessage(
                    Text.of(
                        """
                    玩家 ${player.name.string} 的魔力属性值
                    魔力: ${data.mana}
                    最大魔力值: ${data.maxMana}
                    魔力恢复/秒 :${data.manaRegeneration}
                    当前视图 服务器 - 其余玩家
                """.trimIndent()
                    )
                )
            }
            return
        }
        val data = ClientManaManager.getSelfMana()
        user.sendMessage(
            Text.of(
                """
                    玩家 ${user.name.string} 的魔力属性值
                    魔力: ${data.mana}
                    最大魔力值: ${data.maxMana}
                    魔力恢复/秒 :${data.manaRegeneration}
                    当前视图 客户端
                """.trimIndent()
            )
        )
    }

}