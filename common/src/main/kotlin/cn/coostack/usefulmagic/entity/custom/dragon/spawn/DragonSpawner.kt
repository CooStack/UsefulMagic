package cn.coostack.usefulmagic.entity.custom.dragon.spawn

import cn.coostack.cooparticlesapi.animation.Animate
import cn.coostack.cooparticlesapi.animation.AnimateManager
import cn.coostack.cooparticlesapi.animation.AnimateNode
import cn.coostack.cooparticlesapi.api.controler.Controlable
import cn.coostack.cooparticlesapi.display.DisplayEntityManager
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.renderer.post.CooPostEffects
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.sound.ServerManagedSoundInstance
import cn.coostack.cooparticlesapi.sound.ServerSoundManager
import cn.coostack.cooparticlesapi.sound.SoundVolumeFalloff
import cn.coostack.cooparticlesapi.supports.TextureSheetsEnum
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.animate.CompositionAction
import cn.coostack.usefulmagic.animate.RenderAction
import cn.coostack.usefulmagic.animate.TickableAction
import cn.coostack.usefulmagic.entity.custom.dragon.MagicDragonEntity
import cn.coostack.usefulmagic.entity.custom.dragon.playDragonSoundOnce
import cn.coostack.usefulmagic.entity.custom.dragon.display.DragonCircleEyeDisplay
import cn.coostack.usefulmagic.entity.custom.dragon.emitters.TrackingTailEmitter
import cn.coostack.usefulmagic.entity.custom.dragon.skills.emitter.MagicRuneRingComposition
import cn.coostack.usefulmagic.entity.custom.dragon.skills.emitter.MagicRuneRingEffects
import cn.coostack.usefulmagic.entity.custom.dragon.spawn.composition.MagicDragonSpawnLaserComposition
import cn.coostack.usefulmagic.entity.custom.dragon.spawn.composition.MagicDragonSpawnRuneComposition
import cn.coostack.usefulmagic.entity.custom.dragon.spawn.composition.MagicDragonSpawningFloorComposition
import cn.coostack.usefulmagic.extend.lerpAsProgress
import cn.coostack.usefulmagic.renderer.DragonMagicBallRenderEntity
import cn.coostack.usefulmagic.renderer.StraightLaserRenderEntity
import cn.coostack.usefulmagic.renderer.UsefulMagicPostEffects
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import kotlin.math.PI
import kotlin.random.Random

/**
 * 要实现一种类似状态机的效果
 * - 参考 Ender Dragon Fight
 *
 * 只考虑服务器
 *
 * 需要做一个旋转的魔法阵 + 一些特殊符文旋转 （Composition） 实现一些星空效果
 *
 * 旋转不是roll变换，而是随机旋转到目标之后再进行roll旋转 (做一个旋转工具)
 *
 * 做一个类似Seq的
 *
 * @property conjurePosition 召唤位置 （实际生成位置在上面64方块）
 * @property spawnLevel 生成世界
 */
class DragonSpawner(val conjurePosition: Vec3, val spawnLevel: ServerLevel) {


    sealed interface State {
        fun tick(runtime: DragonSpawner, tick: Int)
        fun start(runtime: DragonSpawner)
        fun end(runtime: DragonSpawner, tick: Int)
    }

    companion object {
        /**
         * 可能的并发场景 （在不同的tick scheduler执行， 不确定这个scheduler和world的是否处于相同线程）
         */
        private val states = ConcurrentHashMap<UUID, DragonSpawner>()

        fun tick(world: ServerLevel) {
            val iterator = states.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next().value
                if (next.state == null) {
                    iterator.remove()
                    continue
                }
                if (next.spawnLevel != world) {
                    if (next.spawnLevel.dimension() == world.dimension()) {
                        next.cancel()
                        iterator.remove()
                    }
                    continue
                }
                next.tick()
            }
        }

        fun wasSpawningAt(world: Level, pos: Vec3): Boolean {
            return states.values.any {
                (it.spawnLevel.dimension() == world.dimension() && pos.distanceTo(it.conjurePosition) <= 512.0)
                        && (it.state !is End || it.state == null)
            }
        }


