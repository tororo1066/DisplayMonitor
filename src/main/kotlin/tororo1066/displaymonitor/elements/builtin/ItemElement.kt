package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.inventory.ItemStack
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.elements.AbstractElement
import tororo1066.displaymonitorapi.elements.Settable

@ClassDoc(
    name = "ItemElement",
    description = "アイテムを表示するElement。"
)
open class ItemElement: DisplayBaseElement() {

    @ParameterDoc(
        name = "itemStack",
        description = "表示するアイテム。",
        default = "minecraft:stone"
    )
    @Settable var itemStack = ItemStack(Material.STONE)
    @ParameterDoc(
        name = "itemDisplayTransform",
        description = "アイテムの表示方法。",
        default = "FIXED"
    )
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