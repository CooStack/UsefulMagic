package cn.coostack.usefulmagic.items

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.PresetLaserEmitters
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.entity.AltarEntity
import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.EnergyCrystalsBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.managers.client.ClientManaManager
import cn.coostack.usefulmagic.meteorite.impl.TestMeteorite
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionLineEmitters
import cn.coostack.usefulmagic.particles.style.LightStyle
import cn.coostack.usefulmagic.particles.style.TestStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionMagicBallStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionMagicStyle
import cn.coostack.usefulmagic.particles.style.explosion.ExplosionStarStyle
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import kotlin.math.PI
import kotlin.random.Random

class DebuggerItem : Item(Properties()) {
    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?>? {
        val res = super.use(world, user, hand)
        // 数学测试
//        testLight(world, user)
        // 陨石测试
//        testMeteorite(world, user, hand)
        // 魔力树形测试
//        testMana(world, user, hand)
        if (world.isClientSide) {
            return res
        }
//        testStar(world as ServerLevel, user as ServerPlayer)
        world as ServerLevel
        user as ServerPlayer
//        testShader(world, user)
//        CooParticlesAPI.scheduler.runTaskTimerMaxTick(5, 60) {
//            repeat(3) {
//        testExplosion(world, user)
//        testMagic(world, user)
//        testExplosionMagicStyle(world, user, RelativeLocation.yAxis())
//            }
//        }
//        CooParticlesAPI.scheduler.runTaskTimerMaxTick(120) {
//            repeat(2) {
//                testStar(world, user)
//            }
//        }
//        testExplosionMagicStyle(world, user)
//        testStar(world, user)
//        testImpact(world, user)

        return res
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val block = context.clickedPos
        val user = context.player ?: return InteractionResult.PASS
        val world = context.level
        if (world.isClientSide) return InteractionResult.PASS
        testFormation(world as ServerLevel, user as ServerPlayer, block)
        return super.useOn(context)
    }


    val random = Random(System.currentTimeMillis())


    fun testFormation(world: ServerLevel, user: ServerPlayer, block: BlockPos) {
        val entity = world.getBlockEntity(block) ?: return
        when (entity) {
            is FormationCoreBlockEntity -> testFormationCore(world, user, entity)
            is EnergyCrystalsBlockEntity -> testFormationEnergy(world, user, entity)
        }

    }

    private fun testFormationEnergy(
        world: ServerLevel,
        user: ServerPlayer,
        entity: EnergyCrystalsBlockEntity
    ) {
        user.sendSystemMessage(
            Component.literal(
                """
                    能量水晶信息
                    蕴含能量: ${entity.currentMana}
                    最大能量: ${entity.maxMana}
                """.trimIndent()
            )
        )
    }


    fun testFormationCore(world: ServerLevel, user: ServerPlayer, entity: FormationCoreBlockEntity) {
        user.sendSystemMessage(
            Component.literal(
                """
                    核心方块信息
                    阵法规模: ${entity.formation.scale.name}
                    阵法生命值: ${entity.formation.formationHealth}
                    阵法是否激活 ${entity.formation.isActiveFormation()}
                    阵法是否可激活 ${entity.formation.canBeFormation()}
                    阵法中水晶个数 ${entity.formation.activeCrystals.size}
                    阵法激活主人${
                    if (entity.formation.owner == null) "" else {
                        world.server.playerList.getPlayer(entity.formation.owner)?.name
                    }
                }
                    阵法范围: ${entity.formation.getFormationTriggerRange()}
                  
                """.trimIndent()
            )
        )
    }

    fun testMagic(world: ServerLevel, user: ServerPlayer) {
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(5, 240) {
            repeat(2) {
                testEmitters(world, user)
            }
        }
        testExplosionMagicBallStyle(world, user)
        val scheduler = CooParticlesAPI.scheduler
        scheduler.runTask(120) {
            CooParticlesAPI.scheduler.runTaskTimerMaxTick(120) {
                repeat(2) {
                    testStar(world, user)
                }
            }
        }

        scheduler.runTask(240) {
            testExplosionMagicStyle(world, user, RelativeLocation.yAxis())
            scheduler.runTask(120) {
                testImpact(world, user, Vec3(0.0, 100.0, 0.0))
            }
        }

    }

    fun testExplosionMagicBallStyle(world: ServerLevel, user: ServerPlayer) {
        val style = ExplosionMagicBallStyle(user.uuid)
        ParticleStyleManager.spawnStyle(world, user.position(), style)
    }

