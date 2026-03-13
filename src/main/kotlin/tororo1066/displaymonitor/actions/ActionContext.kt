package tororo1066.displaymonitor.actions

import org.bukkit.Location
import org.bukkit.entity.Entity
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.actions.IPublicActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration

class ActionContext(private val publicContext: IPublicActionContext): IActionContext {

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

    override fun clone(): IActionContext {
        return cloneWithNewPublicContext(publicContext)
    }

    override fun cloneWithNewPublicContext(publicActionContext: IPublicActionContext): IActionContext {
        val context = ActionContext(publicActionContext)
        context.caster = caster
        context.target = target
        context.location = location?.clone()
        context.configuration = configuration?.clone()
        return context
    }

    override fun getPublicContext(): IPublicActionContext {
        return publicContext
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

    override fun getAllParameters(): Map<String, Any> {
        val map = HashMap<String, Any>()
        configuration?.let {
            map.putAll(it.parameters)
        }
        map.putAll(publicContext.parameters)
        return map
    }
}