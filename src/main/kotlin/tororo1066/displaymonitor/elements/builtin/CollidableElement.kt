package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType
import org.joml.Vector3f
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.hitbox.OBB
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.elements.Settable
import tororo1066.tororopluginapi.SJavaPlugin
import java.lang.ref.WeakReference
import java.util.UUID
import kotlin.math.max

@ClassDoc(
    name = "CollidableElement",
    description = "衝突判定を行うElement。"
)
open class CollidableElement: AbstractElement() {

    override val syncGroup = false

    @ParameterDoc(
        name = "width",
        description = "判定する幅。",
        default = "1.0"
    )
    @Settable var width = 1.0

    @ParameterDoc(
        name = "height",
        description = "判定する高さ。",
        default = "1.0"
    )
    @Settable var height = 1.0

    @ParameterDoc(
        name = "depth",
        description = "判定する奥行。",
        default = "1.0"
    )
    @Settable var depth = 1.0

    @ParameterDoc(
        name = "onCollide",
        description = "衝突時に実行するアクション。"
    )
    @Settable var onCollide: Execute? = null

    @ParameterDoc(
        name = "switchHover",
        description = "衝突時のアクションを切り替えた時のみに実行するか。",
        default = "true"
    )
    @Settable var switchCollide = true

    @ParameterDoc(
        name = "visualizeHitbox",
        description = "クリックの判定範囲を表示するか。 (デバッグ用)",
        default = "false"
    )
    @Settable var visualizeHitbox = false

    var location: Location? = null
    var removed = false
    var attachedEntity: WeakReference<Entity>? = null

    val collidedEntityUUIDs = mutableSetOf<UUID>()

    override fun spawn(entity: Entity?, location: Location) {
        this.location = location
        startTick(entity)
    }

    override fun remove() {
        removed = true
        stopTick()
    }

    override fun tick(entity: Entity?) {
        val loc = attachedEntity?.get()?.location ?: location ?: return

        val obb = OBB(
            min = Vector3f((loc.x - width / 2).toFloat(), (loc.y - height / 2).toFloat(), (loc.z - depth / 2).toFloat()),
            max = Vector3f((loc.x + width / 2).toFloat(), (loc.y + height / 2).toFloat(), (loc.z + depth / 2).toFloat())
        )
        obb.rotateX(Math.toRadians(loc.pitch.toDouble()).toFloat())
        obb.rotateY(Math.toRadians(loc.yaw.toDouble()).toFloat())

        if (visualizeHitbox) {
            obb.showParticle(loc.world, null)
        }

        val length = max(width, depth)

        val entities = loc.world.getNearbyEntities(loc, length, height, length)
        entities.forEach { nearby ->
            if (nearby == entity) return@forEach
            if (nearby.persistentDataContainer.has(NamespacedKey(SJavaPlugin.plugin, "displayentity"), PersistentDataType.STRING)) return@forEach

            val collided = obb.intersect(nearby.boundingBox)
            if (collided) {
                if (switchCollide) {
                    if (!collidedEntityUUIDs.contains(nearby.uniqueId)) {
                        collidedEntityUUIDs.add(nearby.uniqueId)
                        runExecute(onCollide) {
                            it.target = nearby
                        }
                    }
                } else {
                    runExecute(onCollide) {
                        it.target = nearby
                    }
                }
            } else {
                if (switchCollide) {
                    collidedEntityUUIDs.remove(nearby.uniqueId)
                }
            }

            if (removed) return
        }
    }

    override fun attachEntity(entity: Entity) {
        location = entity.location
        attachedEntity = WeakReference(entity)
    }

    override fun move(location: Location) {
        this.location = location
    }

    override fun applyChanges() {}
}