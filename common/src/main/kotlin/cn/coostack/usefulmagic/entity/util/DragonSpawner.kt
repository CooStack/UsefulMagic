package cn.coostack.usefulmagic.entity.util

import cn.coostack.cooparticlesapi.animation.Animate
import cn.coostack.cooparticlesapi.animation.AnimateManager
import cn.coostack.cooparticlesapi.animation.AnimateNode
import cn.coostack.cooparticlesapi.network.particle.composition.manager.ParticleCompositionManager
import cn.coostack.cooparticlesapi.renderer.server.ServerRenderEntityManager
import cn.coostack.cooparticlesapi.sound.ServerManagedSoundInstance
import cn.coostack.cooparticlesapi.sound.ServerSoundManager
import cn.coostack.cooparticlesapi.sound.SoundVolumeFalloff
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.ServerCameraUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.animate.CompositionAction
import cn.coostack.usefulmagic.animate.RenderAction
import cn.coostack.usefulmagic.entity.custom.MagicDragonEntity
import cn.coostack.usefulmagic.particles.entity.dragon.composition.lifecycle.spawn.MagicDragonSpawnLaserComposition
import cn.coostack.usefulmagic.particles.entity.dragon.composition.lifecycle.spawn.MagicDragonSpawningFloorComposition
import cn.coostack.usefulmagic.renderer.DragonMagicBallRenderEntity
import cn.coostack.usefulmagic.renderer.StraightLaserRenderEntity
import cn.coostack.usefulmagic.sounds.UsefulMagicSoundEvents
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import kotlin.random.Random

/**
 * 要实现一种类似状态机的效果
 * - 参考 Ender Dragon Fight
 *
 * 只考虑服务器
 *
 * @property spawnPosition
 * @property spawnLevel
 */
class DragonSpawner(val spawnPosition: Vec3, val spawnLevel: ServerLevel) {
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

