package cn.coostack.usefulmagic.formation

import cn.coostack.cooparticlesapi.barrages.BarrageManager
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.entity.formation.EnergyCrystalsBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.RecoverCrystalsBlockEntity
import cn.coostack.usefulmagic.entity.custom.formation.FormationCoreEntity
import cn.coostack.usefulmagic.formation.api.AttackCrystal
import cn.coostack.usefulmagic.formation.api.BlockFormation
import cn.coostack.usefulmagic.formation.api.DefendCrystal
import cn.coostack.usefulmagic.formation.api.FormationCrystal
import cn.coostack.usefulmagic.formation.api.FormationScale
import cn.coostack.usefulmagic.formation.api.FormationSettings
import cn.coostack.usefulmagic.formation.api.FormationTargetOption
import cn.coostack.usefulmagic.formation.target.BarrageTargetOption
import cn.coostack.usefulmagic.formation.target.LivingEntityTargetOption
import cn.coostack.usefulmagic.formation.target.MeteoriteEntityTargetOption
import cn.coostack.usefulmagic.formation.target.ProjectileEntityTargetOption
import cn.coostack.usefulmagic.managers.server.ServerFormationManager
import cn.coostack.usefulmagic.meteorite.MeteoriteFallingBlockEntity
import cn.coostack.usefulmagic.meteorite.MeteoriteManager
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationBreak
import cn.coostack.usefulmagic.packet.s2c.PacketS2CFormationCreate
import cn.coostack.usefulmagic.particles.barrages.api.DamagedBarrage
import cn.coostack.usefulmagic.particles.emitters.CircleEmitters
import cn.coostack.usefulmagic.particles.emitters.LightningParticleEmitters
import cn.coostack.usefulmagic.particles.style.formation.FormationStyle
import cn.coostack.usefulmagic.particles.style.formation.LargeFormationStyle
import cn.coostack.usefulmagic.particles.style.formation.MidFormationStyle
import cn.coostack.usefulmagic.particles.style.formation.SmallFormationStyle
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.FishEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.UUID
import kotlin.math.roundToInt

