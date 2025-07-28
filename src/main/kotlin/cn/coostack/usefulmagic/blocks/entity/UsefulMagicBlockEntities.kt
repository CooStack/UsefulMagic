package cn.coostack.usefulmagic.blocks.entity

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.blocks.entity.formation.DefendCrystalBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.EnergyCrystalsBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.FormationCoreBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.RecoverCrystalsBlockEntity
import cn.coostack.usefulmagic.blocks.entity.formation.SwordAttackCrystalsBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.datafixer.TypeReferences
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.util.Util

object UsefulMagicBlockEntities {

    @JvmStatic
    val ALTAR_BLOCK: BlockEntityType<AltarBlockEntity> = create(
        "altar_block", BlockEntityType.Builder.create(
            ::AltarBlockEntity, UsefulMagicBlocks.ALTAR_BLOCK
        )
    )

    @JvmStatic
    val ALTAR_BLOCK_CORE: BlockEntityType<AltarBlockCoreEntity> = create(
        "altar_block_core", BlockEntityType.Builder.create(
            ::AltarBlockCoreEntity, UsefulMagicBlocks.ALTAR_BLOCK_CORE
        )
    )

    @JvmStatic
    val MAGIC_CORE: BlockEntityType<MagicCoreBlockEntity> = create(
        "magic_core", BlockEntityType.Builder.create(
            ::MagicCoreBlockEntity, UsefulMagicBlocks.MAGIC_CORE
        )
    )

    @JvmStatic
    val ENERGY_CRYSTAL: BlockEntityType<EnergyCrystalsBlockEntity> = create(
        "energy_crystal", BlockEntityType.Builder.create(
            ::EnergyCrystalsBlockEntity, UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK
        )
    )

    @JvmStatic
    val SWORD_ATTACK_CRYSTAL: BlockEntityType<SwordAttackCrystalsBlockEntity> = create(
        "sword_attack_crystal", BlockEntityType.Builder.create(
            ::SwordAttackCrystalsBlockEntity, UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK
        )
    )

    @JvmStatic
    val RECOVER_CRYSTAL = create(
        "recover_crystal", BlockEntityType.Builder.create(
            ::RecoverCrystalsBlockEntity, UsefulMagicBlocks.RECOVER_CRYSTAL_BLOCK
        )
    )

    @JvmStatic
    val FORMATION_CORE: BlockEntityType<FormationCoreBlockEntity> = create(
        "formation_core", BlockEntityType.Builder.create(
            ::FormationCoreBlockEntity, UsefulMagicBlocks.FORMATION_CORE_BLOCK
        )
    )

    @JvmStatic
    val DEFEND_CRYSTAL: BlockEntityType<DefendCrystalBlockEntity> = create(
        "defend_crystal", BlockEntityType.Builder.create(
            ::DefendCrystalBlockEntity, UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK
        )
    )

    @JvmStatic
    private fun <T : BlockEntity> create(
        id: String, builder: BlockEntityType.Builder<T>
    ): BlockEntityType<T> {
        val type = Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id)
        return Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Identifier.of(UsefulMagic.MOD_ID, id), builder.build(type)
        )
    }

    @JvmStatic
    fun reg() {
    }

}