        fun tick() {
            val iterator = states.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next().value
                if (next.state == null) {
                    iterator.remove()
                    continue
                }
                next.tick()
            }
        }


        class Start : State {

            override fun tick(runtime: DragonSpawner, tick: Int) {
                ServerSoundManager.instance(
                    UsefulMagicSoundEvents.DRAGON_SPAWN_MAGIC_LASER.get(),
                    SoundSource.HOSTILE
                )
                    .world(runtime.spawnLevel)
                    .position(runtime.spawnPosition)
                    .layer("spawning")
                    .uniqueKey(UUID.randomUUID().toString())
                    .visibleRange(256.0)
                    .relative()
                    .volume(1f)
                    .pitch(1f)
                    .spawn()
                runtime["looping_ball"] = ServerSoundManager.instance(
                    UsefulMagicSoundEvents.LIQUID_BALL.get(),
                    SoundSource.HOSTILE
                )
                    .world(runtime.spawnLevel)
                    .position(runtime.spawnPosition)
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

                runtime["spawn_laser"] = StraightLaserRenderEntity(runtime.spawnLevel, runtime.spawnPosition).apply {
                    lifetime = 30
                    maxRadius = 5f
                    this.phaseTicks = 20
                    this.setColor(
                        Math3DUtil.colorOf(255, 120, 240)
                    )
                    this.renderRange = 512.0
                    this.alpha = 1.0
                    this.brightness = 1.2f
                    updateBeam(runtime.spawnPosition.add(0.0, 256.0, 0.0), runtime.spawnPosition.add(0.0, -128.0, 0.0))

                    ServerRenderEntityManager.spawn(this)
                }

                ServerCameraUtil.sendShake(
                    runtime.spawnLevel, runtime.spawnPosition,
                    256.0, 4.0, 80, 8.0
                )

                runtime.state = Magic()
            }

            override fun start(runtime: DragonSpawner) {
            }

            override fun end(runtime: DragonSpawner, tick: Int) {
            }
        }

        /**
         * 释放魔法的过程
         */
        class Magic : State {

            override fun tick(runtime: DragonSpawner, tick: Int) {
                // 等待多少个tick之后转移到下一个-
                // 大概的设计是 龙生成的位置会先生成一个球  TODO（魔法球， 有纹路各种的 （纹理+泛光））
                // 然后他的半径 64个位置 Y随机偏移 -10 - +20 生成对应的魔法阵， 召唤激光
                // 全部召唤完成后进入下一个阶段 State
                // 激光周围会有类似的粒子轨迹发射器


                // 先做框架测试 暂定100tick下一阶段
                if (tick > 120) {
                    runtime.state = LaserCharging()
                }
            }

            override fun start(runtime: DragonSpawner) {
                runtime["floor"] =
                    MagicDragonSpawningFloorComposition(runtime.spawnPosition, runtime.spawnLevel).apply {
                        ParticleCompositionManager.spawn(this)
                    }

                runtime["magic"] =
                    DragonMagicBallRenderEntity(runtime.spawnLevel, runtime.spawnPosition.add(0.0, 64.0, 0.0))
                        .apply {
                            size = 5f
                            setColor(Math3DUtil.colorOf(255, 100, 240))
                            ServerRenderEntityManager.spawn(this)
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
                    if (!rewindDisplayed && remainingCount > 4 * 20) {
                        rewindDisplayed = true
                        ServerSoundManager.instance(
                            UsefulMagicSoundEvents.DRAGON_REWIND.get(),
                            SoundSource.HOSTILE
                        )
                            .world(runtime.spawnLevel)
                            .position(runtime.spawnPosition)
                            .layer("rewind")
                            .visibleRange(128.0)
                            .volumeFalloff(SoundVolumeFalloff.QUADRATIC)
                            .syncEveryTick(true)
                            .relative()
                            .spawn()
                    }
                    if (remainingCount > 20 * 8) {
                        runtime.state = End()
                    }
                    return
                }
                if (tick % step != 0 || offsets.isEmpty()) {
                    return
                }

                val spawnPos = runtime.spawnPosition
                val dragonPos = spawnPos.add(0.0, 64.0, 0.0)
                val world = runtime.spawnLevel


                val animates = runtime.getAsIsInstance<MutableList<Animate>>("animates") ?: ArrayList()
                // 先定义一下animate
                val offsetPos = offsets.removeFirst().add(dragonPos).add(0.0, -32.0, 0.0)
                // 这里应该播放类似低音 传送的效果
                ServerSoundManager.instance(
                    UsefulMagicSoundEvents.DRAGON_MAGIC_APPEAR.get(),
                    SoundSource.HOSTILE
                ).world(world)
                    .position(offsetPos)
                    .volume(0.3f)
                    .pitch(0.9f + Random.nextFloat() * 0.3f)
                    .visibleRange(256.0)
                    .uniqueKey(UUID.randomUUID().toString())
                    .layer("active")
                    .spawn()
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
                dragon.setPos(runtime.spawnPosition.add(0.0, 64.0, 0.0))
                // 他的锚点位置不能是他的首次生成位置
                // 但是他需要在64.0 这里进行首个技能的释放
                dragon.spawnPosition = runtime.spawnPosition.add(0.0, 32.0, 0.0)
                runtime.spawnLevel.addFreshEntity(dragon)


                ServerCameraUtil.sendShake(
                    runtime.spawnLevel, runtime.spawnPosition,
                    256.0, 4.0, 80, 8.0
                )

                // 爆炸音效

            }

            override fun end(runtime: DragonSpawner, tick: Int) {
                runtime.invokeAsIfPrecent<MagicDragonSpawningFloorComposition>("floor") {
                    it.remove()
                }
                runtime.invokeAsIfPrecent<DragonMagicBallRenderEntity>("magic") {
                    it.discard(true)
                }
                runtime.invokeAsIfPrecent<MutableList<Animate>>("animates") {
                    it.forEach {
                        it.cancel()
                    }
                }
                runtime.invokeAsIfPrecent<ServerManagedSoundInstance>("looping_ball") {
                    it.fadeTo(0f, 20)
                }
            }

        }


        val START = Start()
    }

    var params = HashMap<String, Any>()

    var tick = 0
    var state: State? = START
        set(value) {
            field?.end(this, tick)
            field = value
            tick = 0
            value?.start(this)
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
    }
}
