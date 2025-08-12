package com.example.examplemod

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer

object ExampleModClient : ClientModInitializer {
    override fun onInitializeClient() {
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world! Client")
        CommonClass.init()
    }
}