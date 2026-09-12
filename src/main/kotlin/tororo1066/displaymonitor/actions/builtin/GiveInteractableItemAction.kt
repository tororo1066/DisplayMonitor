package tororo1066.displaymonitor.actions.builtin

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.UseCooldown
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.tororopluginapi.SJavaPlugin
import tororo1066.tororopluginapi.sItem.SInteractItemManager
import java.util.UUID

@ClassDoc(
    name = "GiveInteractableItem",
    description = "対象のプレイヤーにインタラクト可能なアイテムを付与する。"
)
class GiveInteractableItemAction: GiveItemAction() {

    @ParameterDoc(
        name = "interactCooldown",
        description = "アイテムをインタラクトしたときのクールダウン時間。単位はtick。",
        default = "0"
    )
    var interactCooldown = 0
    @ParameterDoc(
        name = "unique",
        description = "trueの場合、同じアイテムを複数持つことができない。",
        default = "false"
    )
    var unique = false
    @ParameterDoc(
        name = "targetOnly",
        description = "trueの場合、対象のプレイヤーのみがアイテムを使用できる。",
        default = "false"
    )
    var targetOnly = false
    @ParameterDoc(
        name = "onInteract",
        description = "アイテムを使用したときのアクション。"
    )
    var onInteract: Execute = Execute.empty()
    @ParameterDoc(
        name = "onDrop",
        description = "アイテムをドロップしたときのアクション。"
    )
    var onDrop: Execute = Execute.empty()
    @ParameterDoc(
        name = "onSwap",
        description = "アイテムをスワップしたときのアクション。"
    )
    var onSwap: Execute = Execute.empty()

    companion object {
        val interactManager = SInteractItemManager(SJavaPlugin.plugin, disableCoolTimeView = true)
        init {
            interactManager.setOnSetCoolDownEvent { cooldown, item ->
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.setCooldown(item.itemStack, cooldown)
                }
            }
        }
    }

    override fun createItemStack(context: IActionContext): ItemStack {
        val baseItem = super.createItemStack(context)
        @Suppress("UnstableApiUsage")
        baseItem.setData(
            DataComponentTypes.USE_COOLDOWN,
            UseCooldown.useCooldown(0.00001f)
                .cooldownGroup(Key.key("displaymonitor", UUID.randomUUID().toString()))
        )

        fun cloneContext(player: Player): IActionContext {
            val cloneContext = context.clone()
            cloneContext.target = player
            return cloneContext
        }

        val interactItem = interactManager.createSInteractItem(baseItem, unique)
            .setInitialCoolDown(interactCooldown)
            .setInteractEvent { e, item ->
                if (context.publicContext.stop) {
                    item.delete()
                    return@setInteractEvent false
                }
                if (targetOnly && e.player != context.target) return@setInteractEvent false
                onInteract(cloneContext(e.player))
                true
            }
            .setDropEvent { e, item ->
                if (context.publicContext.stop) {
                    item.delete()
                    return@setDropEvent
                }
                if (targetOnly && e.player != context.target) return@setDropEvent
                onDrop(cloneContext(e.player))
            }
            .setSwapEvent { e, item ->
                if (context.publicContext.stop) {
                    item.delete()
                    return@setSwapEvent
                }
                if (targetOnly && e.player != context.target) return@setSwapEvent
                onSwap(cloneContext(e.player))
            }

        return interactItem.itemStack
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        super.prepare(configuration)
        interactCooldown = configuration.getInt("interactCooldown", 0)
        unique = configuration.getBoolean("unique", false)
        targetOnly = configuration.getBoolean("targetOnly", false)
        onInteract = configuration.getConfigExecute("onInteract") ?: Execute.empty()
        onDrop = configuration.getConfigExecute("onDrop") ?: Execute.empty()
        onSwap = configuration.getConfigExecute("onSwap") ?: Execute.empty()
    }
}