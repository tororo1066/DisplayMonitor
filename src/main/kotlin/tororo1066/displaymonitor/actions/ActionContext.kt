package tororo1066.displaymonitor.actions

import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.displaymonitor.configuration.AdvancedConfiguration
import tororo1066.displaymonitor.elements.AbstractElement
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

class ActionContext(): Cloneable {

    var groupUUID: UUID = UUID.randomUUID()
    var uuid = UUID.randomUUID()

    var caster: Player? = null
    var location: Location? = null

    val elements = HashMap<String, AbstractElement>()

    var configuration: AdvancedConfiguration? = null
    val parameters get() = configuration?.parameters
    var stop = false

    constructor(caster: Player, location: Location): this() {
        this.caster = caster
        this.location = location
    }

    constructor(caster: Player): this() {
        this.caster = caster
        this.location = caster.location
    }

    fun cloneWithRandomUUID(): ActionContext {
        val context = clone()
        context.uuid = UUID.randomUUID()
        return context
    }

    public override fun clone(): ActionContext {
        val context = ActionContext()
        context.groupUUID = groupUUID
        context.uuid = uuid
        context.caster = caster
        context.location = location
        context.elements.putAll(elements)
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
            map["caster.world"] = it.world.name
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