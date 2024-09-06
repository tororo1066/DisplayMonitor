package tororo1066.displaymonitor.element

import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import tororo1066.displaymonitor.Utils.getEnum
import tororo1066.displaymonitor.Utils.getStringItemStack

class ItemElement(config: ConfigurationSection): DisplayBaseElement(config) {

    var itemStack = config.getStringItemStack("itemStack") ?: ItemStack(Material.STONE)
    var itemDisplayTransform = config.getEnum<ItemDisplayTransform>("itemDisplayTransform") ?: ItemDisplayTransform.FIXED

    override val clazz = ItemDisplay::class.java

    override fun applyEntity(entity: Display) {
        if (entity !is ItemDisplay) return
        entity.itemStack = itemStack
        entity.itemDisplayTransform = itemDisplayTransform
    }

    override fun edit(p: Player, edit: ConfigurationSection) {
        itemStack = edit.getStringItemStack("itemStack") ?: itemStack
        itemDisplayTransform = edit.getEnum<ItemDisplayTransform>("itemDisplayTransform") ?: itemDisplayTransform

        super.edit(p, edit)
    }

}