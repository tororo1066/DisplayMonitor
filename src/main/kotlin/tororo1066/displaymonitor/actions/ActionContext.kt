package tororo1066.displaymonitor.actions

import org.bukkit.Location
import org.bukkit.entity.Entity
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.actions.IPublicActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration
import java.util.UUID

class ActionContext(private val publicContext: IPublicActionContext): IActionContext {

    private var groupUUID: UUID = UUID.randomUUID()
    private var uuid: UUID = UUID.randomUUID()

    private var caster: Entity? = null
    private var target: Entity? = null
    private var location: Location? = null

    private var configuration: IAdvancedConfiguration? = null

    private val prepareParameters = HashMap<String, Any>()
    private var stop = false

    constructor(publicContext: IPublicActionContext, caster: Entity, location: Location): this(publicContext) {
        this.caster = caster
        this.target = caster
        this.location = location
    }

    constructor(publicContext: IPublicActionContext, caster: Entity): this(publicContext, caster, caster.location)

    override fun cloneWithRandomUUID(): IActionContext {
        val context = clone()
        context.uuid = UUID.randomUUID()
        return context
    }

    override fun clone(): IActionContext {
        return cloneWithNewPublicContext(publicContext)
    }

    override fun cloneWithNewPublicContext(publicActionContext: IPublicActionContext): IActionContext {
        val context = ActionContext(publicActionContext)
        context.groupUUID = groupUUID
        context.uuid = uuid
        context.caster = caster
        context.target = target
        context.location = location?.clone()
        context.configuration = configuration?.clone()
        return context
    }

    override fun getPublicContext(): IPublicActionContext {
        return publicContext
    }

    override fun getGroupUUID(): UUID {
        return groupUUID
    }

    override fun setGroupUUID(groupUUID: UUID) {
        this.groupUUID = groupUUID
    }

    override fun getUUID(): UUID {
        return uuid
    }

    override fun setUUID(uuid: UUID) {
        this.uuid = uuid
    }

    override fun getConfiguration(): IAdvancedConfiguration? {
        return configuration
    }

    override fun setConfiguration(configuration: IAdvancedConfiguration?) {
        configuration?.publicContext = publicContext
        this.configuration = configuration
    }

    override fun getCaster(): Entity? {
        return caster
    }

    override fun setCaster(caster: Entity?) {
        this.caster = caster
    }

    override fun getTarget(): Entity? {
        return target
    }

    override fun setTarget(target: Entity?) {
        this.target = target
    }

    override fun getLocation(): Location? {
        return location
    }

    override fun setLocation(location: Location?) {
        this.location = location
    }

    override fun getPrepareParameters(): java.util.HashMap<String, Any> {
        return prepareParameters
    }

    override fun getStop(): Boolean {
        return stop
    }

    override fun setStop(stop: Boolean) {
        this.stop = stop
    }

    override fun getDefaultParameters(): MutableMap<String, Any> {
        val map = HashMap<String, Any>()
//        caster?.let {
//            map["caster.name"] = it.name
//            map["caster.uuid"] = it.uniqueId.toString()
//            map["caster.location.x"] = it.location.x
//            map["caster.location.y"] = it.location.y
//            map["caster.location.z"] = it.location.z
//            map["caster.location.yaw"] = it.location.yaw
//            map["caster.location.pitch"] = it.location.pitch
//            map["caster.location.world"] = it.location.world.name
//        }
//        target?.let {
//            map["target.name"] = it.name
//            map["target.uuid"] = it.uniqueId.toString()
//            map["target.location.x"] = it.location.x
//            map["target.location.y"] = it.location.y
//            map["target.location.z"] = it.location.z
//            map["target.location.yaw"] = it.location.yaw
//            map["target.location.pitch"] = it.location.pitch
//            map["target.location.world"] = it.location.world.name
//        }
        location?.let {
            map["location.x"] = it.x
            map["location.y"] = it.y
            map["location.z"] = it.z
            map["location.yaw"] = it.yaw
            map["location.pitch"] = it.pitch
            map["location.world"] = it.world?.name ?: ""
        }
        return map
    }
}