        class Start : State {
            override fun tick(runtime: DragonSpawner, tick: Int) {
                if (tick <= 60) return
                // 这里做一个附魔符文的聚集 （曲线聚集 （随机方向， 然后吸附感））
                // 实现的是移动拖尾聚集效果 （Animation）

                runtime.getAsIsInstance<DragonCircleEyeDisplay>("start_eye") ?: let {
                    // 他要收缩，且在下面（spawnPosition）
                    runtime["start_eye"] =
                        DragonCircleEyeDisplay(runtime.conjurePosition, runtime.spawnLevel).apply {
                            r = 32.0
                            rotateSpeed = PI / 32 // 速度要从 1/32 -> 1/4
                            borderSize = 0.8f
                            borderColor = Math3DUtil.colorOf(255, 150, 200)
                            count = 6
                            eyeScaled = 3f
                            DisplayEntityManager.spawn(this)
                        }
                }

                runtime.invokeAsIfPrecent<DragonCircleEyeDisplay>("start_eye") {
                    val progress = 0.01 * (tick - 60)
                    it.rotateSpeed = progress.lerpAsProgress(PI / 32, PI / 4)
                    it.r = progress.lerpAsProgress(32, 0.5)
                }
                // 进行能量聚集 （到Dragon Pos）
                if (tick == 155) {
                    playDragonSoundOnce(
                        runtime.spawnLevel,
                        runtime.conjurePosition,
                        UsefulMagicSoundEvents.DRAGON_SPAWN_MAGIC_LASER.get(),
                        SoundSource.HOSTILE,
                        2f,
                        1f,
                        256.0,
                    )
                }
                if (tick > 158) {
                    playDragonSoundOnce(
                        runtime.spawnLevel,
                        runtime.conjurePosition,
                        SoundEvents.ENDER_DRAGON_GROWL,
                        SoundSource.HOSTILE,
                        18f,
                        0.4f,
                        256.0,
                    )
                    runtime.state = PrepareMagic()
                }
            }

            override fun start(runtime: DragonSpawner) {
                runtime["rewind"] = false
            }

            override fun end(runtime: DragonSpawner, tick: Int) {
                runtime.invokeAsIfPrecent<DragonCircleEyeDisplay>("start_eye") {
                    it.discard()
                }
            }
        }

        class PrepareMagic : State {
            override fun tick(runtime: DragonSpawner, tick: Int) {
                runtime.state = Magic()
            }

