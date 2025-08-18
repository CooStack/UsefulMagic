package cn.coostack.usefulmagic.extend

import net.minecraft.world.phys.Vec3

fun Vec3.multiply(scaled: Number): Vec3 {
    return this.scale(scaled.toDouble())
}