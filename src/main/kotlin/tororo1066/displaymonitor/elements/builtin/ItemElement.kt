package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.inventory.ItemStack
import tororo1066.displaymonitor.configuration.AdvancedConfigurationSection
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitor.elements.Settable

class ItemElement: DisplayBaseElement() {

    @Settable var itemStack = ItemStack(Material.STONE)
    @Settable var itemDisplayTransform = ItemDisplayTransform.FIXED

    override val clazz = ItemDisplay::class.java

    override fun applyEntity(entity: Display) {
        if (entity !is ItemDisplay) return
        entity.itemStack = itemStack
        entity.itemDisplayTransform = itemDisplayTransform
    }

    override fun clone(): AbstractElement {
        val element = super.clone() as ItemElement
        element.itemStack = itemStack.clone()
        return element
    }

}