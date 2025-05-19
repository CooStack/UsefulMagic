package cn.coostack.usefulmagic.blocks.entitiy

import cn.coostack.usefulmagic.UsefulMagic
import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
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