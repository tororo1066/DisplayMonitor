package tororo1066.displaymonitor.elements.builtin

import org.bukkit.entity.Entity
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.elements.Settable
import tororo1066.tororopluginapi.sEvent.SEvent
import tororo1066.tororopluginapi.sEvent.SEventUnit
import java.lang.ref.WeakReference

abstract class DetachableElement: AbstractElement() {

    @ParameterDoc(
        name = "onDetach",
        description = "Elementが他のエンティティから外れた時のアクション。"
    )
    @Settable var onDetach: Execute? = null

    protected val sEvent = SEvent()
    private val eventUnits = mutableListOf<SEventUnit<*>>()

    private var attachedEntity: WeakReference<Entity>? = null

    override fun attachEntity(entity: Entity) {
        val currentAttachedRef = attachedEntity
        attachedEntity = WeakReference(entity)
        val currentAttached = currentAttachedRef?.get()
        if (currentAttached != null && currentAttached != entity) {
            runExecute(onDetach) {
                it.target = currentAttached
                it.location = currentAttached.location
            }
        }

        if (currentAttachedRef == null || currentAttachedRef.get() != entity) {
            eventUnits.forEach { it.unregister() }
            eventUnits.clear()

            val quit = sEvent.register<PlayerQuitEvent> { e ->
                val attached = attachedEntity?.get() ?: return@register
                if (attached.uniqueId == e.player.uniqueId) {
                    runExecute(onDetach) {
                        it.target = e.player
                        it.location = e.player.location
                    }
                }
            }

            val changedWorld = sEvent.register<PlayerChangedWorldEvent> { e ->
                val attached = attachedEntity?.get() ?: return@register
                if (attached.uniqueId == e.player.uniqueId) {
                    runExecute(onDetach) {
                        it.target = e.player
                        it.location = e.player.location
                    }
                }
            }

            val death = sEvent.register<PlayerDeathEvent> { e ->
                val attached = attachedEntity?.get() ?: return@register
                if (attached.uniqueId == e.entity.uniqueId) {
                    runExecute(onDetach) {
                        it.target = e.entity
                        it.location = e.entity.location
                    }
                }
            }

            eventUnits.addAll(listOf(quit, changedWorld, death))
        }
    }
}