package tororo1066.displaymonitor.actions.parameters

import org.bukkit.entity.Entity

class DefaultEntityParameters: AbstractEntityParameters<Entity>() {

    override fun getParameters(prefix: String, entity: Entity): Map<String, Any> {
        return mapOf()
    }
}