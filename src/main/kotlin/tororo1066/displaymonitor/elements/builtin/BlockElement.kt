package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Bukkit
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.elements.Settable

@ClassDoc(
    name = "BlockElement",
    description = "ブロックを表示するElement。"
)
class BlockElement: DisplayBaseElement() {

    @ParameterDoc(
        name = "block",
        description = "表示するブロック。",
        default = "minecraft:stone"
    )
    @Settable
    var block: BlockData = Bukkit.createBlockData("minecraft:stone")

    override val clazz = BlockDisplay::class.java

    override fun applyEntity(entity: Display) {
        if (entity !is BlockDisplay) return
        entity.block = block
    }
}