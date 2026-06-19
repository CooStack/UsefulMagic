package cn.coostack.usefulmagic.entity.util

import net.minecraft.world.phys.Vec3
import kotlin.test.Test
import kotlin.test.assertTrue

class FlightMovementUtilTest {
    @Test
    fun `orbit approach steers inward when outside target radius`() {
        val center = Vec3.ZERO
        val currentPosition = Vec3(10.0, 0.0, 0.0)

        val velocity = FlightMovementUtil.orbitApproach(
            currentPosition = currentPosition,
            currentVelocity = Vec3.ZERO,
            center = center,
            targetRadius = 0.5,
            preferredY = 0.0,
            acceleration = 0.8,
            damping = 0.95,
            shrinkRate = 1.0
        )

        val radialDirection = currentPosition.subtract(center).normalize()
        assertTrue(
            velocity.dot(radialDirection) < 0.0,
            "orbitApproach should add inward radial velocity while approaching the target"
        )
    }

    @Test
    fun `orbit around steers outward when inside target radius`() {
        val center = Vec3.ZERO
        val currentPosition = Vec3(2.0, 0.0, 0.0)

        val velocity = FlightMovementUtil.orbitAround(
            currentPosition = currentPosition,
            currentVelocity = Vec3.ZERO,
            center = center,
            radius = 5.0,
            preferredY = 0.0,
            acceleration = 0.8,
            damping = 0.95
        )

        val radialDirection = currentPosition.subtract(center).normalize()
        assertTrue(
            velocity.dot(radialDirection) > 0.0,
            "orbitAround should add outward radial velocity when it is inside the orbit radius"
        )
    }

    @Test
    fun `orbit approach gets closer over repeated ticks`() {
        val center = Vec3.ZERO
        val targetRadius = 0.5
        var currentPosition = Vec3(10.0, 0.0, 0.0)
        var currentVelocity = Vec3.ZERO
        var previousDistance = currentPosition.distanceTo(center)
        var tick = 0

        while (previousDistance > targetRadius && tick < 30) {
            currentVelocity = FlightMovementUtil.orbitApproach(
                currentPosition = currentPosition,
                currentVelocity = currentVelocity,
                center = center,
                targetRadius = targetRadius,
                preferredY = 0.0,
                acceleration = 0.8,
                damping = 0.95,
                shrinkRate = 1.0
            )
            currentPosition = currentPosition.add(currentVelocity)
            val distance = currentPosition.distanceTo(center)

            assertTrue(
                distance <= previousDistance + 1.0E-6,
                "orbitApproach should not move farther from the target at tick $tick"
            )
            previousDistance = distance
            tick++
        }

        assertTrue(
            previousDistance <= targetRadius + 1.0E-6,
            "orbitApproach should reach the target radius"
        )
    }

    @Test
    fun `orbit approach cancels existing outward velocity`() {
        val center = Vec3.ZERO
        var currentPosition = Vec3(10.0, 0.0, 0.0)
        var currentVelocity = Vec3(1.6, 0.0, 1.2)
        var previousDistance = currentPosition.distanceTo(center)

        repeat(20) { tick ->
            currentVelocity = FlightMovementUtil.orbitApproach(
                currentPosition = currentPosition,
                currentVelocity = currentVelocity,
                center = center,
                targetRadius = 0.5,
                preferredY = 0.0,
                acceleration = 0.8,
                damping = 0.95,
                shrinkRate = 1.0
            )
            currentPosition = currentPosition.add(currentVelocity)
            val distance = currentPosition.distanceTo(center)

            assertTrue(
                distance <= previousDistance + 1.0E-6,
                "orbitApproach should pull inward even after outward velocity at tick $tick"
            )
            previousDistance = distance
        }
    }
}
