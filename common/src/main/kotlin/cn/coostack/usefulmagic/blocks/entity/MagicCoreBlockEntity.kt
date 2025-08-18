package cn.coostack.usefulmagic.blocks.entity

import cn.coostack.cooparticlesapi.CooParticlesAPI
import cn.coostack.cooparticlesapi.barrages.HitBox
import cn.coostack.cooparticlesapi.extend.relativize
import cn.coostack.cooparticlesapi.network.particle.emitters.ControlableParticleData
import cn.coostack.cooparticlesapi.network.particle.emitters.ParticleEmittersManager
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.PhysicsParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.impl.SimpleParticleEmitters
import cn.coostack.cooparticlesapi.network.particle.emitters.type.EmittersShootTypes
import cn.coostack.cooparticlesapi.network.particle.style.ParticleStyleManager
import cn.coostack.cooparticlesapi.network.particle.util.ServerParticleUtil
import cn.coostack.cooparticlesapi.particles.impl.ControlableCloudEffect
import cn.coostack.cooparticlesapi.particles.impl.ControlableEnchantmentEffect
import cn.coostack.cooparticlesapi.utils.Math3DUtil
import cn.coostack.cooparticlesapi.utils.builder.PointsBuilder
import cn.coostack.usefulmagic.entity.util.MagicBookSpawner
import cn.coostack.usefulmagic.particles.style.entitiy.CraftingLevel1Style
import cn.coostack.usefulmagic.particles.style.entitiy.CraftingLevel2Style
import cn.coostack.usefulmagic.particles.style.entitiy.CraftingLevel3Style
import cn.coostack.usefulmagic.recipe.AltarRecipeType
import cn.coostack.usefulmagic.recipe.AltarStackRecipeInput
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import java.util.Optional
import java.util.Random
import kotlin.math.min

class MagicCoreBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.MAGIC_CORE.get(), pos, state) {
    var currentMana = 0
    var maxMana = 0
    var currentReviveSpeed = 0
    var crafting = false
    var craftingTick = 0
    val magicBookSpawner = MagicBookSpawner(this)

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        tag.putInt("current_mana", currentMana)
        tag.putInt("crafting_tick", craftingTick)
        tag.putBoolean("crafting", crafting)
    }


    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {

        currentMana = tag.getInt("current_mana")
        crafting = tag.getBoolean("crafting")
        craftingTick = tag.getInt("crafting_tick")
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener?>? {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    private fun getCurrentRecipe(): Optional<RecipeHolder<AltarRecipeType>> {
        return level!!.recipeManager.getRecipeFor(
            AltarRecipeType.Type, getRecipeInput(), level
        )
    }

    private fun removeRecipeItems() {
        getAnotherAltarBlockEntities().forEach {
            it.setAltarStack(it.getAltarStack().item.craftingRemainingItem?.defaultInstance ?: ItemStack.EMPTY)
        }
    }

    private fun getCenterEntity(): AltarBlockCoreEntity? {
        val entity = level!!.getBlockEntity(worldPosition.below(3))
        return entity as? AltarBlockCoreEntity
    }

    private fun getRecipeInput(): AltarStackRecipeInput {
        val pos = worldPosition.below(3)
        val origin = level!!.getBlockEntity(pos)
        val east = level!!.getBlockEntity(pos.east(3))
        val west = level!!.getBlockEntity(pos.west(3))
        val south = level!!.getBlockEntity(pos.south(3))
        val north = level!!.getBlockEntity(pos.north(3))
        val se = level!!.getBlockEntity(pos.south(2).east(2))
        val sw = level!!.getBlockEntity(pos.south(2).west(2))
        val ne = level!!.getBlockEntity(pos.north(2).east(2))
        val nw = level!!.getBlockEntity(pos.north(2).west(2))
        val input = NonNullList.withSize(8, ItemStack.EMPTY)
        if (nw is AltarEntity) {
            input[0] = nw.getAltarStack()
        }
        if (north is AltarEntity) {
            input[1] = north.getAltarStack()
        }
        if (ne is AltarEntity) {
            input[2] = ne.getAltarStack()
        }
        if (east is AltarEntity) {
            input[3] = east.getAltarStack()
        }
        if (se is AltarEntity) {
            input[4] = se.getAltarStack()
        }
        if (south is AltarEntity) {
            input[5] = south.getAltarStack()
        }
        if (sw is AltarEntity) {
            input[6] = sw.getAltarStack()
        }
        if (west is AltarEntity) {
            input[7] = west.getAltarStack()
        }
        var center = ItemStack.EMPTY
        if (origin is AltarEntity) {
            center = origin.getAltarStack()
        }
        return AltarStackRecipeInput(
            input, center, currentMana
        )
    }

    private fun hasRecipe(): Boolean {
        return getCurrentRecipe().isPresent
    }

    fun getCenterAltarBlockEntity(): AltarBlockCoreEntity? {
        val pos = worldPosition.below(3)
        val entity = level!!.getBlockEntity(pos)
        if (entity !is AltarBlockCoreEntity) {
            return null
        }
        return entity
    }

    fun getAnotherAltarBlockEntities(): Set<AltarEntity> {
        level ?: return HashSet()
        val res = HashSet<AltarEntity>()
        val pos = worldPosition.below(3)
        val origin = level!!.getBlockEntity(pos)
        val east = level!!.getBlockEntity(pos.east(3))
        val west = level!!.getBlockEntity(pos.west(3))
        val south = level!!.getBlockEntity(pos.south(3))
        val north = level!!.getBlockEntity(pos.north(3))
        val se = level!!.getBlockEntity(pos.south(2).east(2))
        val sw = level!!.getBlockEntity(pos.south(2).west(2))
        val ne = level!!.getBlockEntity(pos.north(2).east(2))
        val nw = level!!.getBlockEntity(pos.north(2).west(2))
        if (origin != null && origin is AltarBlockCoreEntity) res.add(origin)
        if (east != null && east is AltarBlockEntity) res.add(east)
        if (north != null && north is AltarBlockEntity) res.add(north)
        if (west != null && west is AltarBlockEntity) res.add(west)
        if (sw != null && sw is AltarBlockEntity) res.add(sw)
        if (south != null && south is AltarBlockEntity) res.add(south)
        if (nw != null && nw is AltarBlockEntity) res.add(nw)
        if (se != null && se is AltarBlockEntity) res.add(se)
        if (nw != null && nw is AltarBlockEntity) res.add(nw)
        if (ne != null && ne is AltarBlockEntity) res.add(ne)
        return res
    }

    var currentRecipe: AltarRecipeType? = null
    var tick = 0
    private var status = false
    private fun handleRecipe() {
        if (level!!.isClientSide) return
        if (hasRecipe()) {
            val current = getCurrentRecipe()
            val value = current.get().value
            // 重新进入时 currentRecipe是null
            if (currentRecipe == null) {
                this.currentRecipe = value
            }

            if (currentRecipe != null && currentRecipe != value) {
                this.currentRecipe = value
                craftingTick = 0
            }
            if (!crafting) {
                crafting = true
            }
            if (!status) {
                startCrafting()
                status = true
            }
            onCrafting()
            if (craftingTick++ >= currentRecipe!!.tick) {
                val value = currentRecipe!!.assemble(getRecipeInput(), null)
                removeRecipeItems()
                getCenterEntity()?.setAltarStack(value)
                // 完成
                finishCrafting()
                crafting = false
                craftingTick = 0
                currentMana -= currentRecipe!!.manaNeed
                level!!.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 3F, 1F)
            }
        } else {
            if (crafting) {
                cancelCrafting()
            }
            status = false
            crafting = false
            craftingTick = 0
        }
        setChanged()
        level!!.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_ALL)

    }

    fun checkCompleteness(): Boolean {
        return maxMana > 0
    }

    // tickStyle
    // craftingStyle
    // finishCraftingStyle
    // cancelCraftingStyle

    fun tick(
        world: Level,
        pos: BlockPos,
        state: BlockState,
    ) {
        handleRecipe()
        // 更新方块
        // 查找周围的8个
        var anotherRevive = 0
        var anotherMaxMana = 0
        val entities = getAnotherAltarBlockEntities().map {
            val max = it.getDownActiveBlocksMaxMana()
            it to max
            anotherMaxMana += max
            anotherRevive += it.getDownActiveBlocksManaReviveSpeed()
            it to max
        }

        maxMana = anotherMaxMana
        currentReviveSpeed = anotherRevive

        // 尝试生成
        // 只在服务器调用
        if (!world.isClientSide) {
            magicBookSpawner.tick()
            if (magicBookSpawner.start) {
                tick++
                return
            }
        }

        if (crafting) {
            // 合成中不能聚集魔力
            tick++
            return
        }

        if (tick % 20 == 0) {
            currentMana += currentReviveSpeed
            currentMana = min(maxMana, currentMana)
            setChanged()
            if (!world.isClientSide) {
                // 生成粒子
                for (pair in entities) {
                    val entity = pair.first as BlockEntity
                    if (pair.second == 0 || currentMana == maxMana) {
                        continue
                    }
                    val loc = entity.blockPos.center.add(0.0, 0.5, 0.0)
                    val dir = loc.relativize(pos.center)
                    // 生成粒子
                    val emitters = SimpleParticleEmitters(
                        loc, world, ControlableParticleData()
                            .apply {
                                color = Math3DUtil.colorOf(240, 100, 255)
                                maxAge = 35
                                effect = ControlableCloudEffect(uuid)
                                velocity = dir
                                speed = 0.1
                            }
                    )
                    emitters.maxTick = 1
                    ParticleEmittersManager.spawnEmitters(emitters)
                }
            }
        }
        tick++
    }


    val options: Int
        get() = ParticleOption.getParticleCounts()
    val random = Random(System.currentTimeMillis())
    val rangeBall = PointsBuilder().addBall(64.0, 8 * options).create()
    private fun onCrafting() {
        val max = currentRecipe!!.tick
        level ?: return
        if (level!!.isClientSide) return
        val world = level as ServerLevel
        if (craftingTick % 3 == 0) {
            repeat(if (tick < max / 2) 5 else 10) {
                val it = rangeBall.random()
                val pos = worldPosition.center.add(it.toVector())
                val dir = it.normalize().multiply(-5.75)
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.END_ROD, world, pos, dir.toVector()
                )
            }
        }
    }

    private fun startCrafting() {
        if (level!!.isClientSide) {
            return
        }
        val tick = currentRecipe!!.tick

        // 计算tick所属档位
        val over = if (tick <= 500) {
            spawnStyleLevel1()
        } else if (tick <= 1500) {
            spawnStyleLevel2()
        } else {
            spawnStyleLevel3()
        }

        val emitters = getAnotherAltarBlockEntities()
            .map {
                it as BlockEntity
                SimpleParticleEmitters(
                    it.blockPos.center.add(0.0, 0.5, 0.0), it.level,
                    ControlableParticleData()
                        .apply {
                            val center = it.blockPos.center.add(0.0, 0.5, 0.0)
                            val corePos = this@MagicCoreBlockEntity.blockPos.center
                            velocity = center.relativize(corePos)
                            effect = ControlableEnchantmentEffect(uuid)
                            size = 0.2f
                            maxAge = 35
                            age = 0
                            color = Math3DUtil.colorOf(240, 120, 200)
                            speed = 0.1
                        }
                ).also {
                    it.delay = 20
                }
            }.toList()
        val preTickAdded = (if (tick >= 500) (tick - 200) else tick) / 9
        var index = 0
        var cancel = false
        var loop = craftingTick
        CooParticlesAPI.scheduler.runTaskTimerMaxTick(tick - loop + 1) {
            loop++
            if (!crafting && !cancel) {
                cancel = true
                over(loop >= tick)
                emitters.forEach {
                    it.stop()
                }
            }
            if (cancel) return@runTaskTimerMaxTick
            if (craftingTick > preTickAdded * index && index < emitters.size) {
                ParticleEmittersManager.spawnEmitters(
                    emitters[index++].apply {
                        maxTick = tick - craftingTick
                    })
            }
        }
    }

    private fun finishCrafting() {
        if (level!!.isClientSide) return
        ServerParticleUtil.spawnSingle(
            ParticleTypes.FIREWORK, level as ServerLevel, worldPosition.center, Vec3.ZERO, true, 0.2, 300
        )
    }

    private fun cancelCrafting() {
        if (level!!.isClientSide) return
        ServerParticleUtil.spawnSingle(
            ParticleTypes.FLAME, level as ServerLevel, worldPosition.center, Vec3.ZERO, true, 0.1, 100
        )
        level!!.playSound(null, worldPosition, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 3F, 1F)
    }


    /**
     * 圆聚集 较快
     * 圆环旋转
     * 0-500
     */
    private fun spawnStyleLevel1(): (Boolean) -> Unit {
        if (level!!.isClientSide) return {}
        val style = CraftingLevel1Style(4.0, 120 * options)
        val toCenterPos = worldPosition.below(3).center
        val emitters = SimpleParticleEmitters(
            toCenterPos,
            level,
            ControlableParticleData()
                .apply {
                    color = Math3DUtil.colorOf(240, 200, 140)
                    effect = ControlableCloudEffect(uuid)
                    this.maxAge = 30
                    speed = 0.2
                }
        )
        emitters.apply {
            shootType = EmittersShootTypes.math(
                "2 * COS(RAD(i * c * 10))",
                "0",
                "2 * SIN(RAD(i * c * 10))",
                "ox-x",
                "oy-y + 3",
                "oz-z",
            )
            count = 40 * options
            maxTick = currentRecipe!!.tick - craftingTick
            delay = 10
        }
        ParticleEmittersManager.spawnEmitters(emitters)
        ParticleStyleManager.spawnStyle(level as ServerLevel, toCenterPos, style)
        return {
            emitters.stop()
            style.status.setStatus(2)
        }
    }

    /**
     * 小魔法阵
     * 圆聚集 较快
     * 500-2000
     */
    private fun spawnStyleLevel2(): (Boolean) -> Unit {
        if (level!!.isClientSide) return {}
        val style = CraftingLevel2Style()
        val toCenterPos = worldPosition.below(3).center
        val emitters = SimpleParticleEmitters(
            toCenterPos,
            level,
            ControlableParticleData()
                .apply {
                    color = Math3DUtil.colorOf(240, 200, 140)
                    effect = ControlableCloudEffect(uuid)
                    this.maxAge = 30
                    speed = 0.2
                }
        )
        emitters.apply {
            shootType = EmittersShootTypes.math(
                "4 * COS(RAD(i * c * 10))",
                "0",
                "4 * SIN(RAD(i * c * 10))",
                "ox-x",
                "oy-y + 3",
                "oz-z",
            )
            count = 60 * options
            maxTick = currentRecipe!!.tick - craftingTick
            delay = 10
        }
        ParticleStyleManager.spawnStyle(level as ServerLevel, toCenterPos, style)
        ParticleEmittersManager.spawnEmitters(emitters)
        return {
            emitters.stop()
            style.status.setStatus(2)
        }
    }

    /**
     * 大魔法阵
     * 完成时生成粒子柱
     * >2000
     * 返回事件完成的callback
     */
    private fun spawnStyleLevel3(): (Boolean) -> Unit {
        if (level!!.isClientSide) return {}
        val style = CraftingLevel3Style()
        style.age = craftingTick
        val toCenterPos = worldPosition.below(3).center
        val emitters = SimpleParticleEmitters(
            toCenterPos,
            level,
            ControlableParticleData()
                .apply {
                    color = Math3DUtil.colorOf(240, 200, 140)
                    effect = ControlableCloudEffect(uuid)
                    this.maxAge = 30
                    speed = 0.2
                    velocity = Vec3.ZERO
                }
        )
        emitters.apply {
            shootType = EmittersShootTypes.math(
                "5 * COS(RAD(i * c * 10))",
                "0",
                "5 * SIN(RAD(i * c * 10))",
                "ox-x",
                "oy-y + 3",
                "oz-z",
            )
            count = 90 * options
            maxTick = currentRecipe!!.tick - craftingTick
            delay = 10
        }
        ParticleEmittersManager.spawnEmitters(emitters)
        ParticleStyleManager.spawnStyle(level as ServerLevel, toCenterPos, style)
        return {
            val up = PhysicsParticleEmitters(
                toCenterPos, level, ControlableParticleData()
                    .apply {
                        color = Math3DUtil.colorOf(240, 200, 140)
                        effect = ControlableCloudEffect(uuid)
                        speed = 0.5
                        this.maxAge = 40
                    }
            )
            up.apply {
                wind.direction = Vec3(0.0, 10.0, 0.0)
                count = 30
                shootType = EmittersShootTypes.box(
                    HitBox.of(1.0, 1.0, 1.0)
                )
                gravity = PhysicsParticleEmitters.EARTH_GRAVITY
                airDensity = PhysicsParticleEmitters.SEA_AIR_DENSITY
                maxTick = 60
            }
            val centerUP = PhysicsParticleEmitters(
                toCenterPos, level, ControlableParticleData()
                    .apply {
                        color = Math3DUtil.colorOf(240, 140, 140)
                        effect = ControlableCloudEffect(uuid)
                        speed = 0.02
                        this.maxAge = 40
                    }
            )
            centerUP.apply {
                wind.direction = Vec3(0.0, 10.0, 0.0)
                count = 30
                shootType = EmittersShootTypes.box(
                    HitBox.of(1.0, 1.0, 1.0)
                )
                gravity = PhysicsParticleEmitters.EARTH_GRAVITY
                airDensity = PhysicsParticleEmitters.SEA_AIR_DENSITY
                maxTick = 60
            }
            style.status.setStatus(2)
            emitters.stop()
            if (it) {
                ParticleEmittersManager.spawnEmitters(up)
                ParticleEmittersManager.spawnEmitters(centerUP)
            }
        }
    }

}