            override fun start(runtime: DragonSpawner) {
                runtime["looping_ball"] = ServerSoundManager.instance(
                    UsefulMagicSoundEvents.LIQUID_BALL.get(),
                    SoundSource.HOSTILE
                )
                    .world(runtime.spawnLevel)
                    .position(runtime.conjurePosition)
                    .layer("spawning")
                    .uniqueKey(UUID.randomUUID().toString())
                    .visibleRange(128.0)
                    .volume(0.5f)
                    .pitch(1.4f)
                    .relative()
                    .volumeFalloff(SoundVolumeFalloff.QUADRATIC)
                    .looping()
                    .syncEveryTick(true)
                    .spawn()
                    .apply {
                        fadeTo(0.5f, 20, stopWhenFinished = false)
                    }

                runtime["spawn_laser"] = StraightLaserRenderEntity(runtime.spawnLevel, runtime.conjurePosition).apply {
                    lifetime = 30
                    maxRadius = 5f
                    this.phaseTicks = 20
                    this.setColor(
                        Math3DUtil.colorOf(255, 120, 240)
                    )
                    this.renderRange = 512.0
                    this.alpha = 1.0
                    this.brightness = 0.8f
                    updateBeam(
                        runtime.conjurePosition.add(0.0, 256.0, 0.0),
                        runtime.conjurePosition.add(0.0, -128.0, 0.0)
                    )

                    ServerRenderEntityManager.spawn(this)
                }

                ServerCameraUtil.sendShake(
                    runtime.spawnLevel, runtime.conjurePosition,
                    256.0, 4.0, 80, 8.0
                )

                // 发送发光， RGB分离
                CooPostEffects.server.spawn(runtime.spawnLevel, UsefulMagicPostEffects.rgbDashBlur(20, 2f, 2f, 2f))
                CooPostEffects.server.spawn(
                    runtime.spawnLevel,
                    UsefulMagicPostEffects.flameExplodeFlash(10, 2f, 2f, Vec3(1.0, 1.0, 1.0))
                )
                runtime["collect_emitter_animate"] = Animate()
                    .addNode(
                        AnimateNode()
                            .addAction(TickableAction { false }
                                .addPreTickAction {
                                    // 红色， 绿色， 淡蓝色 随机颜色吸收
                                    if (tickCount % 10 != 0) {
                                        return@addPreTickAction
                                    }
                                    val randomCount = (2 minRangeTo 6).random()

                                    val colorLeft = Math3DUtil.colorOf(255, 80, 60)
                                    val colorRight = Math3DUtil.colorOf(150, 255, 190)
                                    repeat(randomCount) {
                                        val randomColor =
                                            GraphMathHelper.lerp(Random.nextFloat(), colorLeft, colorRight)
                                        val targetPos = runtime.conjurePosition.add(0.0, 64.0, 0.0)
                                        val randomPos = targetPos.offsetRandomly(Random.nextDouble(32.0, 72.0))
                                            .withY { y - 32.0 }
                                        val emitter = TrackingTailEmitter(randomPos, runtime.spawnLevel).apply {
                                            trackingTarget = targetPos
                                            this.arriveCanceled = true
                                            particleConfig.apply {
                                                this.color = randomColor
                                                this.visibleRange = 256f
                                                this.setTextureSheet(TextureSheetsEnum.ADDITION_BLEND_TRANSLUCENT)
                                            }
                                            this.simpleConfig.apply {
                                                this.minAge = 10
                                                this.maxAge = 15
                                                this.minCount = 5
                                                this.maxCount = 8
                                                this.minSize = 0.1
                                                this.maxSize = 0.3
                                                this.minSpeed = 1.2
                                                this.maxSpeed = 3.6
                                            }

                                            this.noiseStrength = 0.06
                                            this.offsetNoise = 0.2
                                            this.strength = 2.8
                                            this.velocity = Vec3.ZERO.random()
                                        }
                                        ParticleEmittersManager.spawnEmitters(emitter)
                                    }
                                })
                    ).apply {
                        AnimateManager.displayAnimateServer(this)
                    }
                runtime["emitter_spawning_animate"] = Animate()
                    .addNode(
                        AnimateNode()
                            .addAction(
                                TickableAction {
                                    runtime.getAsIsInstance<Boolean>("rewind") ?: false
                                }

                            ))
                    .apply {
                        AnimateManager.displayAnimateServer(this)
                    }
            }

            override fun end(runtime: DragonSpawner, tick: Int) {
            }

        }

        /**
         * 释放魔法的过程
         */
        class Magic : State {
            var count = 0
            override fun tick(runtime: DragonSpawner, tick: Int) {
                // 等待多少个tick之后转移到下一个-
                // 大概的设计是 龙生成的位置会先生成一个球  TODO（魔法球， 有纹路各种的 （纹理+泛光））
                // 然后他的半径 64个位置 Y随机偏移 -10 - +20 生成对应的魔法阵， 召唤激光
                // 全部召唤完成后进入下一个阶段 State
                // 激光周围会有类似的粒子轨迹发射器
                if (tick % 60 == 0 && tick != 0) {
                    playDragonSoundOnce(
                        runtime.spawnLevel,
                        runtime.conjurePosition,
                        SoundEvents.ENDER_DRAGON_GROWL,
                        SoundSource.HOSTILE,
                        18f,
                        0.7f + Random.nextFloat() * 0.2f,
                        256.0,
                    )
                    if (count == 0) {
                        ServerCameraUtil.sendShake(
                            runtime.spawnLevel, runtime.conjurePosition,
                            256.0, 0.5, 80, 8.0
                        )
                        // 发送发光， RGB分离
                        CooPostEffects.server.spawn(
                            runtime.spawnLevel,
                            UsefulMagicPostEffects.rgbDashBlur(20, 2f, 2f, 2f)
                        )
                    }
                    count++
                }

                // 先做框架测试 暂定100tick下一阶段
                if (tick > 120) {
                    runtime.state = LaserCharging()
                }
            }

