package cn.coostack.usefulmagic.meteorite.impl

import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.meteorite.Meteorite
import cn.coostack.usefulmagic.utils.MathUtil
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

class OptionMeteorite(val material: Block, val r: Int, val hitMethod: (Vec3) -> Unit) : Meteorite() {
    private var tickMethod: OptionMeteorite.() -> Unit = {}
    var time = 0
    fun withTick(tickMethod: OptionMeteorite.() -> Unit): OptionMeteorite {
        this.tickMethod = tickMethod
        return this
    }

    override fun tick() {
        super.tick()
        time++
        tickMethod(this)
    }

    override fun getBlocks(): Map<RelativeLocation, BlockState> {
        return MathUtil.getSolidBall(r).associateWith {
            material.defaultBlockState()
        }
    }

    override fun onHit(pos: Vec3) {
        hitMethod(pos)
    }
}