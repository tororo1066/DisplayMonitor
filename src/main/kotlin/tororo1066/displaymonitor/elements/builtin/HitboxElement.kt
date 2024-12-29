package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitor.elements.AsyncExecute
import tororo1066.displaymonitor.elements.Settable

class HitboxElement: AbstractElement() {

    override val syncGroup = false

    @Settable var width = 1.0
    @Settable var height = 1.0
    @Settable var onCollide = AsyncExecute.empty()

    var location: Location? = null

    override fun spawn(p: Player?, location: Location) {
        this.location = location
        startTick(p)
    }

    override fun remove() {
        tickTask?.cancel()
    }

    override fun tick(p: Player?) {
        val loc = location ?: return
        val entities = loc.world.getNearbyEntities(loc, width, height, width)
        entities.forEach { entity ->
            runExecute(onCollide) {
//                it.caster = entity
            }
        }
    }

    override fun attachEntity(entity: Entity) {
        TODO("Not yet implemented")
    }

    override fun move(location: Location) {
        TODO("Not yet implemented")
    }

    override fun applyChanges() {
        TODO("Not yet implemented")
    }


}