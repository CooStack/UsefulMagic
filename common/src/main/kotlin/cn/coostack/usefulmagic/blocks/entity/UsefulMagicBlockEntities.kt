package cn.coostack.usefulmagic.blocks.entity

import cn.coostack.cooparticlesapi.platform.registry.CommonDeferredBlockEntityType
import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.blocks.entity.formation.DefendCrystalBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.EnergyCrystalsBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.RecoverCrystalsBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.SwordAttackCrystalsBlockEntity
import com.mojang.datafixers.DSL
import net.minecraft.Util
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import java.util.function.Supplier

/**
 * 首次初始化需要在block注册之后
 * 最好在block entity注册事件里执行实例化
 */
object UsefulMagicBlockEntities {
    val blockEntityTypes = mutableListOf<CommonDeferredBlockEntityType<*>>()

    @JvmStatic
    val ALTAR_BLOCK: CommonDeferredBlockEntityType<AltarBlockEntity> = create(
        "altar_block", BlockEntityType.Builder.of(
            ::AltarBlockEntity, UsefulMagicBlocks.ALTAR_BLOCK.get()
        )
    )

    @JvmStatic
    val ALTAR_BLOCK_CORE: CommonDeferredBlockEntityType<AltarBlockCoreEntity> = create(
        "altar_block_core", BlockEntityType.Builder.of(
            ::AltarBlockCoreEntity, UsefulMagicBlocks.ALTAR_BLOCK_CORE.get()
        )
    )

    @JvmStatic
    val MAGIC_CORE: CommonDeferredBlockEntityType<MagicCoreBlockEntity> = create(
        "magic_core", BlockEntityType.Builder.of(
            ::MagicCoreBlockEntity, UsefulMagicBlocks.MAGIC_CORE.get()
        )
    )

    @JvmStatic
    val ENERGY_CRYSTAL: CommonDeferredBlockEntityType<EnergyCrystalsBlockEntity> = create(
        "energy_crystal", BlockEntityType.Builder.of(
            ::EnergyCrystalsBlockEntity, UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK.get()
        )
    )

    @JvmStatic
    val SWORD_ATTACK_CRYSTAL: CommonDeferredBlockEntityType<SwordAttackCrystalsBlockEntity> = create(
        "sword_attack_crystal", BlockEntityType.Builder.of(
            ::SwordAttackCrystalsBlockEntity, UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK.get()
        )
    )

    @JvmStatic
    val RECOVER_CRYSTAL = create(
        "recover_crystal", BlockEntityType.Builder.of(
            ::RecoverCrystalsBlockEntity, UsefulMagicBlocks.RECOVER_CRYSTAL_BLOCK.get()
        )
    )

    @JvmStatic
    val FORMATION_CORE: CommonDeferredBlockEntityType<FormationCoreBlockEntity> = create(
        "formation_core", BlockEntityType.Builder.of(
            ::FormationCoreBlockEntity, UsefulMagicBlocks.FORMATION_CORE_BLOCK.get()
        )
    )

    @JvmStatic
    val DEFEND_CRYSTAL: CommonDeferredBlockEntityType<DefendCrystalBlockEntity> = create(
        "defend_crystal", BlockEntityType.Builder.of(
            ::DefendCrystalBlockEntity, UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK.get()
        )
    )

    @JvmStatic
    private fun <T : BlockEntity> create(
        id: String, builder: BlockEntityType.Builder<T>
    ): CommonDeferredBlockEntityType<T> {
        val create = CommonDeferredBlockEntityType(
            ResourceLocation.fromNamespaceAndPath(UsefulMagic.MOD_ID, id)
        ) {
            builder.build(null)
        }
        blockEntityTypes.add(create)
        return create
    }

    @JvmStatic
    fun reg() {
    }

}