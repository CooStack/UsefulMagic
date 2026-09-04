package cn.coostack.usefulmagic.display.magic.attack

import cn.coostack.cooparticlesapi.animation.timeline.ValueConstSpeedAnimator
import cn.coostack.cooparticlesapi.annotations.CodecField
import cn.coostack.cooparticlesapi.annotations.CooAutoRegister
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.display.AutoDisplayEntity
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableFallingDustEffect
import cn.coostack.cooparticlesapi.utils.MinecraftRendererUtil
import cn.coostack.usefulmagic.barrages.magic.GoldenMagicBarrage
import cn.coostack.usefulmagic.extend.boxCenterPosition
import cn.coostack.usefulmagic.extend.canSee
import cn.coostack.usefulmagic.particles.composition.magic.attack.BlockBorderComposition
import cn.coostack.usefulmagic.particles.emitters.magic.BlockFragmentEmitters
import cn.coostack.usefulmagic.utils.FriendFilterHelper
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import cn.coostack.cooparticlesapi.extend.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@CooAutoRegister
class GoldenMagicDisplay(pos: Vec3, world: Level?) : AutoDisplayEntity(pos, world) {
    @CodecField
    var attackCount = 4

    @CodecField
    var attackCD = 0

    @CodecField
    var shooterID = 0

    @CodecField
    var age = 0

    @CodecField
    var damage = 0.0

    @CodecField
    var scaleTarget = ValueConstSpeedAnimator(
        0.03, 0.6
    )

    init {
        scale = scaleTarget.current.toFloat()
        prevScale = scale
        manageRotation = false
    }

    private val renderBlock = Blocks.GOLD_BLOCK
    private val stack = renderBlock.asItem().defaultInstance
    private val composition = BlockBorderComposition(pos, world)
    override fun render(
        view: Matrix4f,
        proj: Matrix4f,
        modelMatrixStack: PoseStack,
        buffer: MultiBufferSource,
        delta: Float,
        camera: Camera
    ) {
        // 金块
        // 进行一些旋转
        val itemRenderer = Minecraft.getInstance().itemRenderer
        val model = itemRenderer.getModel(stack, world, null, 1)
        val scale = scale(delta)
        MinecraftRendererUtil.applyAtPoint(
            renderCenterOffset(), modelMatrixStack
        ) {
            MinecraftRendererUtil.applyRotation(
                this,
                yaw(delta),
                pitch(delta),
                roll(delta)
            )
            scale(scale, scale, scale)
        }
        MinecraftRendererUtil.renderItemModel(
            itemRenderer, stack,
            modelMatrixStack, model,
            LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY,
            buffer.getBuffer(RenderType.cutout())
        )
    }

    override fun transformOffset(): Vec3 {
        return -renderCenterOffset()
    }

    fun setShooter(shooter: LivingEntity): GoldenMagicDisplay {
        this.shooterID = shooter.id
        return this
    }

    fun getShooter(): LivingEntity? {
        return world?.getEntity(shooterID) as? LivingEntity
    }


    override fun remove() {
        super.remove()
        composition.remove()
    }

    override fun tick() {
        super.tick()
        scale = scaleTarget.next().toFloat()
        if (age++ > 20 * 15) {
            world!!.playSound(
                null,
                BlockPos.containing(pos),
                SoundEvents.NETHERITE_BLOCK_BREAK,
                SoundSource.PLAYERS,
                5f,
                2f
            )
            remove()
        }
        val shooter = getShooter() ?: return
        // 跟随玩家
        // 围绕玩家旋转
        val angle = 5 * age * PI / 180
        val xOff = cos(angle) * 4
        val zOff = sin(angle) * 4
        this.pos = shooter.eyePosition.add(xOff, -0.5, zOff)
        if (world!!.isClientSide) {
            return
        }
        composition.teleportTo(pos)
        if (!composition.displayed) {
            ParticleCompositionManager.spawn(composition)
        }
        if (attackCD-- > 0) return

        // 搜索攻击范围内的目标
        val box = AABB.ofSize(pos, 48.0, 48.0, 48.0)
        val target = world!!.getEntitiesOfClass(LivingEntity::class.java, box) {
            // 过滤目标
            it != shooter && it.canSee(pos) && FriendFilterHelper.filterNotFriend(shooter, it) && it.isAlive
        }.firstOrNull()
        if (target != null) {
            // 发起攻击
            // 生成弹幕
            scaleTarget.targetNum = scaleTarget.targetNum.toDouble() - 0.06
            attackCD = 40
            if (attackCount-- < 0) {
                remove()
            }
            val dir = (target.boxCenterPosition() - pos)
            rotateToPoint(dir.asRelative())
            val barrage = GoldenMagicBarrage(pos, world as ServerLevel, damage, shooter)
            barrage.direction = dir
            BarrageManager.spawn(barrage)
            // 生成金块碎片粒子
            val emitter = BlockFragmentEmitters(pos, world)
                .apply {
                    template.apply {
                        effect = ControlableFallingDustEffect(
                            uuid, Blocks.GOLD_BLOCK.defaultBlockState()
                        )
                    }
                    maxTick = 1
                    gravity = 0.05
                }
            ParticleEmittersManager.spawnEmitters(emitter)
        }
    }
}
