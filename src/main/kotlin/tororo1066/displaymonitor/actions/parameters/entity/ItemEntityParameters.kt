package tororo1066.displaymonitor.actions.parameters.entity

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Item
import tororo1066.displaymonitor.actions.parameters.AbstractEntityParameters
import tororo1066.tororopluginapi.sItem.SItem

@Suppress("unused")
class ItemEntityParameters: AbstractEntityParameters<Item>() {

    val plain = PlainTextComponentSerializer.plainText()

    override fun getParameters(prefix: String, entity: Item): Map<String, Any> {
        val map = HashMap<String, Any>()

        map["${prefix}.item.base64"] = SItem(entity.itemStack).toByteArrayBase64()
        map["${prefix}.item.type"] = entity.itemStack.type.name
        map["${prefix}.item.amount"] = entity.itemStack.amount
        entity.itemStack.itemMeta?.let {
            if (it.hasDisplayName() && it.displayName() != null) {
                map["${prefix}.item.displayName"] = plain.serialize(it.displayName()!!)
            }
        }
        return map
    }
}