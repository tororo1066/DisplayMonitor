package tororo1066.displaymonitor.elements.builtin

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import tororo1066.displaymonitor.Utils
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitor.elements.Settable

class TextElement: DisplayBaseElement() {

    @Settable var text: Component = Component.text("undefined")
    @Settable var backgroundColor: Color? = null
    @Settable var lineWidth = 200
    @Settable var seeThrough = false
    @Settable var defaultBackground = false
    @Settable var shadow = false
    @Settable var alignment = TextDisplay.TextAlignment.CENTER
    @Settable var textOpacity = -1

    override val clazz = TextDisplay::class.java

    override fun applyEntity(entity: Display) {
        if (entity !is TextDisplay) return
        entity.text(text)
        entity.backgroundColor = backgroundColor
        entity.lineWidth = lineWidth
        entity.isSeeThrough = seeThrough
        entity.isDefaultBackground = defaultBackground
        entity.isShadowed = shadow
        entity.alignment = alignment
        entity.textOpacity = textOpacity.toByte()
    }

    override fun clone(): AbstractElement {
        val element = super.clone() as TextElement
        element.text = text
        element.backgroundColor = backgroundColor
        element.lineWidth = lineWidth
        element.seeThrough = seeThrough
        element.defaultBackground = defaultBackground
        element.shadow = shadow
        element.alignment = alignment
        element.textOpacity = textOpacity
        return element
    }
}