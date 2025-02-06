package tororo1066.displaymonitor.actions

import tororo1066.displaymonitorapi.actions.IPublicActionContext
import tororo1066.displaymonitorapi.elements.IAbstractElement

class PublicActionContext: IPublicActionContext {

    private val elements = HashMap<String, IAbstractElement>()
    private var stop = false
    private var shouldAutoStop = true

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
}