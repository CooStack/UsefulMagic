package cn.coostack.usefulmagic.items.prop

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.data.cache.CacheKey
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.scheduler.CooScheduler
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.entity.formation.EnergyCrystalsBlockEntity
import cn.coostack.usefulmagic.effects.UsefulMagicEffects
import cn.coostack.usefulmagic.formation.CrystalFormation
import cn.coostack.usefulmagic.formation.api.BlockFormation
import cn.coostack.usefulmagic.formation.api.DefendCrystal
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.gamerules.UsefulMagicGameRules
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.particles.emitters.ExplodeMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionAnimateLaserMagicEmitters
import cn.coostack.usefulmagic.particles.emitters.explosion.ExplosionWaveEmitters
import cn.coostack.usefulmagic.particles.fall.composition.GuideCircleComposition
import cn.coostack.usefulmagic.particles.fall.composition.SkyFallingComposition
import cn.coostack.usefulmagic.renderer.SkyFallingRenderEntity
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import cn.coostack.usefulmagic.utils.MathUtil
import cn.coostack.usefulmagic.utils.UsefulMagicFlightController
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.RelativeMovement
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 天空坠落魔法
 *
 * 消耗品
 * 使用时会在视线前方最多 100 方块进行爆破
 * 展开时会对周围(r <= 10) 的实体进行缓速
 */
class SkyFallingRuneItem : Item(Properties().stacksTo(16).rarity(Rarity.EPIC)) {
    companion object {
        const val KNOCKBACK_HIT_DAMAGE = 100f
        const val HIT_DAMAGE = 200f
        const val EXPLOSION_MAX_RADIUS = 48
        private const val PROTECTED_BLOCK_DAMAGE = 0.5f
        private const val FORMATION_MANA_PER_DAMAGE = 10f
        val playerTasks = HashMap<UUID, MutableList<CooScheduler.TickRunnable>>()

        @JvmField
        val GUIDE_COMPOSITION = CacheKey.of<GuideCircleComposition>(
            ResourceLocation.fromNamespaceAndPath(
                UsefulMagic.MOD_ID,
                "guide_composition"
            )
        )

        @JvmField
        val SKY_FALLING_COMPOSITION = CacheKey.of<SkyFallingComposition>(
            ResourceLocation.fromNamespaceAndPath(
                UsefulMagic.MOD_ID,
                "sky_falling_composition"
            )
        )

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
        tooltip: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        tooltip.add(Component.literal("花费巨大的代价，迅速击溃敌人"))
        tooltip.add(Component.literal("超位魔法-天空坠落"))
        tooltip.add(Component.literal("右键使用"))
        tooltip.add(Component.literal("消耗品"))
        tooltip.add(Component.literal("不消耗魔力值"))
        super.appendHoverText(stack, context, tooltip, tooltipFlag)
    }

    override fun inventoryTick(stack: ItemStack, world: Level, entity: Entity, slot: Int, selected: Boolean) {
        if (!selected) return
        if (world.isClientSide) return
        if (entity !is Player) return
        if (UsefulMagicEffects.isMagicSealed(entity)) return
        val world = world as ServerLevel
        val entity = entity as ServerPlayer

        var composition = entity.cacher.getOrCreate(GUIDE_COMPOSITION) {
            GuideCircleComposition(getTargetLocation(entity), entity.level())
        }
        if (composition.bindPlayer != entity.uuid) {
            composition.bindPlayer = entity.uuid
            if (composition.displayed) {
                composition.markDirty()
            }
        }
        if (!composition.isValid()) {
            composition = GuideCircleComposition(getTargetLocation(entity), entity.level())
            composition.bindPlayer = entity.uuid
            entity.cacher[GUIDE_COMPOSITION] = composition
            ParticleCompositionManager.spawn(composition)
            return
        }
        if (!composition.displayed) {
            ParticleCompositionManager.spawn(composition)
            return
        }
    }


