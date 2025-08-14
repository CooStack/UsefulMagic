package com.example.examplemod

import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

/**
 * 没有找到能够直接注入 object作为 event的方法
 * 如果注解了 EventBusSubscriber 则一定要是class 而不是 object
 * 要不然就会报大错 :)
 */
@Mod(Constants.MOD_ID)
object ExampleModNeo {
    init {
        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.
        MOD_BUS.addListener(::onSetup)
        MOD_BUS.addListener(::onServerSetup)
        MOD_BUS.addListener(::onCommonSetup)
        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!")
        CommonClass.init()
    }

    private fun onSetup(event: FMLClientSetupEvent) {
        Constants.LOG.info("Initializing client...")
    }

    private fun onServerSetup(event: FMLDedicatedServerSetupEvent) {
        Constants.LOG.info("Initializing server...")

    }

    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        Constants.LOG.info("Hello This is working...")
    }
}