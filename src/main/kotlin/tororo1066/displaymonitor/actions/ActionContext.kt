package tororo1066.displaymonitor.actions

import org.bukkit.Location
import org.bukkit.entity.Entity
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import java.util.UUID

class ActionContext(val publicContext: PublicActionContext): Cloneable {

    var groupUUID: UUID = UUID.randomUUID()
    var uuid = UUID.randomUUID()

    var caster: Entity? = null
    var target: Entity? = null
    var location: Location? = null

    var configuration: AdvancedConfiguration? = null

    val prepareParameters = mutableMapOf<String, Any>()
    var stop = false

    constructor(publicContext: PublicActionContext, caster: Entity, location: Location): this(publicContext) {
        this.caster = caster
        this.target = caster
        this.location = location
    }

    constructor(publicContext: PublicActionContext, caster: Entity): this(publicContext, caster, caster.location)

    fun cloneWithRandomUUID(): ActionContext {
        val context = clone()
        context.uuid = UUID.randomUUID()
        return context
    }

    public override fun clone(): ActionContext {
        val context = ActionContext(publicContext)
        context.groupUUID = groupUUID
        context.uuid = uuid
        context.caster = caster
        context.target = target
        context.location = location
        context.configuration = configuration
        return context
    }

    fun getDefaultParameters(): MutableMap<String, Any> {
        val map = HashMap<String, Any>()
        caster?.let {
            map["caster.name"] = it.name
            map["caster.uuid"] = it.uniqueId.toString()
            map["caster.location.x"] = it.location.x
            map["caster.location.y"] = it.location.y
            map["caster.location.z"] = it.location.z
            map["caster.location.yaw"] = it.location.yaw
            map["caster.location.pitch"] = it.location.pitch
            map["caster.location.world"] = it.location.world.name
        }
        target?.let {
            map["target.name"] = it.name
            map["target.uuid"] = it.uniqueId.toString()
            map["target.location.x"] = it.location.x
            map["target.location.y"] = it.location.y
            map["target.location.z"] = it.location.z
            map["target.location.yaw"] = it.location.yaw
            map["target.location.pitch"] = it.location.pitch
            map["target.location.world"] = it.location.world.name
        }
        location?.let {
            map["location.x"] = it.x
            map["location.y"] = it.y
            map["location.z"] = it.z
            map["location.yaw"] = it.yaw
            map["location.pitch"] = it.pitch
            map["location.world"] = it.world.name
        }
        return map
    }
}