package tororo1066.displaymonitor.actions

import tororo1066.displaymonitor.storage.WorkspaceStorage
import tororo1066.displaymonitorapi.actions.IPublicActionContext
import tororo1066.displaymonitorapi.elements.IAbstractElement
import tororo1066.displaymonitorapi.workspace.IAbstractWorkspace
import java.util.concurrent.ConcurrentHashMap

class PublicActionContext: IPublicActionContext {

    private val elements = ConcurrentHashMap<String, IAbstractElement>()
    private var stop = false
    private var shouldAutoStop = true
    private var parameters: MutableMap<String, Any> = mutableMapOf()
    private var workspace: IAbstractWorkspace = WorkspaceStorage.DisplayMonitorWorkspace.instance

    override fun getElements(): Map<String, IAbstractElement> {
        return elements
    }

    override fun getStop(): Boolean {
        return stop
    }

    override fun getShouldAutoStop(): Boolean {
        return shouldAutoStop
    }

    override fun setShouldAutoStop(shouldStop: Boolean) {
        shouldAutoStop = shouldStop
    }

    override fun setStop(stop: Boolean) {
        this.stop = stop
    }

    override fun getParameters(): MutableMap<String, Any> {
        return parameters
    }

    override fun setParameters(parameters: MutableMap<String, Any>) {
        this.parameters = parameters
    }

    override fun getWorkspace(): IAbstractWorkspace {
        return workspace
    }

    override fun setWorkspace(workspace: IAbstractWorkspace) {
        this.workspace = workspace
    }

    override fun getAllElements(): Map<String, IAbstractElement> {
        val result = mutableMapOf<String, IAbstractElement>()
        for ((key, element) in elements) {
            result[key] = element
            result.putAll(collectElements(element, key))
        }
        return result
    }

    private fun collectElements(
        element: IAbstractElement,
        parentKey: String
    ): Map<String, IAbstractElement> {
        val result = mutableMapOf<String, IAbstractElement>()
        if (element.hasChildren()) {
            for ((key, child) in element.children) {
                val key = "$parentKey.$key"
                result[key] = child
                result.putAll(collectElements(child, key))
            }
        }
        return result
    }

    override fun shallowCopy(): IPublicActionContext {
        val newContext = PublicActionContext()
        newContext.parameters = parameters.toMutableMap()
        newContext.workspace = workspace
        return newContext
    }
}