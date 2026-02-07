package tororo1066.displaymonitor

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.BoundingBox
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.collections.get
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.text.toDouble
import kotlin.times


class OBB {
    var center: Vector3f // OBBの中心
        private set
    var axes: Array<Vector3f> // OBBの3つの軸（ベクトル）
        private set
    var halfWidths: Vector3f // 各軸に沿った半径（スケール）
        private set

    constructor(center: Vector3f, axes: Array<Vector3f>, halfWidths: Vector3f) {
        this.center = center
        this.axes = axes
        this.halfWidths = halfWidths
    }

    constructor(min: Vector3f, max: Vector3f) {
        // AABBの中心を求める
        this.center = Vector3f((max.x + min.x) / 2f, (max.y + min.y) / 2f, (max.z + min.z) / 2f)

        // 軸のベクトルを求める
        this.axes = arrayOf(Vector3f(1f, 0f, 0f), Vector3f(0f, 1f, 0f), Vector3f(0f, 0f, 1f))

        // 半径は各軸に沿った幅の半分
        this.halfWidths = Vector3f((max.x - min.x) / 2f, (max.y - min.y) / 2f, (max.z - min.z) / 2f)
    }

    fun scale(vector3f: Vector3f) {
        for (i in 0..2) {
            halfWidths[i] = halfWidths[i] * vector3f[i]
        }
    }

    private operator fun Vector3f.set(i: Int, value: Float) {
        when (i) {
            0 -> this.x = value
            1 -> this.y = value
            2 -> this.z = value
        }
    }

    fun rotate(quaternionf: Quaternionf) {
        for (i in 0..2) {
            axes[i] = axes[i].rotate(quaternionf)
        }
    }

    fun rotateY(yaw: Float) {
        for (i in 0..2) {
            axes[i] = axes[i].rotateY(yaw)
        }
    }

    fun rotateX(pitch: Float) {
        for (i in 0..2) {
            axes[i] = axes[i].rotateX(pitch)
        }
    }

    fun modifyBy(p: Player, display: Display) {
        //ここでOBBを編集する

        rotate(display.transformation.rightRotation)
        scale(display.transformation.scale)
        rotate(display.transformation.leftRotation)

        //相対的に移動する

        val translation = display.transformation.translation.cloneVector()


        when (display.billboard) {
            //固定
            Display.Billboard.FIXED -> {
                center.add(
                    translation
                        .rotateX(Math.toRadians(display.location.pitch.toDouble()).toFloat())
                        .rotateY(Math.toRadians(-display.location.yaw.toDouble()).toFloat())
                )
                val x = Math.toRadians(display.location.pitch.toDouble()).toFloat()
                rotateX(x)
                val y = Math.toRadians(-display.location.yaw.toDouble()).toFloat()
                rotateY(y)
            }
            //プレイヤーの視線方向に合わせる(横方向のみ)
            Display.Billboard.VERTICAL -> {
                center.add(
                    translation
                        .rotateX(Math.toRadians(display.location.pitch.toDouble()).toFloat())
                        .rotateY(Math.toRadians(-p.eyeLocation.yaw.toDouble()).toFloat() - Math.toRadians(180.0).toFloat())
                )
                val x = Math.toRadians(display.location.pitch.toDouble()).toFloat()
                rotateX(x)
                val y = Math.toRadians(-p.eyeLocation.yaw.toDouble()).toFloat() - Math.toRadians(180.0).toFloat()
                rotateY(y)
            }
            //プレイヤーの視線方向に合わせる(縦方向のみ)
            Display.Billboard.HORIZONTAL -> {
                center.add(
                    translation
                        .rotateX(Math.toRadians(-p.eyeLocation.pitch.toDouble()).toFloat())
                        .rotateY(Math.toRadians(-display.location.yaw.toDouble()).toFloat())
                )
                val x = Math.toRadians(-p.eyeLocation.pitch.toDouble()).toFloat()
                rotateX(x)
                val y = Math.toRadians(-display.location.yaw.toDouble()).toFloat()
                rotateY(y)
            }
            //プレイヤーの視線方向に合わせる(全方向)
            Display.Billboard.CENTER -> {
                center.add(
                    translation
                        .rotateX(Math.toRadians(-p.eyeLocation.pitch.toDouble()).toFloat())
                        .rotateY(Math.toRadians(-p.eyeLocation.yaw.toDouble()).toFloat() - Math.toRadians(180.0).toFloat())
                )
                val x = Math.toRadians(-p.eyeLocation.pitch.toDouble()).toFloat()
                rotateX(x)
                val y = Math.toRadians(-p.eyeLocation.yaw.toDouble()).toFloat() - Math.toRadians(180.0).toFloat()
                rotateY(y)
            }
        }

        //テキストディスプレイの場合は中央が一番下にあるので、中心をy軸方向に半分上げる
        if (display is TextDisplay) {
            center.y += halfWidths[1]
        }

        //ブロックディスプレイの場合軸方向に半分ずらす
        if (display is BlockDisplay) {
            center.add(axes[0].cloneVector().mul(0.5f))
            center.add(axes[1].cloneVector().mul(0.5f))
            center.add(axes[2].cloneVector().mul(0.5f))
        }
    }

