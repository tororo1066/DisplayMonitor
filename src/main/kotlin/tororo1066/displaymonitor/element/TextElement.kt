package tororo1066.displaymonitor.element

import net.kyori.adventure.text.Component
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import tororo1066.displaymonitor.Utils
import tororo1066.displaymonitor.Utils.getEnum

class TextElement(config: ConfigurationSection): DisplayBaseElement(config) {

    var text = config.getRichMessage("text") ?: Component.text("undefined")
    var backgroundColor = Utils.hexToBukkitColor(config.getString("backgroundColor", "#000000")!!)
    var lineWidth = config.getInt("lineWidth", 200)
    var seeThrough = config.getBoolean("seeThrough", false)
    var defaultBackground = config.getBoolean("defaultBackground", false)
    var shadow = config.getBoolean("shadow", false)
    var alignment = config.getEnum<TextDisplay.TextAlignment>("alignment") ?: TextDisplay.TextAlignment.CENTER
    var textOpacity = config.getInt("textOpacity", -1)

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

    override fun edit(p: Player, edit: ConfigurationSection) {
        text = edit.getRichMessage("text") ?: text
        backgroundColor = edit.getString("backgroundColor")?.let { Utils.hexToBukkitColor(it) } ?: backgroundColor
        lineWidth = edit.getInt("lineWidth", lineWidth)
        seeThrough = edit.getBoolean("seeThrough", seeThrough)
        defaultBackground = edit.getBoolean("defaultBackground", defaultBackground)
        shadow = edit.getBoolean("shadow", shadow)
        alignment = edit.getEnum<TextDisplay.TextAlignment>("alignment") ?: alignment
        textOpacity = edit.getInt("textOpacity", textOpacity)

        super.edit(p, edit)
    }
}