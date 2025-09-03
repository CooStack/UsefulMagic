package cn.coostack.usefulmagic.particles.style.wand.starry

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.network.particle.style.SequencedParticleStyle
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.GraphMathHelper
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.cooparticlesapi.utils.helper.StatusHelper
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBuffer
import cn.coostack.cooparticlesapi.utils.helper.buffer.ControlableBufferHelper
import cn.coostack.usefulmagic.extend.isOf
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.Random
import java.util.SortedMap
import java.util.TreeMap
import java.util.UUID
import kotlin.math.PI

/**
 * 释放时的法杖 (朝向在释放时锁定)
 */
class StarrySpellcasterMagicStyle(uuid: UUID = UUID.randomUUID()) :
    SequencedParticleStyle(256.0, uuid) {
    val status = HelperUtil.styleStatus(20)

    @ControlableBuffer("direction")
    var direction = Vec3(0.0, 1.0, 0.0)

    @ControlableBuffer("age")
    var age = 0

    @ControlableBuffer("player_uuid")
    var player: UUID = UUID.randomUUID()

    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            return StarrySpellcasterMagicStyle(uuid)
        }
    }

    init {
        HelperUtil.styleSequencedAnimationHelper<StarrySpellcasterMagicStyle>()
            .loadStyle(this)
            .addAnimate({ this.age > 1 }, 1)
            .addAnimate({ this.age > 10 }, 2)
        status.loadControler(this)
    }


    override fun getParticlesCount(): Int {
        return 3
    }

    val random = Random(System.currentTimeMillis())
    override fun getCurrentFramesSequenced(): SortedMap<SortedStyleData, RelativeLocation> {
        val res = TreeMap<SortedStyleData, RelativeLocation>()
        var order = 0
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder().addPolygonInCircle(3, 25 * ParticleOption.getParticleCounts(), 7.0)
                                .addBuilder(
                                    RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder().addPolygonInCircle(3, 25 * ParticleOption.getParticleCounts(), 7.0)
                                        .rotateAsAxis(0.3333333333333333 * PI, RelativeLocation.yAxis())
                                )
                                .addCircle(5.0, 180)
                                .addBuilder(
                                    RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder().addPolygonInCircle(3, 10 * ParticleOption.getParticleCounts(), 3.5)
                                        .rotateAsAxis(0.16666666666666666 * PI, RelativeLocation.yAxis())
                                )
                                .addBuilder(
                                    RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder().addPolygonInCircle(3, 10 * ParticleOption.getParticleCounts(), 3.5)
                                        .rotateAsAxis(0.5 * PI, RelativeLocation.yAxis())
                                )
                                .addPolygonInCircle(4, 25 * ParticleOption.getParticleCounts(), 10.0)
                                .addBuilder(
                                    RelativeLocation(0.0, 0.0, 0.0),
                                    PointsBuilder().addPolygonInCircle(4, 25 * ParticleOption.getParticleCounts(), 10.0)
                                        .rotateAsAxis(0.25 * PI, RelativeLocation.yAxis())
                                )
                        ) { rel ->
                            getSingle(rel)
                                .build()
                        }.loadScaleHelper(0.01, 1.0, 20).toggleOnDisplay {
                            val alpha = HelperUtil.alphaStyle(0.1, 1.0, 20)
                            alpha.loadControler(this)
                            addPreTickAction {
                                if (status.displayStatus == 1) {
                                    scaleHelper?.doScale()
                                    alpha.increaseAlpha()
                                } else {
                                    alpha.decreaseAlpha()
                                }
                            }
                            this.addPreTickAction {
                                rotateToWithAngle(RelativeLocation.of(direction), PI / 32)
                            }
                        }
                )
            }, order++)
        ] = RelativeLocation()
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(12.0, 120)
                        ) { rel ->
                            getSingle(rel)
                                .displayer {
                                    ParticleDisplayer.withSingle(
                                        ControlableEnchantmentEffect(it)
                                    )
                                }
                                .addParticleHandler {
                                    this.currentAge = random.nextInt(this.lifetime)
                                    this.size = 0.6f
                                }
                                .build()
                        }.toggleOnDisplay {
                            val alpha = HelperUtil.alphaStyle(0.1, 1.0, 20)
                            alpha.loadControler(this)
                            addPreTickAction {
                                if (status.displayStatus == 1) {
                                    alpha.increaseAlpha()
                                } else {
                                    alpha.decreaseAlpha()
                                }
                            }
                            this.addPreTickAction {
                                rotateToWithAngle(RelativeLocation.of(direction), -PI / 64)
                            }
                        }
                )
            }, order++)
        ] = RelativeLocation()
        res[
            SortedStyleData({
                ParticleDisplayer.withStyle(
                    ParticleShapeStyle(it)
                        .appendBuilder(
                            PointsBuilder()
                                .addCircle(11.0, 120 * ParticleOption.getParticleCounts())
                                .addCircle(13.0, 120 * ParticleOption.getParticleCounts())
                        ) { rel ->
                            getSingle(rel)
                                .addParticleHandler {
                                    this.size = 0.3f
                                }
                                .build()
                        }.toggleOnDisplay {
                            val alpha = HelperUtil.alphaStyle(0.1, 1.0, 20)
                            alpha.loadControler(this)
                            addPreTickAction {
                                if (status.displayStatus == 1) {
                                    alpha.increaseAlpha()
                                } else {
                                    alpha.decreaseAlpha()
                                }
                            }
                            this.addPreTickAction {
                                rotateToWithAngle(RelativeLocation.of(direction), PI / 64)
                            }
                        }
                )
            }, order++)
        ] = RelativeLocation()


        return res
    }


    private fun getSingle(it: RelativeLocation): StyleDataBuilder {
        return StyleDataBuilder()
            .addParticleHandler {
                val start = Vector3f(1f, 0f, 0f)
                color = start
                textureSheet = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
            }.addParticleControlerHandler {
                this.addPreTickAction {
                    val final = Vector3f(1f, 120 / 255f, 100 / 255f)
                    val start = Vector3f(1f, 0f, 0f)
                    val length = this.loc.distanceTo(this@StarrySpellcasterMagicStyle.pos)
                    val current = GraphMathHelper.lerp(length / 13f, start, final)
                    this.color = current
                }
            }
    }

    override fun writePacketArgsSequenced(): Map<String, ParticleControlerDataBuffer<*>> {
        return HashMap<String, ParticleControlerDataBuffer<*>>().apply {
            putAll(status.toArgsPairs())
            putAll(ControlableBufferHelper.getPairs(this@StarrySpellcasterMagicStyle))
        }
    }

    override fun readPacketArgsSequenced(args: Map<String, ParticleControlerDataBuffer<*>>) {
        ControlableBufferHelper.setPairs(this, args)
        status.readFromServer(args)
    }

    override fun onDisplay() {
        addPreTickAction {
            age++
            // Debug
            val playerEntity = world!!.getPlayerByUUID(player) ?: let {
                remove()
                return@addPreTickAction
            }
            teleportTo(playerEntity.position())
            if (client) return@addPreTickAction
            if (!playerEntity.useItem.isOf(UsefulMagicItems.STARRY_WAND)) {
                status.setStatus(StatusHelper.Status.DISABLE)
            }
        }
    }

}