            override fun start(runtime: DragonSpawner) {
                runtime["floor"] =
                    MagicDragonSpawningFloorComposition(runtime.conjurePosition, runtime.spawnLevel).apply {
                        ParticleCompositionManager.spawn(this)
                    }

                runtime["rune"] =
                    MagicDragonSpawnRuneComposition(runtime.conjurePosition, runtime.spawnLevel).apply {
                        ParticleCompositionManager.spawn(this)
                    }
            }

            override fun end(runtime: DragonSpawner, tick: Int) {
            }
        }

        class LaserCharging : State {
            val count = 10
            // 定义一下常量偏移
            val offsets = ArrayDeque<Vec3>(
                PointsBuilder()
                    .addDiscreteCircleXZ(64.0, count, 12.0)
                    .createWithoutClone()
                    .map { it.toVector() }
            )

            var rewindDisplayed = false
            override fun tick(runtime: DragonSpawner, tick: Int) {
                val step = 40
                if (tick > step * count) {
                    val remainingCount = tick - step * count

                    if (tick % 60 == 0){
                        playDragonSoundOnce(
                            runtime.spawnLevel,
                            runtime.conjurePosition,
                            SoundEvents.ENDER_DRAGON_GROWL,
                            SoundSource.HOSTILE,
                            18f,
                            0.6f + Random.nextFloat() * 0.2f,
                            256.0,
                        )
                    }

                    if (!rewindDisplayed && remainingCount > 4 * 20) {
                        rewindDisplayed = true
                        ServerSoundManager.instance(
                            UsefulMagicSoundEvents.DRAGON_REWIND.get(),
                            SoundSource.HOSTILE
                        )
                            .world(runtime.spawnLevel)
                            .position(runtime.conjurePosition)
                            .layer("rewind")
                            .visibleRange(256.0)
                            .volumeFalloff(SoundVolumeFalloff.QUADRATIC)
                            .syncEveryTick(true)
                            .relative()
                            .spawn()
                        runtime["rewind"] = true
                        val dragonPos = runtime.conjurePosition.add(0.0, 64.0, 0.0)
                        runtime["rewind_compositions"] = MagicRuneRingEffects.spawnRewindRings(
                            dragonPos,
                            runtime.spawnLevel
                        )
                        runtime.invokeAsIfPrecent<Animate>("collect_emitter_animate") {
                            it.cancel()
                        }

                    }
                    if (remainingCount > 20 * 8) {
                        runtime.state = End()
                    }
                    return
                }
                if (tick % step != 0 || offsets.isEmpty()) {
                    return
                }

                val spawnPos = runtime.conjurePosition
                val dragonPos = spawnPos.add(0.0, 64.0, 0.0)
                val world = runtime.spawnLevel


                val animates = runtime.getAsIsInstance<MutableList<Animate>>("animates") ?: ArrayList()
                // 先定义一下animate
                val offsetPos = offsets.removeFirst().add(dragonPos).add(0.0, -32.0, 0.0)
                // 这里应该播放类似低音 传送的效果
                playDragonSoundOnce(
                    world,
                    offsetPos,
                    UsefulMagicSoundEvents.DRAGON_MAGIC_APPEAR.get(),
                    SoundSource.HOSTILE,
                    3f,
                    0.9f + Random.nextFloat() * 0.3f,
                    256.0,
                )
                val animate = Animate()
                    .addNode(
                        AnimateNode()
                            .addAction(
                                CompositionAction(
                                    MagicDragonSpawnLaserComposition(offsetPos, world).apply {
                                        direction = (this.position - dragonPos).asRelative()
                                    }
                                )
                            )
                            .addAction(
                                RenderAction(StraightLaserRenderEntity(world, offsetPos).apply {
                                    lifetime = 11451419
                                    maxRadius = 1.5f
                                    this.phaseTicks = 10
                                    this.setColor(
                                        Math3DUtil.colorOf(255, 120, 240)
                                    )
                                    this.renderRange = 512.0
                                    this.alpha = 0.9
                                    this.brightness = 0.6f
                                    updateBeam(offsetPos - (dragonPos - offsetPos).normalize() * 2, dragonPos)
                                })
                                    .cancelMethod {
                                        it.discard()
                                    }
                            )
                    )
                animates.add(animate)
                AnimateManager.displayAnimateServer(animate)
                runtime["animates"] = animates
            }

