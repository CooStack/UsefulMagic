package cn.coostack.usefulmagic.platform

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader

class FabricEnvHelper : EnvHelper {
    override fun isServerSide(): Boolean {
        return FabricLoader.getInstance().environmentType == EnvType.SERVER
    }
}