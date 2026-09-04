package cn.coostack.usefulmagic.items.weapon.magic.useful

import cn.coostack.cooparticlesapi.data.cache.CacheKey
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.damagetypes.UsefulMagicDamageSources
import cn.coostack.usefulmagic.data.magic.DiggingState
import cn.coostack.usefulmagic.extend.mana
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes
import cn.coostack.usefulmagic.items.weapon.magic.MagicItem
import cn.coostack.usefulmagic.particles.emitters.CollectLineParticleEmitter
import cn.coostack.usefulmagic.renderer.CylinderLaserRenderEntity
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import cn.coostack.usefulmagic.utils.MagicHelper
import cn.coostack.usefulmagic.utils.MathUtil
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.roundToInt

class DiggingMagic(properties: Properties) : MagicItem(properties) {
    companion object {
        private const val FIRE_DELAY_TICKS = 10
        private const val MAX_DISTANCE = 64.0
        private const val LASER_RADIUS = 0.15
        private const val FIRE_TICKS = 60
        private const val DIGGING_PARTICLE_INTERVAL = 2
        private const val DIGGING_SOUND_INTERVAL = 5

        private val LASER_COLOR = Math3DUtil.colorOf(255, 246, 178)

        private val DIGGING_STATE_TAG = CacheKey.of<DiggingState>(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "digging_magic_state")
        )

