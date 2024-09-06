package tororo1066.displaymonitor.actions

import org.bukkit.Location
import org.bukkit.entity.Player
import tororo1066.displaymonitor.element.AbstractElement
import java.util.UUID

class ActionContext: Cloneable {

    var uuid = UUID.randomUUID()

    lateinit var caster: Player
    lateinit var location: Location

    val elements = HashMap<String, AbstractElement>()

    fun cloneWithRandomUUID(): ActionContext {
        val context = clone()
        context.uuid = UUID.randomUUID()
        return context
    }

    public override fun clone(): ActionContext {
        val context = ActionContext()
        context.caster = caster
        context.location = location
        context.elements.putAll(elements)
        return context
    }
}