    fun testExplosionMagicStyle(world: ServerLevel, user: ServerPlayer, rotate: RelativeLocation) {
        val style = ExplosionMagicStyle()
        style.rotateDirection = rotate
        ParticleStyleManager.spawnStyle(world, user.position(), style)
    }

    fun testImpact(world: ServerLevel, user: ServerPlayer, toPoint: Vec3) {
        val emitters = PresetLaserEmitters(user.eyePosition, world)
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

    fun testStar(world: ServerLevel, user: ServerPlayer) {
        val style = ExplosionStarStyle(user.uuid)
        val r = random.nextDouble(2.0, 5.0)
        val p = PointsBuilder()
            .addBall(r, 1)
            .rotateAsAxis(random.nextDouble(-PI, PI))
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
            .create().random()
        ParticleStyleManager.spawnStyle(world, user.eyePosition.add(p.toVector()), style)
    }

    fun testEmitters(world: ServerLevel, user: ServerPlayer) {
        val r = 45.0
        val p = PointsBuilder()
            .addBall(r, 1)
            .rotateAsAxis(random.nextDouble(-PI, PI))
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
            .create().random()
        val spawnPos = user.eyePosition.add(p.toVector())
        val emitters = ExplosionLineEmitters(spawnPos, world)
//        val k = -1 / tan(theta)
//        val vAngle = atan(k)
//        val vx = cos(vAngle)
//        val vz = sin(vAngle)
//        emitters.emittersVelocity = Vec3(vx, 0.0, vz)
//        emitters.maxTick = 120
        val targetPoint = user.eyePosition.add(user.forward.normalize().scale(5.0))
        emitters.targetPoint = targetPoint
        emitters.templateData.maxAge = 120
        emitters.templateData.speed = 1.2
        emitters.templateData.velocity = Vec3(
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
        )
        ParticleEmittersManager.spawnEmitters(emitters)
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(1, 160) {
            emitters.targetPoint = user.eyePosition.add(user.forward.normalize().scale(3.0))
        }
    }

    fun testBlock(world: Level, user: Player, block: BlockPos) {
        val entity = world.getBlockEntity(block) ?: return
        if (!world.isClientSide) {
            return
        }
        if (entity is AltarEntity) {
            user.sendSystemMessage(
                Component.literal(
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
            user.sendSystemMessage(
                Component.literal(
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

    fun testLight(world: Level, user: Player) {
        if (world.isClientSide) return
        val style = LightStyle(
            Vec3(210.0, 120.0, 200.0), 40.0, 0.4f, 2f, 1f, 120
        )
        ParticleStyleManager.spawnStyle(world as ServerLevel, user.position(), style)
    }

    fun testMath(world: Level, user: Player) {
        if (world.isClientSide) return
        user as ServerPlayer
        world as ServerLevel
        val random = Random(System.currentTimeMillis())
        val randomDirection = RelativeLocation(
            0.0,
            random.nextDouble(-5.0, 5.0),
            random.nextDouble(-5.0, 5.0),
        )
        randomDirection.multiply(3.0 / randomDirection.length())
        val style = TestStyle(user.uuid)
        ParticleStyleManager.spawnStyle(world, user.eyePosition, style)
    }

    fun testMeteorite(world: Level, user: Player, hand: InteractionHand) {
        if (world.isClientSide) {
            return
        }
        user as ServerPlayer
        val random = Random(System.currentTimeMillis())
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(5, 60) {
            val origin = user.eyePosition.add(random.nextDouble(-16.0, 16.0), 50.0, random.nextDouble(-16.0, 16.0))
            val meteorite = TestMeteorite().apply {
                shooter = user
                direction = RelativeLocation.yAxis().multiply(-1)
            }
            meteorite.spawn(origin, world as ServerLevel)
        }
    }

    fun testMana(world: Level, user: Player, hand: InteractionHand) {
        if (!world.isClientSide) {
            val data = UsefulMagic.state.getDataFromServer(user.uuid)
            user.sendSystemMessage(
                Component.literal(
                    """
                    玩家 ${user.name.string} 的魔力属性值
                    魔力: ${data.mana}
                    最大魔力值: ${data.maxMana}
                    魔力恢复/秒 :${data.manaRegeneration}
                    当前视图 服务器
                """.trimIndent()
                )
            )
            UsefulMagic.state.magicPlayerData.forEach {
                val player = world.server!!.playerList.getPlayer(it.key) ?: return@forEach
                val data = it.value
                user.sendSystemMessage(
                    Component.literal(
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
        user.sendSystemMessage(
            Component.literal(
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