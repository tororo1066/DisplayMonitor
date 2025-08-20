package tororo1066.displaymonitor.elements.builtin

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitorapi.elements.Settable

@ClassDoc(
    name = "TextElement",
    description = "テキストを表示するElement。"
)
open class TextElement: DisplayBaseElement() {

    @ParameterDoc(
        name = "text",
        description = "表示するテキスト。",
        default = "undefined"
    )
    @Settable var text: Component = Component.text("undefined")
    @ParameterDoc(
        name = "backgroundColor",
        description = "背景色。",
        default = "null"
    )
    @Settable var backgroundColor: Color? = null
    @ParameterDoc(
        name = "lineWidth",
        description = "行の幅。",
        default = "200"
    )
    @Settable var lineWidth = 200
    @ParameterDoc(
        name = "seeThrough",
        description = "透過するか。",
        default = "false"
    )
    @Settable var seeThrough = false
    @ParameterDoc(
        name = "defaultBackground",
        description = "デフォルトの背景色を使用するか。",
        default = "false"
    )
    @Settable var defaultBackground = false
    @ParameterDoc(
        name = "shadow",
        description = "影を表示するか。",
        default = "false"
    )
    @Settable var shadow = false
    @ParameterDoc(
        name = "alignment",
        description = "テキストの配置。",
        default = "CENTER"
    )
    @Settable var alignment = TextDisplay.TextAlignment.CENTER
    @ParameterDoc(
        name = "textOpacity",
        description = "テキストの透明度。",
        default = "-1"
    )
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