package cn.coostack.usefulmagic.extend

/**
 * 默认当成double处理
 *
 * @param other
 * @return
 */
operator fun Number.plus(other: Number): Number {
    return this.toDouble() + other.toDouble()
}

operator fun Number.minus(other: Number): Number {
    return this.toDouble() - other.toDouble()
}

operator fun Number.times(other: Number): Number {
    return this.toDouble() * other.toDouble()
}

fun Number.lerpAsProgress(start: Number, end: Number): Double {
    val step = end - start
    return (start + this * step).toDouble()
}