    override fun getUseAnimation(stack: ItemStack): UseAnim {
        return UseAnim.BOW
    }


    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return 5
    }

    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        if (user !is Player) return stack
        if (UsefulMagicEffects.isMagicSealed(user)) {
            return stack
        }

        val hand = user.usedItemHand
        val stack = user.getItemInHand(hand)
        if (!user.isCreative) {
            stack.count -= 1
            user.cooldowns.addCooldown(this, 20 * 60 * 20)
        } else {
            user.cooldowns.addCooldown(this, 20 * 20)
        }
        if (world.isClientSide) return stack
        world.playSound(
            null, user.x, user.y, user.z,
            UsefulMagicSoundEvents.SKY_FALLING_MAGIC_START.get(),
            SoundSource.PLAYERS,
            10f, 1f
        )
        val world = world as ServerLevel
        if (!(user.cacher[SKY_FALLING_COMPOSITION]?.isValid() ?: false)) {
            val composition = SkyFallingComposition(user.position(), world)
            composition.bindPlayer = user.uuid
            ParticleCompositionManager.spawn(composition)
            user.cacher[SKY_FALLING_COMPOSITION] = composition
        }
        // 设置target
        val target = getTargetLocation(user)
        val tasks = playerTasks[user.uuid] ?: ArrayList()
        if (tasks.isNotEmpty() && tasks.any { !it.canceled }) {
            return stack
        }
        val serverUser = user as ServerPlayer
        val explodeTask = CooParticlesAPI.scheduler.runTask(12 * 20) {
            handleExplodeParticle(world, serverUser, target)
            handleExplode(world, serverUser, target)
        }
        tasks.add(explodeTask)
        val data = UsefulMagic.state.getDataFromServer(user.uuid)
        val releasePos = user.position()
        UsefulMagicFlightController.grant(serverUser, UsefulMagicFlightController.Source.SKY_FALLING)
        val attackTask = CooParticlesAPI.scheduler.runTaskTimerMaxTick(1, 12 * 20) {
            val r = 12.0
            val box = AABB.ofSize(target, r * 2, r * 2, r * 2)
            val entities = world.getEntitiesOfClass(LivingEntity::class.java, box) {
                !data.isFriend(it.uuid) && it.uuid != user.uuid
            }
            entities.forEach { entity ->
                entity.addEffect(
                    MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(UsefulMagicEffects.FREEZE.get()),
                        5,
                        1
                    ), user
                )
            }
            user.addEffect(
                MobEffectInstance(
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(UsefulMagicEffects.FREEZE.get()),
                    5,
                    1
                ), user
            )
            lockPlayerPosition(serverUser, releasePos)
        }.setFinishCallback {
            UsefulMagicFlightController.revoke(serverUser, UsefulMagicFlightController.Source.SKY_FALLING)
        }
        tasks.add(attackTask)
        playerTasks[user.uuid] = tasks
        return super.finishUsingItem(stack, world, user)
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack?> {
        if (UsefulMagicEffects.isMagicSealed(user)) {
            return InteractionResultHolder.fail(user.getItemInHand(hand))
        }
        // 防止重复执行（然后第二个无效）
        val styleAlive = user.cacher[SKY_FALLING_COMPOSITION]?.isValid() ?: false
        val taskAlive = (playerTasks[user.uuid] ?: ArrayList()).all { it.canceled }
        if (styleAlive && taskAlive) {
            return super.use(world, user, hand)
        }
        user.startUsingItem(hand)
        return super.use(world, user, hand)
    }


    private fun handleExplode(world: ServerLevel, user: ServerPlayer, target: Vec3) {
        // 有效攻击范围  r <= 24
        // 攻击性击退范围 r <= 36
        // 击退范围 r <= 48
        val r = 48.0
        val data = UsefulMagic.state.getDataFromServer(user.uuid)
        val entities = world.getEntitiesOfClass(LivingEntity::class.java, AABB.ofSize(target, r * 2, r * 2, r * 2)) {
            !data.isFriend(it.uuid) && it.uuid != user.uuid
        }

        val knockbackEntity = entities.filter {
            val d = it.position().distanceTo(target)
            d > 36.0 && d <= r
        }

        val knockbackAndHitEntity = entities.filter {
            val d = it.position().distanceTo(target)
            d <= 36.0 && d > 24.0
        }

        val hitEntity = entities.filter {
            val d = it.position().distanceTo(target)
            d <= 24.0
        }
        // 第一轮的伤害
        knockbackEntity.forEach {
            val dir = target.relativize(it.position()).normalize().scale(3.0)
            // 3.0
            it.deltaMovement = dir
            it.hurtMarked = true
        }

        knockbackAndHitEntity.forEach {
            val dir = target.relativize(it.position()).normalize().scale(2.0)
            // 4.0
            it.deltaMovement = dir
            it.hurtMarked = true
        }
        hitEntity.forEach {
            val dir = target.relativize(it.position()).normalize().scale(0.5)
            // 5.0
            it.deltaMovement = dir
            it.hurtMarked = true
        }
        applyDamageWithFormationProtection(
            world,
            user,
            knockbackAndHitEntity.map { it to KNOCKBACK_HIT_DAMAGE } + hitEntity.map { it to 2048f },
            formationDamage = { formation ->
                val distance = max(
                    0.0,
                    formation.formationCore.distanceTo(target) - formation.getFormationTriggerRange()
                )
                when {
                    distance <= 24.0 -> 2048f / 5
                    distance <= 36.0 -> KNOCKBACK_HIT_DAMAGE / 5
                    else -> null
                }
            }
        )

        var currentRadius = 1
        val protectedBlocks = HashSet<BlockPos>()
        val protectedBlockCounts = HashMap<BlockFormation, Int>()
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(2, EXPLOSION_MAX_RADIUS) {
            createSkyFallingExplosionStep(
                currentRadius++,
                world,
                target,
                user,
                protectedBlocks,
                protectedBlockCounts
            )
            createSkyFallingExplosionStep(
                currentRadius++,
                world,
                target,
                user,
                protectedBlocks,
                protectedBlockCounts
            )
        }.setFinishCallback {
            applyProtectedFormationDamage(protectedBlockCounts, user)
        }
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(5, 8 * 20) {
            val attackBox = AABB.ofSize(target, 96.0, 96.0, 96.0)
            val hitEntities = world.getEntitiesOfClass(
                LivingEntity::class.java,
                attackBox
            ) {
                !data.isFriend(it.uuid) && it.uuid != user.uuid
            }
            applyDamageWithFormationProtection(
                world,
                user,
                hitEntities.map { it to HIT_DAMAGE / 8 },
                formationDamage = { formation ->
                    if (intersectsFormation(attackBox, formation)) HIT_DAMAGE / 8 / 5 else null
                },
                resetInvulnerableTime = true
            )
        }
    }

    private fun applyDamageWithFormationProtection(
        world: ServerLevel,
        user: ServerPlayer,
        targets: Iterable<Pair<LivingEntity, Float>>,
        formationDamage: (BlockFormation) -> Float?,
        resetInvulnerableTime: Boolean = false
    ) {
        val attacker = LivingEntityTargetOption(user, false)
        val blockingFormations = ServerFormationManager.activeFormations.values.mapNotNull { formation ->
            if (formation.world !== world ||
                formation.owner == user.uuid ||
                !formation.isActiveFormation() ||
                !formation.hasCrystalType(DefendCrystal::class.java) ||
                formation.isFriendly(attacker)
            ) {
                return@mapNotNull null
            }
            formationDamage(formation)?.let { formation to it }
        }

        blockingFormations.forEach { (formation, damage) ->
            formation.attack(damage, null, formation.formationCore)
        }

        val source = world.damageSources().playerAttack(user)
        targets.forEach { (entity, damage) ->
            if (blockingFormations.any { (formation) ->
                    formation.formationCore.distanceTo(entity.position()) <= formation.getFormationTriggerRange()
                }
            ) {
                return@forEach
            }
            entity.hurt(source, damage)
            if (resetInvulnerableTime) {
                entity.invulnerableTime = 0
            }
        }
    }

    private fun intersectsFormation(box: AABB, formation: BlockFormation): Boolean {
        val core = formation.formationCore
        val closest = Vec3(
            core.x.coerceIn(box.minX, box.maxX),
            core.y.coerceIn(box.minY, box.maxY),
            core.z.coerceIn(box.minZ, box.maxZ)
        )
        val range = formation.getFormationTriggerRange()
        return core.distanceToSqr(closest) <= range * range
    }

    private fun lockPlayerPosition(user: ServerPlayer, pos: Vec3) {
        user.connection.teleport(
            pos.x,
            pos.y,
            pos.z,
            user.yRot,
            user.xRot,
            RelativeMovement.ROTATION
        )
        user.deltaMovement = Vec3.ZERO
        user.hurtMarked = true
    }

    private fun createSkyFallingExplosionStep(
        currentRadius: Int,
        world: ServerLevel,
        center: Vec3,
        user: ServerPlayer,
        protectedBlocks: MutableSet<BlockPos>,
        protectedBlockCounts: MutableMap<BlockFormation, Int>
    ) {
        // 魔法地形破坏关闭时, 跳过整段方块清除(实体伤害已在调用前单独结算)。
        if (!UsefulMagicGameRules.canDestroyTerrain(world)) return
        val attacker = LivingEntityTargetOption(user, false)
        val solidBall = MathUtil.getSolidBall(currentRadius).map {
            ofFloored((it + RelativeLocation.of(center)).toVector())
        }.toSet()

        solidBall.forEach { pos ->
            if (!world.hasChunkAt(pos)) return@forEach
            val state = world.getBlockState(pos)
            val fluid = world.getFluidState(pos)
            val resistance = max(state.block.explosionResistance, fluid.explosionResistance)
            val canBreak =
                resistance >= 0f && resistance < 1000f && (fluid.isEmpty || state.`is`(Blocks.WATER)) && !state.isAir
            if (!canBreak) return@forEach

            val formation = ServerFormationManager.getFormationFromPos(pos.center, world)
            if (formation != null && formation.hasCrystalType(DefendCrystal::class.java) && formation.isActiveFormation()) {
                if (!formation.isFriendly(attacker) && protectedBlocks.add(pos)) {
                    protectedBlockCounts[formation] = (protectedBlockCounts[formation] ?: 0) + 1
                }
                return@forEach
            }

            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)
        }
    }

    private fun applyProtectedFormationDamage(
        protectedBlockCounts: Map<BlockFormation, Int>,
        user: ServerPlayer
    ) {
        val attacker = LivingEntityTargetOption(user, false)
        protectedBlockCounts.forEach { (formation, blockCount) ->
            if (blockCount <= 0 || !formation.isActiveFormation()) return@forEach

            val damage = blockCount.toFloat() * PROTECTED_BLOCK_DAMAGE
            val requiredMana = (damage * FORMATION_MANA_PER_DAMAGE).roundToInt()
            if (requiredMana <= 0) return@forEach

            val defendCrystal = formation.activeCrystals.firstOrNull { it is DefendCrystal }
            val currentMana =
                formation.activeCrystals.filterIsInstance<EnergyCrystalsBlockEntity>().sumOf { it.currentMana }

            if (defendCrystal != null && currentMana >= requiredMana) {
                formation.transformMana(defendCrystal, requiredMana)
            } else {
                val breakDamage = if (formation is CrystalFormation) {
                    max(damage, formation.formationHealth)
                } else {
                    damage
                }
                formation.breakFormation(breakDamage, attacker)
            }
        }
    }

    private fun handleExplodeParticle(world: ServerLevel, user: ServerPlayer, target: Vec3) {
        val entity = SkyFallingRenderEntity(world, target.add(0.0, -48.0, 0.0))
        ServerRenderEntityManager.spawn(entity)
        // 爆炸粒子
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(
            10, 150
        ) {
            ServerCameraUtil.sendShake(world, target, 128.0, 1.0, 20)
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
            gravity = -0.001
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
                        it.maxAge = 200
                        it.velocity = Vec3(0.0, 0.01, 0.0)
                    }
                    discrete = 0.1
                    randomVector = true
                    randomSpeed = 0.02
                }
        }

        // 冲击波云
        ParticleEmittersManager.spawnEmitters(genWave(20.0, 6.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(40.0, 10.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(60.0, 15.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(80.0, 19.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(90.0, 10.0, 0.85, 1.0))
        ParticleEmittersManager.spawnEmitters(genWave(110.0, 18.0, 0.85, 1.0))


        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            UsefulMagicSoundEvents.MAGIC_EXPLODE.get(),
            SoundSource.PLAYERS,
            10f,
            2f
        )
        world.playSound(
            null,
            user.x,
            user.y,
            user.z,
            SoundEvents.GENERIC_EXPLODE,
            SoundSource.PLAYERS,
            10f,
            1.5f
        )
    }

}
