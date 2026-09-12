package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "GiveItem",
    description = "対象のプレイヤーにアイテムを付与する。"
)
open class GiveItemAction: AbstractAction() {

    @ParameterDoc(
        name = "itemStack",
        description = "付与するアイテム。"
    )
    var itemStack = ""
    @ParameterDoc(
        name = "slot",
        description = "付与するスロット、もしくは装備スロット。 指定されていない場合は空いているスロットに追加される。",
    )
    var slot: EquipmentSlot? = null
    var slotIndex: Int? = null
    @ParameterDoc(
        name = "setForce",
        description = "trueの場合、指定されたスロットに強制的にアイテムをセットする。falseの場合、既にアイテムがある場合は追加されない。",
        default = "false"
    )
    var setForce = false
    @ParameterDoc(
        name = "dropIfFull",
        description = "trueの場合、アイテムが付与できない場合にアイテムをドロップする。falseの場合、アイテムが付与できない場合は失敗する。",
        default = "false"
    )
    var dropIfFull = false

    override fun run(context: IActionContext): ActionResult {
        val target = context.target as? Player ?: return ActionResult.targetRequired()
        val item = try {
            createItemStack(context)
        } catch (e: Exception) {
            return ActionResult.failed("Failed to create item stack: ${e.message}")
        }

        return item.setToInventory(target)
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        itemStack = configuration.getString("itemStack") ?: ""
        slot = configuration.getEnum("slot", EquipmentSlot::class.java)
        slotIndex = if (configuration.isInt("slot")) configuration.getInt("slot") else null
        setForce = configuration.getBoolean("setForce", false)
        dropIfFull = configuration.getBoolean("dropIfFull", false)
    }

    open fun createItemStack(context: IActionContext): ItemStack {
        return Bukkit.getItemFactory().createItemStack(itemStack)
    }

    private fun ItemStack.setToInventory(
        player: Player
    ): ActionResult {
        if (isEmpty) return ActionResult.noParameters("ItemStack is empty")
        val inventory = player.inventory
        val isSet = slot != null || slotIndex != null
        val currentItem = slot?.let { inventory.getItem(it) } ?: slotIndex?.let { inventory.getItem(it) }
        val maxStackSize = minOf(this.maxStackSize, inventory.maxStackSize)

        val inserted = if (isSet) {
            if (setForce) {
                val newItem = this.clone()
                newItem.amount = minOf(this.amount, maxStackSize)
                slot?.let { inventory.setItem(it, newItem) } ?: slotIndex?.let { inventory.setItem(it, newItem) }
                newItem.amount
            } else {
                if (currentItem == null || currentItem.isEmpty) {
                    val amountToAdd = minOf(this.amount, maxStackSize)
                    val newItem = this.clone()
                    newItem.amount = amountToAdd
                    slot?.let { inventory.setItem(it, newItem) } ?: slotIndex?.let { inventory.setItem(it, newItem) }
                    amountToAdd
                } else if (currentItem.isSimilar(this)) {
                    val amountToAdd = minOf(this.amount, (maxStackSize - currentItem.amount).coerceAtLeast(0))
                    currentItem.amount += amountToAdd
                    amountToAdd
                } else {
                    0
                }
            }
        } else {
            val leftovers = inventory.addItem(this.clone()).values
            val remaining = leftovers.reduceOrNull { total, stack ->
                total.amount += stack.amount
                total
            }

            this.amount - (remaining?.amount ?: leftovers.sumOf { it.amount })
        }

        val remainingAmount = this.amount - inserted
        if (remainingAmount > 0 && dropIfFull) {
            val dropItem = this.clone()
            dropItem.amount = remainingAmount
            player.world.dropItem(player.location, dropItem).apply {
                pickupDelay = 0
                owner = player.uniqueId
                setCanMobPickup(false)
            }
        }
        return ActionResult.success()
    }
}