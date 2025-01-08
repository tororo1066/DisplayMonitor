package tororo1066.displaymonitor.actions.parameters

import org.bukkit.entity.Entity

abstract class AbstractEntityParameters<T: Entity> {

    abstract fun getParameters(prefix: String, entity: T): Map<String, Any>

    fun withNormalParameters(prefix: String, entity: T): Map<String, Any> {
        val map = HashMap<String, Any>()
        map.putAll(getParameters(prefix, entity))

        map["${prefix}.name"] = entity.name
        map["${prefix}.uuid"] = entity.uniqueId.toString()
        map["${prefix}.location.world"] = entity.location.world?.name ?: "null"
        map["${prefix}.location.x"] = entity.location.x
        map["${prefix}.location.y"] = entity.location.y
        map["${prefix}.location.z"] = entity.location.z
        map["${prefix}.location.yaw"] = entity.location.yaw
        map["${prefix}.location.pitch"] = entity.location.pitch
        map["${prefix}.type"] = entity.type.name

        return map
    }

    @Suppress("UNCHECKED_CAST")
    fun getAllParameters(prefix: String, entity: Entity): Map<String, Any> {
        return withNormalParameters(prefix, entity as T)
    }
}