        private val DIGGING_EMITTER = CacheKey.of<CollectLineParticleEmitter>(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "digging_emitter")
        )

        private val LASER_TAG = CacheKey.of<CylinderLaserRenderEntity>(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, "digging_magic_laser")
        )
    }

    override fun release(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        time: Int
    ) {
        clear(shooter, world)
    }

    override fun usingTick(
        shooter: LivingEntity,
        wandStack: ItemStack,
        ballStack: ItemStack,
        world: Level,
        time: Int
    ) {

        updateEmitter(shooter)

        if (time < FIRE_DELAY_TICKS || world !is ServerLevel) {
            return
        }

        val clip = clipBlock(world, shooter)
        val start = calculateStartPosition(shooter)
        val maxUseTicks = MagicHelper.getMaxChargingTick(wandStack).coerceAtLeast(FIRE_DELAY_TICKS)
        val end = if (clip.type == HitResult.Type.BLOCK) {
            clip.location
        } else {
            start.add(shooter.lookAngle.normalize().scale(MAX_DISTANCE))
        }

        updateLaser(shooter, world, start, end, maxUseTicks)
        burnEntities(shooter, world, wandStack, start, end)

        if (clip.type == HitResult.Type.BLOCK) {
            tickDiggingState(shooter, world, wandStack, ballStack, clip, time)
        } else {
            clearDiggingState(shooter, world)
        }
    }

    override fun stopUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack,
        chargingTick: Int,
        max: Boolean
    ) {
        clear(shooter, world)
        if (world.isClientSide || chargingTick <= 0) {
            return
        }
        applyReleaseCooldown(shooter, wandStack, chargingTick)
    }

    override fun startUse(
        shooter: LivingEntity,
        world: Level,
        wandStack: ItemStack,
        ballStack: ItemStack
    ) {
        clear(shooter, world)
    }

    private fun clipBlock(world: Level, shooter: LivingEntity): BlockHitResult {
        val start = shooter.eyePosition
        val end = start.add(shooter.lookAngle.normalize().scale(MAX_DISTANCE))
        return world.clip(
            ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                shooter
            )
        )
    }

    private fun calculateStartPosition(shooter: LivingEntity) = shooter.eyePosition + shooter.forward

    private fun updateEmitter(shooter: LivingEntity) {
        val pos = calculateStartPosition(shooter)
        shooter.cacher.getOrCreate(DIGGING_EMITTER) {
            CollectLineParticleEmitter(pos, shooter.level()).apply {
                simpleData.apply {
                    minSpeed = 1.8
                    maxSpeed = 2.5
                    minAge = 10
                    maxAge = 20
                    minCount = 20
                    maxCount = 30
                    disappearRadius = 0.1
                    leftColor = Math3DUtil.colorOf(210, 255, 100)
                }
                maxTick = -1
                ParticleEmittersManager.spawnEmitters(this)
            }
        }.apply {
            teleportTo(pos)
        }
    }

    private fun updateLaser(
        shooter: LivingEntity,
        world: ServerLevel,
        start: Vec3,
        end: Vec3,
        lifetimeTicks: Int
    ) {
        shooter.cacher.getOrCreate(LASER_TAG) {
            CylinderLaserRenderEntity(world, start).apply {
                updateBeam(start, end)
                color = LASER_COLOR
                maxRadius = 0.5f
                brightness = 1.2f
                phaseTicks = 10
                lifetime = lifetimeTicks
                shrinkOnFadeOut = false
                ServerRenderEntityManager.spawn(this)
            }
        }.apply {
            updateBeam(start, end)
            color = LASER_COLOR
            maxRadius = 0.5f
            brightness = 1.2f
            lifetime = lifetimeTicks
        }
    }

    private fun burnEntities(
        shooter: LivingEntity,
        world: ServerLevel,
        wandStack: ItemStack,
        start: Vec3,
        end: Vec3
    ) {
        val damage = MagicHelper.getMagicDamage(wandStack).toFloat()
        if (damage <= 0f) {
            return
        }
        val source = UsefulMagicDamageSources.entityMagic(world, shooter, shooter)
        world.getEntitiesOfClass(
            LivingEntity::class.java,
            AABB(start, end).inflate(LASER_RADIUS * 2)
        ) {
            it.uuid != shooter.uuid && it.isAlive && FriendFilterHelper.filterNotFriend(shooter, it)
        }.forEach { target ->
            if (!MathUtil.isIntersectsBox(start, end, LASER_RADIUS, target.boundingBox)) {
                return@forEach
            }
            target.remainingFireTicks = target.remainingFireTicks.coerceAtLeast(FIRE_TICKS)
            if (target.hurt(source, damage)) {
                target.invulnerableTime = 5
            }
        }
    }

    private fun tickDiggingState(
        shooter: LivingEntity,
        world: ServerLevel,
        wandStack: ItemStack,
        ballStack: ItemStack,
        clip: BlockHitResult,
        time: Int
    ) {
        val blockPos = clip.blockPos
        if (!canDig(world, blockPos)) {
            clearDiggingState(shooter, world)
            return
        }

        val cached = shooter.cacher[DIGGING_STATE_TAG]
        if (cached != null && (cached.pos != blockPos || cached.world != world.dimension())) {
            cached.resetProgress(world, shooter)
            shooter.cacher.remove(DIGGING_STATE_TAG)
        }

        val state = shooter.cacher.getOrCreate(DIGGING_STATE_TAG) {
            DiggingState(blockPos, 0f, world.dimension(), diggingBreakerId(shooter))
        }
        val magicLevel = diggingLevel(wandStack, ballStack)
        val blockState = world.getBlockState(blockPos)
        val nextProgress = state.progress + state.calculateSpeedAsLevel(world, magicLevel)
        if (nextProgress >= 1f && !consumeBlockMana(shooter, wandStack)) {
            clearDiggingState(shooter, world)
            return
        }

        state.progress = nextProgress
        if (state.progress >= 1f) {
            playBreakEffects(world, blockPos, blockState)
        } else {
            playDiggingEffects(world, blockPos, clip.location, blockState, time)
        }
        state.applyProgress(world, shooter, magicLevel)

        if (state.progress >= 1f || world.getBlockState(blockPos).isAir) {
            shooter.cacher.remove(DIGGING_STATE_TAG)
        }
    }

    private fun canDig(world: Level, pos: BlockPos): Boolean {
        val state = world.getBlockState(pos)
        return !state.isAir && state.getDestroySpeed(world, pos) >= 0f
    }

    private fun diggingLevel(wandStack: ItemStack, ballStack: ItemStack): Int {
        return wandStack.get(UsefulMagicDataComponentTypes.WAND_LEVEL.get())
            ?: ballStack.get(UsefulMagicDataComponentTypes.MAGIC_LEVEL.get())
            ?: 1
    }

    private fun diggingBreakerId(shooter: LivingEntity): Int {
        return -shooter.id - 1
    }

    private fun consumeBlockMana(shooter: LivingEntity, wandStack: ItemStack): Boolean {
        if (shooter !is Player || shooter.hasInfiniteMaterials()) {
            return true
        }
        val manaCost = MagicHelper.getManaCost(wandStack)
        if (manaCost <= 0) {
            return true
        }
        if (shooter.mana < manaCost) {
            return false
        }
        shooter.mana -= manaCost
        return true
    }

    private fun playDiggingEffects(
        world: ServerLevel,
        pos: BlockPos,
        hit: Vec3,
        blockState: net.minecraft.world.level.block.state.BlockState,
        time: Int
    ) {
        if (time % DIGGING_PARTICLE_INTERVAL == 0) {
            world.sendParticles(
                BlockParticleOption(ParticleTypes.BLOCK, blockState),
                hit.x,
                hit.y,
                hit.z,
                3,
                0.08,
                0.08,
                0.08,
                0.02
            )
        }
        if (time % DIGGING_SOUND_INTERVAL == 0) {
            val soundType = blockState.soundType
            world.playSound(
                null,
                pos,
                soundType.hitSound,
                SoundSource.BLOCKS,
                (soundType.volume + 1.0f) * 0.35f,
                soundType.pitch * 0.8f
            )
        }
    }

    private fun playBreakEffects(
        world: ServerLevel,
        pos: BlockPos,
        blockState: net.minecraft.world.level.block.state.BlockState
    ) {
        val soundType = blockState.soundType
        world.sendParticles(
            BlockParticleOption(ParticleTypes.BLOCK, blockState),
            pos.x + 0.5,
            pos.y + 0.5,
            pos.z + 0.5,
            24,
            0.35,
            0.35,
            0.35,
            0.08
        )
        world.playSound(
            null,
            pos,
            soundType.breakSound,
            SoundSource.BLOCKS,
            (soundType.volume + 1.0f) * 0.5f,
            soundType.pitch
        )
    }

    private fun applyReleaseCooldown(
        shooter: LivingEntity,
        wandStack: ItemStack,
        chargingTick: Int
    ) {
        if (shooter !is Player) {
            return
        }
        val maxUseTicks = MagicHelper.getMaxChargingTick(wandStack)
        if (maxUseTicks <= 0) {
            return
        }
        val progress = (chargingTick.toDouble() / maxUseTicks).coerceIn(0.0, 1.0)
        val cooldown = (MagicHelper.getFinalCD(wandStack) * progress).roundToInt()
        if (cooldown > 0) {
            shooter.cooldowns.addCooldown(wandStack.item, cooldown)
        }
    }

    private fun clear(shooter: LivingEntity, world: Level) {
        shooter.cacher.remove(LASER_TAG)?.discard()
        shooter.cacher.remove(DIGGING_EMITTER)?.remove()
        clearDiggingState(shooter, world)
    }

    private fun clearDiggingState(shooter: LivingEntity, world: Level) {
        shooter.cacher.remove(DIGGING_STATE_TAG)?.resetProgress(world, shooter)
    }
}
