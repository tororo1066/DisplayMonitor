package tororo1066.displaymonitor.storage

import tororo1066.displaymonitor.actions.ActionContext
import tororo1066.displaymonitor.actions.*
import tororo1066.displaymonitor.actions.builtin.*
import java.util.UUID

object ActionStorage {
    val actions = mutableMapOf<String, Class<out AbstractAction>>()
    val contextStorage = mutableMapOf<UUID, ActionContext>()

    init {
        actions["SummonElement"] = SummonElement::class.java
        actions["EditElement"] = EditElement::class.java
        actions["Message"] = MessageAction::class.java
        actions["Delay"] = DelayAction::class.java
        actions["Command"] = CommandAction::class.java
        actions["RemoveAllElement"] = RemoveAllElement::class.java
    }
}