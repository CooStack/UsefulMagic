package cn.coostack.usefulmagic.particles.animation

interface ParticleAnimate {
    /**
     * 生命周期自减
     */
    fun decreaseDuration()

    /**
     * 判断生命周期是否为-1 或者 大于0
     */
    fun valid(): Boolean


    fun start()

    fun cancel()
}