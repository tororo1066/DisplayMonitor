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

    lateinit var caster: Player
    lateinit var location: Location

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
        map["caster.name"] = caster.name
        map["caster.uuid"] = caster.uniqueId.toString()
        map["caster.location.x"] = caster.location.x
        map["caster.location.y"] = caster.location.y
        map["caster.location.z"] = caster.location.z
        map["caster.location.yaw"] = caster.location.yaw
        map["caster.location.pitch"] = caster.location.pitch
        map["caster.world"] = caster.world.name
        map["location.x"] = location.x
        map["location.y"] = location.y
        map["location.z"] = location.z
        map["location.yaw"] = location.yaw
        map["location.pitch"] = location.pitch
        map["location.world"] = location.world.name
        return map
    }
}