            override fun start(runtime: DragonSpawner) {
            }

            override fun end(runtime: DragonSpawner, tick: Int) {

            }

        }

        class End : State {

            override fun tick(
                runtime: DragonSpawner,
                tick: Int
            ) {
                runtime.state = null
            }

            override fun start(runtime: DragonSpawner) {
                // 生成龙
                val dragon = MagicDragonEntity(runtime.spawnLevel)
                dragon.setPos(runtime.conjurePosition.add(0.0, 64.0, 0.0))
                // 他的锚点位置不能是他的首次生成位置
                // 但是他需要在64.0 这里进行首个技能的释放
                dragon.spawnPosition = runtime.conjurePosition.add(0.0, 32.0, 0.0)
                dragon.resetDefaultHealth()
                runtime.spawnLevel.addFreshEntity(dragon)

                ServerCameraUtil.sendShake(
                    runtime.spawnLevel, runtime.conjurePosition,
                    256.0, 4.0, 80, 8.0
                )

                // 爆炸效果
                val entity = DragonMagicBallRenderEntity(runtime.spawnLevel, dragon.position())
                    .apply {
                        this.color = Math3DUtil.colorOf(255, 120, 240)
                        this.brightness = 1f
                        this.surfaceBrightness = 1.3
                        this.bloomEnabled = true
                        this.renderRange = 512.0
                        this.size = 256f // 覆盖整个岛屿
                        this.discardTick = 10
                        this.growingTick = 5
                        ServerRenderEntityManager.spawn(this)
                    }

                submitTaskServer(5) {
                    entity.discard(false)
                }

                // 爆炸音效
                playDragonSoundOnce(
                    dragon,
                    UsefulMagicSoundEvents.MAGIC_EXPLODE.get(),
                    SoundSource.HOSTILE,
                    1f,
                    1f,
                    256.0,
                )
                CooPostEffects.server.spawn(
                    runtime.spawnLevel, UsefulMagicPostEffects.flameExplodeFlash(20, 2f, 1f, Vec3(1.0, 1.0, 0.8))
                )
            }

            override fun end(runtime: DragonSpawner, tick: Int) {
                runtime.invokeAsIfPrecent<MagicDragonSpawningFloorComposition>("floor") {
                    it.remove()
                }
                runtime.invokeAsIfPrecent<MutableList<Animate>>("animates") {
                    it.forEach {
                        it.cancel()
                    }
                }
                runtime.invokeAsIfPrecent<Animate>("emitter_spawning_animate") {
                    it.cancel()
                }
                runtime.invokeAsIfPrecent<ServerManagedSoundInstance>("looping_ball") {
                    it.fadeTo(0f, 20)
                }
                runtime.invokeAsIfPrecent<MagicDragonSpawnRuneComposition>("rune") {
                    it.remove()
                }
                runtime.invokeAsIfPrecent<ArrayList<MagicRuneRingComposition>>("rewind_compositions") {
                    it.forEach { com ->
                        com.remove()
                    }
                }

                // 二次调用， 防止因为特殊原因导致取消， 然后残留动画
                runtime.invokeAsIfPrecent<Animate>("collect_emitter_animate") {
                    it.cancel()
                }
            }

        }

    }

    var params = HashMap<String, Any>()

    var tick = 0
    var state: State? = null
        set(value) {
            field?.end(this, tick)
            field = value
            tick = 0
            value?.start(this)
        }


    fun cancel() {
        state = null
        params.forEach { (key, value) ->
            when (value) {
                is Controlable<*> -> value.remove()
                is Animate -> value.cancel()
            }
        }
    }

    inline fun <reified T> invokeAsIfPrecent(key: String, consumer: Consumer<T>) {
        val entry = params[key] ?: return
        entry.runAsIfType<T> {
            consumer.accept(this)
        }
    }

    inline fun <reified T> getAsIsInstance(key: String): T? {
        val entry = params[key] ?: return null
        return entry as? T
    }

    operator fun set(key: String, value: Any?) {
        value ?: return
        params[key] = value
    }


    fun tick() {
        state?.tick(this, tick++)
    }

    fun start() {
        states[UUID.randomUUID()] = this
        state = Start()
    }
}
