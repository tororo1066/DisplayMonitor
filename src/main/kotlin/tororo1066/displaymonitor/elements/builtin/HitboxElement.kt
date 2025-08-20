package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType
import org.joml.Vector3f
import tororo1066.displaymonitor.OBB
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.elements.Settable
import tororo1066.tororopluginapi.SJavaPlugin
import kotlin.math.max

open class HitboxElement: AbstractElement() {

    override val syncGroup = false

    @Settable
    var width = 1.0
    @Settable var height = 1.0
    @Settable var depth = 1.0
    @Settable var onCollide = Execute.empty()
    @Settable var visualizeHitbox = false

    var location: Location? = null
    var removed = false

    override fun spawn(entity: Entity?, location: Location) {
        this.location = location
        startTick(entity)
    }

    override fun remove() {
        removed = true
        tickTask?.cancel()
    }

    override fun tick(entity: Entity?) {
        val loc = location ?: return

        val obb = OBB(
            min = Vector3f((loc.x - width / 2).toFloat(), (loc.y - height / 2).toFloat(), (loc.z - depth / 2).toFloat()),
            max = Vector3f((loc.x + width / 2).toFloat(), (loc.y + height / 2).toFloat(), (loc.z + depth / 2).toFloat())
        )
        obb.rotateX(Math.toRadians(loc.pitch.toDouble()).toFloat())
        obb.rotateY(Math.toRadians(loc.yaw.toDouble()).toFloat())

        if (visualizeHitbox) {
            obb.showParticle(loc.world, null)
        }

        val entities = loc.world.getNearbyEntities(loc, max(width, depth), height, max(width, depth))
        entities.forEach { nearby ->
            if (nearby.persistentDataContainer.has(NamespacedKey(SJavaPlugin.plugin, "displayentity"), PersistentDataType.STRING)) return@forEach
            if (!obb.intersect(nearby.boundingBox)) return@forEach
            runExecute(onCollide) {
                it.target = nearby
            }
            if (removed) return
        }
    }

    override fun attachEntity(entity: Entity) {
        location = entity.location
    }

    override fun move(location: Location) {
        this.location = location
    }

    override fun applyChanges() {}


}