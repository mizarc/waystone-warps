package dev.mizarc.waystonewarps

import org.bukkit.util.Vector

enum class Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST;

    companion object {
        fun fromVector(vector: Vector): Direction {
            return if (vector.z == -1.0) {
                NORTH
            } else if (vector.z == 1.0) {
                SOUTH
            } else if (vector.x == 1.0) {
                EAST
            } else {
                WEST
            }
        }

        fun toVector(direction: Direction): Vector {
            return when(direction) {
                NORTH -> Vector(0, 0, -1)
                SOUTH -> Vector(0, 0, 1)
                EAST -> Vector(1, 0, 0)
                WEST -> Vector(-1, 0, 0)
            }
        }

        fun toYaw(direction: Direction): Float {
            return when(direction) {
                NORTH -> 180f
                SOUTH -> 0f
                EAST -> -90f
                WEST -> 90f
            }
        }
    }
}