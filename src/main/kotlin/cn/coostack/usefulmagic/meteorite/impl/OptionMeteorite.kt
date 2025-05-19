package cn.coostack.usefulmagic.meteorite.impl

import cn.coostack.cooparticlesapi.utils.RelativeLocation
import cn.coostack.usefulmagic.meteorite.Meteorite
import cn.coostack.usefulmagic.utils.MathUtil
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.Vec3d

class OptionMeteorite(val material: Block, val r: Int, val hitMethod: (Vec3d) -> Unit) : Meteorite() {
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
            material.defaultState
        }
    }

    override fun onHit(pos: Vec3d) {
        hitMethod(pos)
    }
}