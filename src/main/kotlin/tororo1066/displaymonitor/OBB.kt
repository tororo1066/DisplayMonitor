package tororo1066.displaymonitor

import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Vector
import org.joml.Vector3f
import kotlin.math.*


class OBB {
    var center: Vector // OBBの中心
        private set
    var axes: Array<Vector> // OBBの3つの軸（ベクトル）
        private set
    var halfWidths: DoubleArray // 各軸に沿った半径（スケール）
        private set

    constructor(center: Vector, axes: Array<Vector>, halfWidths: DoubleArray) {
        this.center = center
        this.axes = axes
        this.halfWidths = halfWidths
    }

    constructor(min: Vector, max: Vector) {
        // AABBの中心を求める
        this.center = min.clone().add(max).multiply(0.5)

        // 軸のベクトルを求める
        this.axes = arrayOf(Vector(1, 0, 0), Vector(0, 1, 0), Vector(0, 0, 1))

        // 半径は各軸に沿った幅の半分
        this.halfWidths = DoubleArray(3)
        halfWidths[0] = (max.x - min.x) / 2.0
        halfWidths[1] = (max.y - min.y) / 2.0
        halfWidths[2] = (max.z - min.z) / 2.0
    }

    // AABBからOBBを作成するコンストラクタ
    constructor(min: Vector, max: Vector, yaw: Double, pitch: Double) {
        // AABBの中心を求める
        this.center = min.clone().add(max).multiply(0.5)

        // 軸のベクトルを回転させる
        this.axes = arrayOf(Vector(1, 0, 0), Vector(0, 1, 0), Vector(0, 0, 1))

        for (i in 0..2) {
            axes[i] = axes[i].rotateAroundX(pitch)
            axes[i] = axes[i].rotateAroundY(yaw)
            axes[i] = axes[i].normalize()
        }

        // 半径は各軸に沿った幅の半分
        this.halfWidths = DoubleArray(3)
        halfWidths[0] = (max.x - min.x) / 2.0
        halfWidths[1] = (max.y - min.y) / 2.0
        halfWidths[2] = (max.z - min.z) / 2.0
    }

    constructor(min: Vector, max: Vector, rotationAxis: Vector, rotationAngle: Double) {
        // AABBの中心を求める
        this.center = min.clone().add(max).multiply(0.5)

        // 軸のベクトルを回転させる
        this.axes = arrayOf(Vector(1, 0, 0), Vector(0, 1, 0), Vector(0, 0, 1))

        for (i in 0..2) {
            axes[i] = axes[i].rotateAroundAxis(rotationAxis, rotationAngle)
            axes[i] = axes[i].normalize()
        }

        // 半径は各軸に沿った幅の半分
        this.halfWidths = DoubleArray(3)
        halfWidths[0] = (max.x - min.x) / 2.0
        halfWidths[1] = (max.y - min.y) / 2.0
        halfWidths[2] = (max.z - min.z) / 2.0
    }

    fun scale(vector3f: Vector3f) {
        for (i in 0..2) {
            halfWidths[i] = halfWidths[i] * vector3f[i]
        }
    }

    fun rotate(yaw: Double, pitch: Double) {
        for (i in 0..2) {
            axes[i] = axes[i].rotateAroundX(pitch)
            axes[i] = axes[i].rotateAroundY(yaw)
            axes[i] = axes[i].normalize()
        }
    }

    fun rotate(rotationAxis: Vector, rotationAngle: Double) {
        if (rotationAngle == 0.0) return
        if (rotationAxis.lengthSquared() == 0.0) return
        for (i in 0..2) {
            axes[i] = axes[i].rotateAroundAxis(rotationAxis, rotationAngle)
            axes[i] = axes[i].normalize()
        }
    }

