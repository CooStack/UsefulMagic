package cn.coostack.usefulmagic

import cn.coostack.usefulmagic.blocks.UsefulMagicBlocks
import cn.coostack.usefulmagic.blocks.entity.AltarBlockCoreEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.AltarBlockEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.MagicCoreBlockEntityRenderer
import cn.coostack.usefulmagic.blocks.entity.UsefulMagicBlockEntities
import cn.coostack.usefulmagic.blocks.entity.formation.renderer.CrystalEntityRenderer
import cn.coostack.usefulmagic.gui.friend.FriendManagerScreen
import cn.coostack.usefulmagic.items.UsefulMagicDataComponentTypes.LARGE_REVIVE_USE_COUNT
import cn.coostack.usefulmagic.items.UsefulMagicItems
import cn.coostack.usefulmagic.items.consumer.LargeManaRevive
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import org.lwjgl.glfw.GLFW

object UsefulMagicNeoClient {

    fun onClientSetup(event: FMLClientSetupEvent) {
        UsefulMagicClient.init()
        loadClientRegistry()
        loadModelPredicate()
    }

    /**
     *
     */
    fun loadClientRegistry() {
        handleBlockLayer()
    }

    private fun loadModelPredicate() {
        ItemProperties.register(
            UsefulMagicItems.LARGE_MANA_REVIVE.getItem(),
            ResourceLocation.withDefaultNamespace("use_count")
        ) { stack: ItemStack, world: Level?, entity: LivingEntity?, seed: Int ->
            val count = stack.get(LARGE_REVIVE_USE_COUNT.get()) ?: LargeManaRevive.MAX_USAGE
            1f - (count.toFloat() / LargeManaRevive.MAX_USAGE)
        }
    }

    private fun handleBlockLayer() {
        val cutoutBlocks = listOf(
            UsefulMagicBlocks.DEFEND_CRYSTAL_BLOCK.get(),
            UsefulMagicBlocks.SWORD_ATTACK_CRYSTAL_BLOCK.get(),
            UsefulMagicBlocks.RECOVER_CRYSTAL_BLOCK.get(),
            UsefulMagicBlocks.ENERGY_CRYSTAL_BLOCK.get(),
            UsefulMagicBlocks.FORMATION_CORE_BLOCK.get(),
            UsefulMagicBlocks.ALTAR_BLOCK.get() // Fabric 里也设置了这个
        )

        cutoutBlocks.forEach { block ->
            ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout())
        }
    }
}