class CrystalFormation(override var world: World?, override var owner: UUID?, override var formationCore: Vec3d) :
    BlockFormation {
    var bindEntity: FormationCoreEntity? = null
        private set
    var style: FormationStyle? = null
    override var uuid: UUID = UUID.randomUUID()
        internal set
    override var activeCrystals: MutableList<FormationCrystal> = ArrayList()
    internal var active = false
    var hasDefend = false
    var hasAttack = false
    var hasRecover = false

    // 绑定对应的生物
    var formationHealth = 10f
    val settings = FormationSettings()
    override fun breakFormation(
        damage: Float,
        attackerOption: FormationTargetOption?
    ) {
        if (!active) return
        formationHealth -= damage
        // 服务器粒子生成与效果展示
        if (world!!.isClient) return
        val breakPacket = PacketS2CFormationBreak(BlockPos.ofFloored(formationCore), damage)
        world!!.players.forEach {
            it as ServerPlayerEntity
            ServerPlayNetworking.send(it, breakPacket)
        }
        attackerOption?.let {
            val pos = attackerOption.pos()
            val dir = formationCore.relativize(pos)
            val emitters =
                CircleEmitters(pos, world)
            emitters.apply {
                maxTick = 1
                templateData.also {
                    it.effect = ControlableCloudEffect(uuid)
                    it.size = 0.1f
                    it.color = Math3DUtil.colorOf(147, 242, 255)
                }
                circleSpeed = 0.8
                precentDrag = 0.6
                circleDirection = dir
            }
            ParticleEmittersManager.spawnEmitters(emitters)
        }
        world!!.playSound(
            null, formationCore.x, formationCore.y, formationCore.z,
            SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.BLOCKS, 10F, 1F
        )
        if (formationHealth <= 0f) {
            world!!.playSound(
                null,
                formationCore.x,
                formationCore.y,
                formationCore.z,
                SoundEvents.BLOCK_BEACON_DEACTIVATE,
                SoundCategory.BLOCKS,
                10f,
                1f
            )
            scale = FormationScale.NONE
            owner = null
            active = false
            activeCrystals.onEach { it.activeFormation = null }.clear()
            formationHealth = 10f
            inTriggerRangeActive = false
            triggerTime = 0
        }
    }

    var scale = FormationScale.NONE

    override fun hasCrystalType(type: Class<out FormationCrystal>): Boolean {
        return activeCrystals.any {
            type.isAssignableFrom(it::class.java)
        }
    }

    override fun getFormationTriggerRange(): Double {
        return when (scale) {
            FormationScale.NONE -> 0.0
            FormationScale.SMALL -> 16.0
            FormationScale.MID -> 32.0
            FormationScale.LARGE -> 64.0
        }
    }


    override fun getFormationScale(): FormationScale {
        return scale
    }

    override fun isFriendly(option: FormationTargetOption): Boolean {
        val optionOwnerUUID = option.getOwnerUUID()
        owner ?: return optionOwnerUUID == null
        optionOwnerUUID ?: return false


        if (world!!.isClient) {
            return true
        }

        val data = UsefulMagic.state.getDataFromServer(owner!!)

        if (option is LivingEntityTargetOption) {
            val target = option.target
            val hostile = target is HostileEntity
            if (hostile && !settings.hostileEntityAttack) return true
            val animal = target is AnimalEntity || target is FishEntity
            if (animal && !settings.animalEntityAttack) return true
            if (target is PlayerEntity && !settings.playerEntityAttack) return true
            if (!animal && !hostile && !settings.anotherEntityAttack && target !is PlayerEntity) return true
            if (target.isSpectator || target.isInCreativeMode) {
                return true
            }
        }
        return data.isFriend(optionOwnerUUID)
    }

    override fun canBeFormation(): Boolean {
        return canBeFormation(FormationScale.SMALL)
    }

    override fun chunkLoaded(): Boolean {
        world ?: return false
        val blockPos = BlockPos.ofFloored(formationCore)
        val cx = ChunkSectionPos.getSectionCoord(blockPos.x)
        val cz = ChunkSectionPos.getSectionCoord(blockPos.z)
        return world!!.isChunkLoaded(cx, cz)
    }

    private fun canBeFormation(scale: FormationScale): Boolean {
        world ?: return false
        val structure = scale.structure.map {
            world!!.getBlockEntity(it.add(BlockPos.ofFloored(formationCore)))
        }
        val allCrystal = structure.all {
            if (it !is FormationCrystal) return@all false
            true
        }
        val hasSpecialCrystal = structure.count {
            it is AttackCrystal || it is DefendCrystal
        } > 0

        val hasEnergyCrystal = structure.count {
            it is EnergyCrystalsBlockEntity
        } > 0

        return allCrystal && hasEnergyCrystal && hasSpecialCrystal

    }

    override fun isActiveFormation(): Boolean {
        return active && formationHealth > 0f
    }

    /**
     * 在阵法成功构建时
     * 生成实体, 在FormationCore的上方
     */
    override fun createFormationEntity(): FormationCoreEntity {
        val entity = FormationCoreEntity(world!!)
        entity.core = BlockPos.ofFloored(formationCore)
        world!!.spawnEntity(entity)
        return entity
    }

    override fun tryBuildFormation(): Boolean {
        world ?: return false
        if (!canBeFormation() || isActiveFormation()) {
            return false
        }
        if (!world!!.isClient) {
            if (ServerFormationManager.checkPosInFormationRange(formationCore, world as ServerWorld)) {
                return false
            }
            // 发包 通知create
            val packet = PacketS2CFormationCreate(BlockPos.ofFloored(formationCore))
            world!!.players.forEach {
                ServerPlayNetworking.send(it as ServerPlayerEntity, packet)
            }
        }
        val stack = arrayListOf(FormationScale.SMALL, FormationScale.MID, FormationScale.LARGE).iterator()
        var updateScale = scale
        if (scale == FormationScale.NONE) {
            while (stack.hasNext()) {
                val current = stack.next()
                val canBeFormation = canBeFormation(current)
                if (!canBeFormation) {
                    break
                }
                updateScale = current
            }
            this.scale = updateScale
        }
        resetFormationHealth()
        updateScale.structure.forEach {
            val entity = world!!.getBlockEntity(it.add(BlockPos.ofFloored(formationCore))) as FormationCrystal
            entity.onFormationActive(this)
            activeCrystals.add(entity)
            if (entity is AttackCrystal) {
                hasAttack = true
            }
            if (entity is DefendCrystal) {
                hasDefend = true
            }
            if (entity is RecoverCrystalsBlockEntity) {
                hasRecover = true
            }
        }
        this.active = true
        onBuild()
        return true
    }

    /**
     * 在阵法构建时(加载,主动激活)
     * 都会执行这个方法
     */
    fun onBuild() {
        // 根据规模加载粒子组合
        if (world!!.isClient) {
            return
        }
        ServerFormationManager.onFormationActive(this)
        if (!settings.displayParticleOnlyTrigger && !world!!.isClient) {
            createStyleOnBuild()
            displayStyleOnBuild()
        }
    }

    private fun displayStyleOnBuild() {
        style?.let {
            it.formationPos = BlockPos.ofFloored(formationCore)
            ParticleStyleManager.spawnStyle(world!!, formationCore.add(0.0, -0.4, 0.0), it)
        }
    }

    private fun createStyleOnBuild() {
        when (scale) {
            FormationScale.SMALL -> {
                style = SmallFormationStyle()
            }

            FormationScale.MID -> {
                style = MidFormationStyle()
            }

            FormationScale.LARGE -> {
                style = LargeFormationStyle()
            }

            FormationScale.NONE -> {
            }
        }
    }

    var workTime = 0

    var inTriggerRangeActive = false
    var triggerTime = 0
    override fun tick() {
        val range = getFormationTriggerRange()
        if (!active) return
        world ?: return
        val isServer = !world!!.isClient
        activeCrystals.forEach(FormationCrystal::tick)
        val box = Box.of(this.formationCore, range * 2, range * 2, range * 2)
        var working = false
        var triggerRange = settings.triggerRange
        if (triggerRange < 0) {
            triggerRange = range
        }
        world!!.getEntitiesByClass(Entity::class.java, box) {
            if (it.pos.distanceTo(formationCore) > range) return@getEntitiesByClass false
            if (it.pos.distanceTo(formationCore) > triggerRange && !inTriggerRangeActive) return@getEntitiesByClass false
            when (it) {
                is ProjectileEntity -> {
                    !isFriendly(ProjectileEntityTargetOption(it))
                }

                is LivingEntity -> {
                    !isFriendly(LivingEntityTargetOption(it))
                }

                is MeteoriteFallingBlockEntity -> {
                    val m = MeteoriteManager.getFromSingleEntity(it) ?: return@getEntitiesByClass false
                    !isFriendly(MeteoriteEntityTargetOption(m))
                }

                else -> {
                    false
                }
            }
        }.forEach {
            triggerTime = 120
            inTriggerRangeActive = true
            working = true
            var option: FormationTargetOption? = when (it) {
                is ProjectileEntity -> {
                    ProjectileEntityTargetOption(it)
                }

                is LivingEntity -> {
                    LivingEntityTargetOption(it)
                }

                is MeteoriteFallingBlockEntity -> {
                    if (!isServer) return@forEach
                    val m = MeteoriteManager.getFromSingleEntity(it) ?: return@forEach
                    MeteoriteEntityTargetOption(m)
                }

                else -> null
            }
            if (option != null) {
                activeCrystals.forEach { crystal ->
                    if (!option!!.isValid()) {
                        return@forEach
                    }
                    option = crystal.handle(option)
                }
            }
        }
        if (isServer) {
            BarrageManager.collectClipBarrages(world as ServerWorld, box).filter {
                if (it.loc.distanceTo(formationCore) > range) {
                    return@filter false
                }
                if (it.loc.distanceTo(formationCore) > triggerRange && !inTriggerRangeActive) return@filter false
                val option = BarrageTargetOption(it)
                !isFriendly(option)
            }.forEach {
                working = true
                triggerTime = 120
                inTriggerRangeActive = true
                var option: FormationTargetOption = BarrageTargetOption(it)
                activeCrystals.forEach { crystal ->
                    if (!option.isValid()) {
                        return@forEach
                    }
                    option = crystal.handle(option)
                }
                // 判断option是否还存在
                // 判断option是否距离过近
                val barrageStillAlive = option.isValid() && option.pos()
                    .distanceTo(formationCore) <= 4.0 && option is BarrageTargetOption && option.target is DamagedBarrage
                if (barrageStillAlive) {
                    attack(option.target.damage.toFloat(), option, option.pos())
                }
            }
        }


        if (!checkIntact()) {
            breakFormation(Float.MAX_VALUE, null)
        }
        if (!isServer) return
        if (triggerTime-- <= 0) {
            triggerTime = 0
            inTriggerRangeActive = false
        }
        displayTime--
        if ((style == null || !(style?.valid ?: false))
            && (inTriggerRangeActive || !settings.displayParticleOnlyTrigger)
        ) {
            createStyleOnBuild()
            displayStyleOnBuild()
        }
        style ?: return
        if (!style!!.displayed) {
            return
        }
        if (working) {
            if (style!!.status != FormationStyle.FormationStatus.WORKING) {
                style!!.changeStatus(FormationStyle.FormationStatus.WORKING)
            }
            workTime = 60
        } else {
            if (workTime-- <= 0) {
                if (style!!.status != FormationStyle.FormationStatus.IDLE) {
                    style!!.changeStatus(FormationStyle.FormationStatus.IDLE)
                }
                workTime = 0
            }
        }
    }


    /**
     * 从其他水晶中调动能量值
     * @return 是否成功调动
     */
    override fun transformMana(requestCrystal: FormationCrystal, count: Int): Boolean {
        world ?: return false
        if (world!!.isClient) return false
        var removed = count
        val iterator = activeCrystals.filter { it is EnergyCrystalsBlockEntity }.iterator()
        var current = iterator.next() as EnergyCrystalsBlockEntity
        val response = ArrayList<EnergyCrystalsBlockEntity>()
        var nextBreak = false
        while (removed > 0) {
            if (current.currentMana < removed) {
                if (current.currentMana > 0) {
                    response.add(current)
                }
                removed -= current.currentMana
                current.decrease(current.currentMana)
                current = iterator.next() as EnergyCrystalsBlockEntity
            } else {
                current.decrease(removed)
                removed = 0
                response.add(current)
            }
            if (nextBreak) break
            if (!iterator.hasNext()) {
                nextBreak = true
            }
        }
        displayTransformManaParticle(requestCrystal, response)
        return true
    }

    override fun attack(damage: Float, who: FormationTargetOption?, attackedPos: Vec3d): Boolean {
        // 自己人不打自己人-+
        if (who != null) {
            if (isFriendly(who)) {
                return false
            }
        }
        if (!hasDefend) {
            // 直接攻击到阵法
            if (who !is LivingEntityTargetOption) {
                who?.damage(damage, world!!.damageSources.magic())
            }
            breakFormation(damage, who)
            return true
        }
        // 防御阵法在抵御实体的时候会当实体在外围
        val defend = activeCrystals.firstOrNull() { it is DefendCrystal } ?: return false
        val need = (damage * 10).roundToInt()
        val hasMana = hasManaToTransform(need)
        if (who !is LivingEntityTargetOption) {
            who?.damage(damage, world!!.damageSources.magic())
        }
        if (hasMana) {
            transformMana(defend, need)
            return true
        } else {
            breakFormation(damage, who)
            return false
        }
    }

    private var displayTime = 0
    private fun displayTransformManaParticle(
        request: FormationCrystal,
        responseCrystals: List<EnergyCrystalsBlockEntity>
    ) {
        displayTime = 1
        responseCrystals.forEach {
            val start = it.crystalPos
            val end = request.crystalPos

            val line = LightningParticleEmitters(start, world).apply {
                this.endPos = RelativeLocation.of(start.relativize(end))
                this.templateData.also { it ->
                    it.speed = 0.0
                    it.color = Math3DUtil.colorOf(230, 130, 255)
                    it.maxAge = 3
                }
                maxTick = 1
            }
            ParticleEmittersManager.spawnEmitters(line)
        }
    }

    override fun hasManaToTransform(count: Int): Boolean {
        world ?: return false
        val currentMana = activeCrystals.filter { it is EnergyCrystalsBlockEntity }.sumOf {
            it as EnergyCrystalsBlockEntity
            it.currentMana
        }
        return currentMana > count
    }

    private fun resetFormationHealth() {
        formationHealth = when (scale) {
            FormationScale.SMALL -> 100f
            FormationScale.MID -> 500f
            FormationScale.LARGE -> 100f
            FormationScale.NONE -> 0f
        }
    }

    /**
     * 检查完整性, 会有神人通过其他特殊方式将对应阵法的水晶替换
     */
    private fun checkIntact(): Boolean {
        world ?: return false
        val structure = scale.structure
        return structure.all {
            val entity = world!!.getBlockEntity(it.add(BlockPos.ofFloored(formationCore)))
            entity is FormationCrystal
        }
    }
}
