package tororo1066.displaymonitor.storage

import tororo1066.displaymonitor.element.*

object ElementStorage {
    val presetElements = mutableMapOf<String, AbstractElement>()
    val elementClasses = mutableMapOf<String, Class<out AbstractElement>>()

    init {
        elementClasses["ItemElement"] = ItemElement::class.java
        elementClasses["BlockElement"] = BlockElement::class.java
        elementClasses["TextElement"] = TextElement::class.java
    }
}