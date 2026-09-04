package cn.coostack.usefulmagic.entity.custom.dragon.skills.barrage

import cn.coostack.cooparticlesapi.api.controler.server.ServerControler
import cn.coostack.cooparticlesapi.barrages.BarrageHitResult
import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.barrages.BarrageOption
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.network.particle.data.minRangeTo
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.barrages.api.EntityMagicDamagedBarrage
import cn.coostack.usefulmagic.particles.emitters.magic.BarrageTailEmitter
import cn.coostack.usefulmagic.utils.EntityUtil
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import cn.coostack.cooparticlesapi.extend.*
import kotlin.random.Random


/**
 * 朝着目标发射，N秒后进行爆炸
 */
class DragonSplitBarrage(loc: Vec3, world: ServerLevel, damage: Double, shooter: LivingEntity) :
    EntityMagicDamagedBarrage(loc, world, BarrageOption(), damage, shooter) {
    private var tick = 0
    private var explode = false
    var explodeTick = 20 * 6

    var splitCount = 20 minRangeTo 40

    override fun onHitDamaged(result: BarrageHitResult) {
    }

    override fun tick() {
        super.tick()
        if (tick++ >= explodeTick && !explode) {
            explode = true
            exploding()
        }
    }

    fun exploding() {
        // 召唤分裂的split （tracker)
        val target = if (shooter is Mob) (shooter as Mob).target else null
        val box = AABB.ofSize(loc, 256.0, 256.0, 256.0)
        val entities = world.getEntitiesOfClass<LivingEntity>(LivingEntity::class.java, box, this::filterHitEntity)

        PointsBuilder()
            .addCircle(1.0, splitCount.random())
            .createWithoutClone()
            .forEach {
                // 因为这个返回的肯定是连 target也没有， entities搜索也是空结果
                // 要的是一个四散开来的， 然后等一会直接加速朝着玩家发射的一个效果
                val finalTarget = entities.randomOrNull() ?: target ?: return
                val tracked = DragonTrackedBarrage(finalTarget, loc, world, damage, shooter!!)
                    .apply {
                        trackingInternal = 30 + Random.nextInt(10, 20)
                        trackingMaxSpeed = 6.0
                        trackingForce = 0.8 + Random.nextDouble(0.3, 0.5)
                        trackingTick = 10 + Random.nextInt(5, 10)
                    }
                tracked.direction = it.toVector().offsetRandomly(0.5).normalize()
                // 朝着四周发散
                tracked.options.enableSpeedWithOptions(0.8)
                    .maxLivingTick(60)
                BarrageManager.spawn(tracked)
            }
        hit(BarrageHitResult())
    }

    override fun filterHitEntity(livingEntity: LivingEntity): Boolean {
        return EntityUtil.filterDragon.test(livingEntity)
    }

    override fun createHitBox(): HitBox {
        return HitBox.of(0.0, 0.0, 0.0)
    }

    override fun createControler(): ServerControler<*> {
        return BarrageTailEmitter(loc, world).apply {
            this.randomData.apply {
                minAge = 10
                maxAge = 30
                minCount = 10
                maxCount = 30
            }
        }
    }

    override fun onHit(result: BarrageHitResult) {
    }
}
