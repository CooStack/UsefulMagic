package cn.coostack.usefulmagic.particles.style.barrage.wand

import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffer
import cn.coostack.cooparticlesapi.network.buffer.ParticleControlerDataBuffers
import cn.coostack.cooparticlesapi.network.particle.style.ParticleGroupStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleShapeStyle
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleProvider
import cn.coostack.cooparticlesapi.particles.ParticleDisplayer
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.cooparticlesapi.utils.helper.HelperUtil
import cn.coostack.usefulmagic.items.UsefulMagicItems
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.util.math.Vec3d
import java.util.UUID
import kotlin.math.PI

class GoldenWandStyle(val usingPlayerUUID: UUID, uuid: UUID = UUID.randomUUID()) :
    ParticleGroupStyle(64.0, uuid) {
    class Provider : ParticleStyleProvider {
        override fun createStyle(
            uuid: UUID,
            args: Map<String, ParticleControlerDataBuffer<*>>
        ): ParticleGroupStyle {
            val player = args["using_player"]!!.loadedValue as UUID
            return GoldenWandStyle(player, uuid).also {
                it.readPacketArgs(args)
            }
        }
    }


    val statusHelper = HelperUtil.styleStatus(10)

    init {
        statusHelper.loadControler(this)
    }

    override fun getCurrentFrames(): Map<StyleData, RelativeLocation> {
        val res = HashMap<StyleData, RelativeLocation>()
        res[StyleData {
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(uuid = it)
                    .appendBuilder(
                        PointsBuilder()
                            .addCircle(4.0, 60)
                            .addCircle(4.5, 60)
                            .addCircle(2.0, 30)
                    ) {
                        StyleData { sIt ->
                            ParticleDisplayer.withSingle(ControlableCloudEffect(sIt))
                        }.withParticleHandler {
                            size = 0.2f
                            colorOfRGBA(255, 255, 0, 0.5f)
                            textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
                        }
                    }
                    .loadScaleHelperBezierValue(
                        1 / 10.0,
                        1.0,
                        10,
                        RelativeLocation(30.0, 0.0, 0.0),
                        RelativeLocation(-0.5, -0.9, 0.0)
                    )
                    .toggleBeforeDisplay { map ->
                        val usingPlayer = world!!.getPlayerByUuid(usingPlayerUUID)
                        this.preRotateTo(
                            map,
                            RelativeLocation.of(usingPlayer?.rotationVector ?: Vec3d(0.0, 1.0, 0.0))
                        )
                    }.toggleOnDisplay {
                        this.addPreTickAction {
                            rotateParticlesAsAxis(PI / 72)
                            val usingPlayer = world!!.getPlayerByUuid(usingPlayerUUID) ?: return@addPreTickAction
                            if (statusHelper.displayStatus == 2) {
                                scaleReversed(false)
                                return@addPreTickAction
                            } else {
                                fastRotateToPlayerView(usingPlayer)
                            }
                        }
                    }
            )
        }
        ] = RelativeLocation(0.0, 5.0, 0.0)
        res[StyleData {
            ParticleDisplayer.withStyle(
                ParticleShapeStyle(uuid = it)
                    .appendBuilder(
                        PointsBuilder()
                            .addPolygonInCircle(4, 20, 3.0)
                            .rotateAsAxis(PI / 4)
                            .addPolygonInCircle(4, 20, 3.0)
                    ) {
                        StyleData { sIt ->
                            ParticleDisplayer.withSingle(ControlableCloudEffect(sIt))
                        }.withParticleHandler {
                            size = 0.1f
                            colorOfRGBA(255, 255, 0, 0.5f)
                            textureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
                        }
                    }
                    .loadScaleHelperBezierValue(
                        1 / 10.0,
                        1.0,
                        10,
                        RelativeLocation(30.0, 0.0, 0.0),
                        RelativeLocation(-0.5, -0.9, 0.0)
                    )
                    .toggleBeforeDisplay { map ->
                        val usingPlayer = world!!.getPlayerByUuid(usingPlayerUUID)
                        this.preRotateTo(
                            map,
                            RelativeLocation.of(usingPlayer?.rotationVector ?: Vec3d(0.0, 1.0, 0.0))
                        )
                    }.toggleOnDisplay {
                        this.addPreTickAction {
                            this.rotateParticlesAsAxis(-PI / 72)
                            val usingPlayer = world!!.getPlayerByUuid(usingPlayerUUID) ?: return@addPreTickAction
                            if (statusHelper.displayStatus == 2) {
                                scaleReversed(false)
                                return@addPreTickAction
                            } else {
                                fastRotateToPlayerView(usingPlayer)
                            }
                        }
                    }
            )
        }] = RelativeLocation(0.0, 5.0, 0.0)
        return res
    }


    override fun beforeDisplay(styles: Map<StyleData, RelativeLocation>) {
        val usingPlayer = world!!.getPlayerByUuid(usingPlayerUUID) ?: return
        preRotateTo(
            styles,
            RelativeLocation.of(usingPlayer.rotationVector)
        )
    }


    override fun onDisplay() {
        addPreTickAction {
            val usingPlayer = world!!.getPlayerByUuid(usingPlayerUUID) ?: return@addPreTickAction
            rotateParticlesToPoint(RelativeLocation.of(usingPlayer.rotationVector))
            teleportTo(usingPlayer.eyePos)
            if (!client) {
                if (statusHelper.displayStatus != 2 && !usingPlayer.activeItem.isOf(UsefulMagicItems.GOLDEN_WAND)) {
                    statusHelper.setStatus(2)
                }
            }
        }
    }

    override fun writePacketArgs(): Map<String, ParticleControlerDataBuffer<*>> {
        return mapOf(
            "using_player" to ParticleControlerDataBuffers.uuid(usingPlayerUUID),
            *statusHelper.toArgsPairs().toTypedArray()
        )
    }

    override fun readPacketArgs(args: Map<String, ParticleControlerDataBuffer<*>>) {
        statusHelper.readFromServer(args)
    }
}