    private fun Vector3f.cloneVector(): Vector3f {
        return Vector3f(this.x, this.y, this.z)
    }

    fun intersect(box: BoundingBox): Boolean {
        val min = Vector3f(box.minX.toFloat(), box.minY.toFloat(), box.minZ.toFloat())
        val max = Vector3f(box.maxX.toFloat(), box.maxY.toFloat(), box.maxZ.toFloat())
        val obb = OBB(min, max)
        return intersect(obb)
    }


    fun intersect(obb: OBB): Boolean {
        // 2つのOBBの中心間のベクトル
        val t = Vector3f(center.x - obb.center.x, center.y - obb.center.y, center.z - obb.center.z)

        // 2つのOBBの中心間のベクトルを各軸に投影
        val p = Vector3f(t.dot(axes[0]), t.dot(axes[1]), t.dot(axes[2]))
        val d = Vector3f(obb.axes[0].dot(axes[0]), obb.axes[0].dot(axes[1]), obb.axes[0].dot(axes[2]))

        // 2つのOBBの中心間のベクトルを各軸に投影したものの絶対値
        val ad = Vector3f(abs(d.x), abs(d.y), abs(d.z))

        // 2つのOBBの中心間のベクトルを各軸に投影したものの絶対値がOBBの半径の和よりも小さいかどうか
        if (abs(p.x) > halfWidths.x + ad.x) return false
        if (abs(p.y) > halfWidths.y + ad.y) return false
        if (abs(p.z) > halfWidths.z + ad.z) return false

        // 2つのOBBの中心間のベクトルを各軸に投影したものの絶対値がOBBの半径の和よりも小さい場合は交差している
        return true
    }

    // OBBとレイの交差判定
    fun rayTrace(start: Vector3f, direction: Vector3f, maxDistance: Double): Boolean {
        val t = Vector3f(center.x - start.x, center.y - start.y, center.z - start.z)
        val p = Vector3f(t.dot(axes[0]), t.dot(axes[1]), t.dot(axes[2]))
        val d = Vector3f(direction.dot(axes[0]), direction.dot(axes[1]), direction.dot(axes[2]))

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

    companion object {
        val villagerHappy = try {
            Particle.valueOf("HAPPY_VILLAGER")
        } catch (_: NoSuchFieldError) {
            Particle.VILLAGER_HAPPY
        }
    }

    fun showParticle(world: World, player: Player?) {
        // nullable をやめて、必ず 8 つの角を生成する
        val corners: Array<Vector3f> = Array(8) { i ->
            val c = Vector3f(center) // center のコピー
            for (j in 0..2) {
                val sign = if ((i and (1 shl j)) == 0) -1f else 1f
                // axes[j] を破壊しないようコピーしてから加算
                val offset = Vector3f(axes[j]).mul(halfWidths[j] * sign)
                c.add(offset)
            }
            c
        }

        // 辺（1bit だけ異なる頂点ペア）だけ線分描画
        for (i in 0..7) {
            for (j in i + 1..7) {
                if (Integer.bitCount(i xor j) == 1) {
                    drawLine(world, player, corners[i], corners[j])
                }
            }
        }
    }

    private fun drawLine(world: World, player: Player?, start: Vector3f, end: Vector3f) {
        val distance = start.distance(end)
        if (distance <= 1.0e-6f) return

        val stepLength = 0.2f
        val steps = max(1, floor((distance / stepLength).toDouble()).toInt())

        val dir = end.cloneVector().sub(start).mul(1f / steps.toFloat()) // 1ステップあたりの増分
        val current = start.cloneVector()

        val loc = Location(world, 0.0, 0.0, 0.0)

        for (i in 0..steps) {
            loc.x = current.x.toDouble()
            loc.y = current.y.toDouble()
            loc.z = current.z.toDouble()

            if (player != null) {
                player.spawnParticle(villagerHappy, loc, 1, 0.0, 0.0, 0.0)
            } else {
                world.spawnParticle(villagerHappy, loc, 1, 0.0, 0.0, 0.0)
            }

            current.add(dir)
        }
    }
}
