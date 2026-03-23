package cn.coostack.usefulmagic

import cn.coostack.cooparticlesapi.CooParticlesAPIClient
import cn.coostack.cooparticlesapi.renderer.client.ClientRenderPipelineManager
import cn.coostack.usefulmagic.renderer.UsefulMagicShaderPipelines
import cn.coostack.usefulmagic.gui.friend.FriendManagerScreen
import cn.coostack.usefulmagic.utils.ParticleOption
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft

object UsefulMagicClient {
    lateinit var friendUIBinding: KeyMapping
    private var renderEntitiesInitialized = false
    private var renderEntitiesPendingInit = false

    @JvmStatic
    val option: Int
        get() = ParticleOption.getParticleCounts()

    fun init() {
        renderEntitiesPendingInit = true
    }

    fun loadKeyBindings(key: KeyMapping) {
        friendUIBinding = key
    }

    fun tickClient() {
        ensureRenderEntitiesInitialized()
        val minecraft = Minecraft.getInstance()
        if (friendUIBinding.isDown) {
            minecraft.setScreen(FriendManagerScreen())
        }
    }

    private fun ensureRenderEntitiesInitialized() {
        if (!renderEntitiesPendingInit || renderEntitiesInitialized) {
            return
        }
        val minecraft = Minecraft.getInstance()
        val renderTarget = minecraft.mainRenderTarget
        if (minecraft.window == null || renderTarget == null) {
            return
        }
        if (
            renderTarget.width <= 0 ||
            renderTarget.height <= 0 ||
            renderTarget.colorTextureId <= 0 ||
            renderTarget.depthTextureId <= 0
        ) {
            return
        }
        initRenderEntities(renderTarget.width, renderTarget.height)
    }

    private fun initRenderEntities(width: Int, height: Int) {
        if (renderEntitiesInitialized) {
            return
        }
        CooParticlesAPIClient.syncRenderBackend()
        ClientRenderPipelineManager.resizeTo(width, height)
        CooParticlesAPIClient.initShaderPrograms()
        UsefulMagicShaderPipelines.init()
        renderEntitiesInitialized = true
        renderEntitiesPendingInit = false
    }
}
