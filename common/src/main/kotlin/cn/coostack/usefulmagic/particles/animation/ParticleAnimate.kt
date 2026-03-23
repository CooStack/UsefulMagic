package cn.coostack.usefulmagic.particles.animation

@Deprecated("用CooParticlesAPI提供的Animate")
/**
 * 留着当纯因为某些选项懒得改了qaq
 *
 * @constructor Create empty Particle animation
 */
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