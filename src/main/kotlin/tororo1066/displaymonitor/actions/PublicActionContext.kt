package tororo1066.displaymonitor.actions

import tororo1066.displaymonitor.elements.AbstractElement

class PublicActionContext {

    val elements = mutableMapOf<String, AbstractElement>()
    var stop = false
}