package tororo1066.displaymonitor.elements.builtin

import org.bukkit.Bukkit
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.elements.Settable

@ClassDoc(
    name = "BlockElement",
    description = "ブロックを表示するElement。"
)
open class BlockElement: DisplayBaseElement<BlockDisplay>() {

    @ParameterDoc(
        name = "block",
        description = "表示するブロック。",
        default = "minecraft:stone"
    )
    @Settable
    var block: BlockData = Bukkit.createBlockData("minecraft:stone")

    override val clazz = BlockDisplay::class.java

    override fun applyEntity(entity: BlockDisplay) {
        entity.block = block
    }
}