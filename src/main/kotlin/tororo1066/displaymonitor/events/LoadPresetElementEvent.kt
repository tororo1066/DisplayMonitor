package tororo1066.displaymonitor.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class LoadPresetElementEvent: Event() {

    companion object {
        private val handlers = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }

    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }
}