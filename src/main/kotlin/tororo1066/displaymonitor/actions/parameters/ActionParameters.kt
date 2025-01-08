package tororo1066.displaymonitor.actions.parameters

import org.bukkit.entity.Entity
import tororo1066.tororopluginapi.otherUtils.UsefulUtility

object ActionParameters {

    fun getEntityParameters(prefix: String, entity: Entity): Map<String, Any> {
        return UsefulUtility.sTry({
            val clazz = Class.forName("tororo1066.displaymonitor.actions.parameters.entity.${entity.type.name.lowercase().replaceFirstChar { it.uppercaseChar() }}EntityParameters")
            val instance = clazz.getDeclaredConstructor().newInstance() as AbstractEntityParameters<*>
            instance.getAllParameters(prefix, entity)
        }, {
            DefaultEntityParameters().getAllParameters(prefix, entity)
        })
    }
}