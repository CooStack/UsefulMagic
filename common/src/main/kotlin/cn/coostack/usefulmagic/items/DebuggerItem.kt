package cn.coostack.usefulmagic.items

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.entity.AltarEntity
import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.EnergyCrystalsBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.extend.mana
import cn.coostack.usefulmagic.extend.manaAbsorptionRate
import cn.coostack.usefulmagic.extend.maxMana
import cn.coostack.usefulmagic.meteorite.MeteoriteBarrage
import cn.coostack.usefulmagic.meteorite.MeteoriteDisplay
import cn.coostack.usefulmagic.particles.composition.LightComposition
import cn.coostack.usefulmagic.particles.composition.explosion.ExplosionMagicBallComposition
import cn.coostack.usefulmagic.particles.composition.explosion.ExplosionMagicComposition
import cn.coostack.usefulmagic.particles.composition.explosion.ExplosionStarComposition
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionLineEmitters
import cn.coostack.usefulmagic.renderer.MeteoriteAtmosphereFireRenderEntity
import cn.coostack.usefulmagic.renderer.SkyFallingRenderEntity
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.PI
import kotlin.random.Random

class DebuggerItem : Item(Properties()) {
    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val res = super.use(world, user, hand)
        if (world.isClientSide) {
            return res
        }
        world as ServerLevel
        user as ServerPlayer
        testShader(world, user)
        return res
    }

    fun testShader(world: ServerLevel, user: ServerPlayer) {
        val target = user.eyePosition.add(user.forward.normalize().scale(3.0))
        val shader = if (user.isShiftKeyDown) {
            SkyFallingRenderEntity(world, user.position())
        } else {
            MeteoriteAtmosphereFireRenderEntity(world)
                .configure(
                    center = target,
                    moveDirection = user.forward,
                    meteoriteSize = 2.2f,
                    maxLifetime = 100,
                    fadeIn = 8,
                    fadeOut = 18,
                )
        }
        ServerRenderEntityManager.spawn(shader)
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
                    当前能量: ${entity.currentMana}
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
                    阵法是否激活: ${entity.formation.isActiveFormation()}
                    阵法是否可激活: ${entity.formation.canBeFormation()}
                    阵法中水晶个数: ${entity.formation.activeCrystals.size}
                    阵法激活主人: ${entity.formation.owner?.let{ world.server.playerList.getPlayer(it) } ?: ""}
                    阵法范围: ${entity.formation.getFormationTriggerRange()}
                  
                """.trimIndent()
            )
        )
    }


    fun testExplosionMagicBallStyle(world: ServerLevel, user: ServerPlayer) {
        val style = ExplosionMagicBallComposition(user.position(), world).apply {
            player = user.uuid
        }
        ParticleCompositionManager.spawn(style)
    }

    fun testExplosionMagicStyle(world: ServerLevel, user: ServerPlayer, rotate: RelativeLocation) {
        val style = ExplosionMagicComposition(user.position(), world)
        style.rotateDirection = rotate
        ParticleCompositionManager.spawn(style)
    }


    fun testStar(world: ServerLevel, user: ServerPlayer) {
        val r = random.nextDouble(2.0, 5.0)
        val p = PointsBuilder()
            .addBall(r, 1)
            .rotateAsAxis(random.nextDouble(-PI, PI))
            .rotateAsAxis(random.nextDouble(-PI, PI), RelativeLocation.xAxis())
            .create().random()
        val style = ExplosionStarComposition(user.eyePosition.add(p.toVector()), world)
        ParticleCompositionManager.spawn(style)
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
            emitters.markDirty()
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
                        祭坛方块属性
                        获取到的魔力最大值: ${entity.getDownActiveBlocksMaxMana()}
                        获取到的魔力恢复速度: ${entity.getDownActiveBlocksManaReviveSpeed()}
                        祭坛物品: ${entity.getAltarStack()}
                    """.trimIndent()
                )
            )
        }

        if (entity is MagicCoreBlockEntity) {
            user.sendSystemMessage(
                Component.literal(
                    """
                        祭坛核心方块属性
                        当前魔力值: ${entity.currentMana}
                        获取到的魔力最大值: ${entity.maxMana}
                        获取到的魔力恢复速度: ${entity.currentReviveSpeed}
                        合成进度: ${entity.craftingTick}
                        是否正在合成: ${entity.crafting}
                    """.trimIndent()
                )
            )
        }

    }

    fun testLight(world: Level, user: Player) {
        if (world.isClientSide) return
        val style = LightComposition(user.position(), world).apply {
            color = Vec3(210.0, 120.0, 200.0)
            maxHeight = 40.0
            minSize = 0.4f
            maxSize = 2f
            alpha = 1f
            maxAge = 120
        }
        ParticleCompositionManager.spawn(style)
    }


    fun testMeteorite(world: Level, user: Player, hand: InteractionHand) {
        if (world.isClientSide) {
            return
        }
        user as ServerPlayer
        val random = Random(System.currentTimeMillis())
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(5, 60) {
            val origin = user.eyePosition.add(random.nextDouble(-16.0, 16.0), 50.0, random.nextDouble(-16.0, 16.0))
            val meteorite = MeteoriteBarrage(
                origin, world as ServerLevel, MeteoriteDisplay(origin, world),
                BarrageOption()
                    .apply {
                        enableSpeed = true
                        accelerationMaxSpeedEnabled = true
                        accelerationMaxSpeed = 5.0
                        speed = 0.1
                        acceleration = 0.1
                    }
            ).apply {
                shooter = user
                direction = -RelativeLocation.yAxis().toVector()
            }
            BarrageManager.spawn(meteorite)
        }
    }

    fun testMana(world: Level, user: Player, hand: InteractionHand) {
        if (!world.isClientSide) {
            val data = UsefulMagic.state.getDataFromServer(user.uuid)
            user.sendSystemMessage(
                Component.literal(
                    """
                    玩家 ${user.name.string} 的魔力属性值
                    魔力: ${user.mana}
                    最大魔力值: ${user.maxMana}
                    魔力恢复/秒: ${user.manaAbsorptionRate}
                    当前视图 服务端
                """.trimIndent()
                )
            )
            UsefulMagic.state.magicPlayerData.forEach {
                val player = world.server!!.playerList.getPlayer(it.key) ?: return@forEach
                user.sendSystemMessage(
                    Component.literal(
                        """
                    玩家 ${player.name.string} 的魔力属性值
                    魔力: ${player.mana}
                    最大魔力值: ${player.maxMana}
                    魔力恢复/秒: ${player.manaAbsorptionRate}
                    当前视图 服务端 - 其余玩家
                """.trimIndent()
                    )
                )
            }
            return
        }
        user.sendSystemMessage(
            Component.literal(
                """
                    玩家 ${user.name.string} 的魔力属性值
                    魔力: ${user.mana}
                    最大魔力值: ${user.maxMana}
                    魔力恢复/秒: ${user.manaAbsorptionRate}
                    当前视图 客户端
                """.trimIndent()
            )
        )
    }

}
