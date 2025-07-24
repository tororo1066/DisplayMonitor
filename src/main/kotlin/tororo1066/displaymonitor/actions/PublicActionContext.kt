package tororo1066.displaymonitor.actions

import tororo1066.displaymonitor.storage.WorkspaceStorage
import tororo1066.displaymonitorapi.actions.IPublicActionContext
import tororo1066.displaymonitorapi.elements.IAbstractElement
import tororo1066.displaymonitorapi.workspace.IAbstractWorkspace

class PublicActionContext: IPublicActionContext {

    private val elements = HashMap<String, IAbstractElement>()
    private var stop = false
    private var shouldAutoStop = true
    private var parameters: MutableMap<String, Any> = mutableMapOf()
    private val prepareParameters: MutableMap<String, Any> = mutableMapOf()
    private var workspace: IAbstractWorkspace = WorkspaceStorage.DisplayMonitorWorkspace.instance

    override fun getElements(): HashMap<String, IAbstractElement> {
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

    override fun getPrepareParameters(): Map<String, Any> {
        return prepareParameters
    }

    override fun getWorkspace(): IAbstractWorkspace {
        return workspace
    }

    override fun setWorkspace(workspace: IAbstractWorkspace) {
        this.workspace = workspace
    }
}