    fun modifyBy(p: Player, display: Display) {
        val rightQuaternion = display.transformation.rightRotation
        val rightRotation = Vector(rightQuaternion.x.toDouble(), rightQuaternion.y.toDouble(), rightQuaternion.z.toDouble())
        val rightRotationAngle = rightQuaternion.w.toDouble()
        rotate(rightRotation, rightRotationAngle)
        scale(display.transformation.scale)
        val leftQuaternion = display.transformation.leftRotation
        val leftRotation = Vector(leftQuaternion.x.toDouble(), leftQuaternion.y.toDouble(), leftQuaternion.z.toDouble())
        val leftRotationAngle = leftQuaternion.w.toDouble()
        rotate(leftRotation, leftRotationAngle)

        //相対的に移動する
        center.add(
            Vector.fromJOML(display.transformation.translation)
                .rotateAroundY(Math.toRadians(-p.eyeLocation.yaw.toDouble()))
                .multiply(Vector(-1.0, 1.0, -1.0))
        )

        when (display.billboard) {
            Display.Billboard.FIXED -> {}
            Display.Billboard.CENTER -> {
                val yaw = Math.toRadians(-p.eyeLocation.yaw.toDouble())
                val pitch = Math.toRadians(p.eyeLocation.pitch.toDouble())
                rotate(yaw, pitch)
            }
            Display.Billboard.VERTICAL -> {
                val yaw = Math.toRadians(-p.eyeLocation.yaw.toDouble())
                rotate(yaw, 0.0)
            }
            Display.Billboard.HORIZONTAL -> {
                val pitch = Math.toRadians(p.eyeLocation.pitch.toDouble())
                rotate(0.0, pitch)
            }
        }

        //テキストディスプレイの場合は中央が一番下にあるので、中心をy軸方向に半分上げる
        if (display is TextDisplay) {
            center.y += halfWidths[1]
        }
    }

    private operator fun Vector.get(i: Int): Double {
        return when (i) {
            0 -> x
            1 -> y
            2 -> z
            else -> throw IndexOutOfBoundsException()
        }
    }

    // OBBとレイの交差判定
    fun rayTrace(start: Vector, direction: Vector, maxDistance: Double): Boolean {
        val t = center.clone().subtract(start)
        val p = Vector(t.dot(axes[0]), t.dot(axes[1]), t.dot(axes[2]))
        val d = Vector(direction.dot(axes[0]), direction.dot(axes[1]), direction.dot(axes[2]))

        var tMin = -maxDistance
        var tMax = maxDistance

        for (i in 0..2) {
            if (abs(d[i]) < 1.0E-6) {
                if (p[i] < -halfWidths[i] || halfWidths[i] < p[i]) {
                    return false
                }
            } else {
                val ood = 1.0 / d[i]
                var t1 = (-halfWidths[i] - p[i]) * ood
                var t2 = (halfWidths[i] - p[i]) * ood

                if (t1 > t2) {
                    val tmp = t1
                    t1 = t2
                    t2 = tmp
                }

                tMin = max(tMin, t1)
                tMax = min(tMax, t2)

                if (tMin > tMax) {
                    return false
                }
            }
        }

        return true
    }

    fun showParticle(player: Player) {
        val corners = arrayOfNulls<Vector>(8)
        for (i in 0..7) {
            corners[i] = center.clone()
            for (j in 0..2) {
                val sign = if ((i and (1 shl j)) == 0) -1 else 1
                corners[i]!!.add(axes[j].clone().multiply(sign * halfWidths[j]))
            }
        }

        for (corner in corners) {
            val loc = player.world.getBlockAt(
                corner!!.blockX, corner.blockY, corner.blockZ
            ).location
            player.spawnParticle(Particle.REDSTONE, loc, 1, 0.0, 0.0, 0.0, DustOptions(Color.LIME, 0.5f))
        }

        for (i in 0..7) {
            for (j in i + 1..7) {
                if (Integer.bitCount(i xor j) == 1) {
                    drawLine(player, corners[i], corners[j])
                }
            }
        }
    }

    private fun drawLine(player: Player, start: Vector?, end: Vector?) {
        val distance = start!!.distance(end!!)
        val step = end.clone().subtract(start).normalize().multiply(0.2) // 線の分解能
        val current = start.clone()

        var i = 0.0
        while (i < distance) {
            val loc = current.toLocation(player.world)
            player.spawnParticle(Particle.REDSTONE, loc, 1, 0.0, 0.0, 0.0, DustOptions(Color.LIME, 0.5f))
            current.add(step)
            i += 0.2
        }
    }
}
