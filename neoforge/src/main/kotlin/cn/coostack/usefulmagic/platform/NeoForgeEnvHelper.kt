package cn.coostack.usefulmagic.platform

import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.loading.FMLLoader

class NeoForgeEnvHelper : EnvHelper {
    override fun isServerSide(): Boolean {
        return FMLLoader.getDist() == Dist.DEDICATED_SERVER
    }
}