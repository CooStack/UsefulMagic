package cn.coostack.usefulmagic.blocks.entitiy

import cn.coostack.cooparticlesapi.CooParticleAPI
import cn.coostack.cooparticlesapi.barrages.HitBox
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
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.recipe.RecipeEntry
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.Optional
import java.util.Random
import kotlin.math.min

class MagicCoreBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(UsefulMagicBlockEntities.MAGIC_CORE, pos, state) {
    var currentMana = 0
    var maxMana = 0
    var currentReviveSpeed = 0
    var crafting = false
    var craftingTick = 0
    val magicBookSpawner = MagicBookSpawner(this)
    override fun writeNbt(nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.writeNbt(nbt, registryLookup)
        nbt?.putInt("current_mana", currentMana)
        nbt?.putInt("crafting_tick", craftingTick)
        nbt?.putBoolean("crafting", crafting)
    }

    override fun readNbt(nbt: NbtCompound?, registryLookup: RegistryWrapper.WrapperLookup?) {
        super.readNbt(nbt, registryLookup)
        currentMana = nbt?.getInt("current_mana") ?: 0
        crafting = nbt?.getBoolean("crafting") ?: false
        craftingTick = nbt?.getInt("crafting_tick") ?: 0
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener?>? {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup?): NbtCompound? {
        return createNbt(registryLookup)
    }

    private fun getCurrentRecipe(): Optional<RecipeEntry<AltarRecipeType>> {
        return world!!.recipeManager.getFirstMatch(
            AltarRecipeType.Type, getRecipeInput(), world
        )
    }

    private fun removeRecipeItems() {
        getAnotherAltarBlockEntities().forEach {
            it.setAltarStack(it.getAltarStack().item.recipeRemainder?.defaultStack ?: ItemStack.EMPTY)
        }
    }

    private fun getCenterEntity(): AltarBlockCoreEntity? {
        val pos = pos.down(3)
        val entity = world!!.getBlockEntity(pos)
        if (entity !is AltarBlockCoreEntity) {
            return null
        }
        return entity
    }

    private fun getRecipeInput(): AltarStackRecipeInput {
        val pos = pos.down(3)
        val origin = world!!.getBlockEntity(pos)
        val east = world!!.getBlockEntity(pos.east(3))
        val west = world!!.getBlockEntity(pos.west(3))
        val south = world!!.getBlockEntity(pos.south(3))
        val north = world!!.getBlockEntity(pos.north(3))
        val se = world!!.getBlockEntity(pos.south(2).east(2))
        val sw = world!!.getBlockEntity(pos.south(2).west(2))
        val ne = world!!.getBlockEntity(pos.north(2).east(2))
        val nw = world!!.getBlockEntity(pos.north(2).west(2))
        val input = DefaultedList.ofSize(8, ItemStack.EMPTY)
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
        val pos = pos.down(3)
        val entity = world!!.getBlockEntity(pos)
        if (entity !is AltarBlockCoreEntity) {
            return null
        }
        return entity
    }

    fun getAnotherAltarBlockEntities(): Set<AltarEntity> {
        world ?: return HashSet()
        val res = HashSet<AltarEntity>()
        val pos = pos.down(3)
        val origin = world!!.getBlockEntity(pos)
        val east = world!!.getBlockEntity(pos.east(3))
        val west = world!!.getBlockEntity(pos.west(3))
        val south = world!!.getBlockEntity(pos.south(3))
        val north = world!!.getBlockEntity(pos.north(3))
        val se = world!!.getBlockEntity(pos.south(2).east(2))
        val sw = world!!.getBlockEntity(pos.south(2).west(2))
        val ne = world!!.getBlockEntity(pos.north(2).east(2))
        val nw = world!!.getBlockEntity(pos.north(2).west(2))
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
        if (world!!.isClient) return
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
                val value = currentRecipe!!.craft(getRecipeInput(), null)
                removeRecipeItems()
                getCenterEntity()?.setAltarStack(value ?: ItemStack.EMPTY)
                // 完成
                finishCrafting()
                crafting = false
                craftingTick = 0
                currentMana -= currentRecipe!!.manaNeed
                world!!.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 3F, 1F)
            }
        } else {
            if (crafting) {
                cancelCrafting()
            }
            status = false
            crafting = false
            craftingTick = 0
        }
        markDirty()
        world!!.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)

    }

    // tickStyle
    // craftingStyle
    // finishCraftingStyle
    // cancelCraftingStyle

    fun tick(
        world: World,
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
        if (!world.isClient) {
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
            markDirty()
            if (!world.isClient) {
                // 生成粒子
                for (pair in entities) {
                    val entity = pair.first as BlockEntity
                    if (pair.second == 0 || currentMana == maxMana) {
                        continue
                    }
                    val loc = entity.pos.toCenterPos().add(0.0, 0.5, 0.0)
                    val dir = loc.relativize(pos.toCenterPos())
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
        world ?: return
        if (world!!.isClient) return
        val world = world as ServerWorld
        if (craftingTick % 3 == 0) {
            repeat(if (tick < max / 2) 5 else 10) {
                val it = rangeBall.random()
                val pos = pos.toCenterPos().add(it.toVector())
                val dir = it.normalize().multiply(-5.75)
                ServerParticleUtil.spawnSingle(
                    ParticleTypes.END_ROD, world, pos, dir.toVector()
                )
            }
        }
    }

    private fun startCrafting() {
        if (world!!.isClient) {
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
                    it.pos.toCenterPos().add(0.0, 0.5, 0.0), it.world,
                    ControlableParticleData()
                        .apply {
                            val center = it.pos.toCenterPos().add(0.0, 0.5, 0.0)
                            val corePos = this@MagicCoreBlockEntity.pos.toCenterPos()
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
        CooParticleAPI.scheduler.runTaskTimerMaxTick(tick - loop + 1) {
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
        if (world!!.isClient) return
        ServerParticleUtil.spawnSingle(
            ParticleTypes.FIREWORK, world as ServerWorld, pos.toCenterPos(), Vec3d.ZERO, true, 0.2, 300
        )
    }

    private fun cancelCrafting() {
        if (world!!.isClient) return
        ServerParticleUtil.spawnSingle(
            ParticleTypes.FLAME, world as ServerWorld, pos.toCenterPos(), Vec3d.ZERO, true, 0.1, 100
        )
        world!!.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 3F, 1F)
    }


    /**
     * 圆聚集 较快
     * 圆环旋转
     * 0-500
     */
    private fun spawnStyleLevel1(): (Boolean) -> Unit {
        if (world!!.isClient) return {}
        val style = CraftingLevel1Style(4.0, 120 * options)
        val toCenterPos = pos.down(3).toCenterPos()
        val emitters = SimpleParticleEmitters(
            toCenterPos,
            world,
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
        ParticleStyleManager.spawnStyle(world as ServerWorld, toCenterPos, style)
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
        if (world!!.isClient) return {}
        val style = CraftingLevel2Style()
        val toCenterPos = pos.down(3).toCenterPos()
        val emitters = SimpleParticleEmitters(
            toCenterPos,
            world,
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
        ParticleStyleManager.spawnStyle(world as ServerWorld, toCenterPos, style)
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
        if (world!!.isClient) return {}
        val style = CraftingLevel3Style()
        style.age = craftingTick
        val toCenterPos = pos.down(3).toCenterPos()
        val emitters = SimpleParticleEmitters(
            toCenterPos,
            world,
            ControlableParticleData()
                .apply {
                    color = Math3DUtil.colorOf(240, 200, 140)
                    effect = ControlableCloudEffect(uuid)
                    this.maxAge = 30
                    speed = 0.2
                    velocity = Vec3d.ZERO
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
        ParticleStyleManager.spawnStyle(world as ServerWorld, toCenterPos, style)
        return {
            val up = PhysicsParticleEmitters(
                toCenterPos, world, ControlableParticleData()
                    .apply {
                        color = Math3DUtil.colorOf(240, 200, 140)
                        effect = ControlableCloudEffect(uuid)
                        speed = 0.5
                        this.maxAge = 40
                    }
            )
            up.apply {
                wind.direction = Vec3d(0.0, 10.0, 0.0)
                count = 30
                shootType = EmittersShootTypes.box(
                    HitBox.of(1.0, 1.0, 1.0)
                )
                gravity = PhysicsParticleEmitters.EARTH_GRAVITY
                airDensity = PhysicsParticleEmitters.SEA_AIR_DENSITY
                maxTick = 60
            }
            val centerUP = PhysicsParticleEmitters(
                toCenterPos, world, ControlableParticleData()
                    .apply {
                        color = Math3DUtil.colorOf(240, 140, 140)
                        effect = ControlableCloudEffect(uuid)
                        speed = 0.02
                        this.maxAge = 40
                    }
            )
            centerUP.apply {
                wind.direction = Vec3d(0.0, 10.0, 0.0)
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