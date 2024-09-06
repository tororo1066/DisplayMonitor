package tororo1066.displaymonitor.element

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Player

class BlockElement(config: ConfigurationSection): DisplayBaseElement(config) {

    var block = Bukkit.createBlockData(config.getString("block") ?: "minecraft:stone")

    override val clazz = BlockDisplay::class.java

    override fun applyEntity(entity: Display) {
        if (entity !is BlockDisplay) return
        entity.block = block
    }

    override fun edit(p: Player, edit: ConfigurationSection) {
        block = Bukkit.createBlockData(edit.getString("block") ?: "minecraft:stone")

        super.edit(p, edit)
    }

}