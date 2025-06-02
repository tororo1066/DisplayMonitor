package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.util.Vector
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.Execute
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import java.util.UUID

@ClassDoc(
    name = "Target",
    description = "データを変更してアクションを実行する。"
)
class TargetAction: AbstractAction() {

    @ParameterDoc(
        name = "target",
        description = "変更する対象。プレイヤーのUUIDまたは名前を指定する。"
    )
    var target: String? = null
    @ParameterDoc(
        name = "location",
        description = "実行する位置。"
    )
    var location: Location? = null
    @ParameterDoc(
        name = "offset",
        description = "位置のオフセット。"
    )
    var offset: Vector? = null
    @ParameterDoc(
        name = "relativeOffset",
        description = "位置の相対オフセット。"
    )
    var relativeOffset: Vector? = null
    @ParameterDoc(
        name = "actions",
        description = "実行するアクションのリスト。"
    )
    var actions: Execute = Execute.empty()

    override fun run(context: IActionContext): ActionResult {

        val cloneContext = context.clone()

        target?.let {
            try {
                Bukkit.getPlayer(UUID.fromString(it))?.let { player ->
                    cloneContext.target = player
                }
            } catch (_: IllegalArgumentException) {
                Bukkit.getPlayer(it)?.let { player -> cloneContext.target = player }
            }
        }

        location?.let {
            val contextLocation = cloneContext.location
            if (contextLocation != null && it.world == null) {
                it.world = contextLocation.world
            }
            cloneContext.location = it
        }

        cloneContext.location?.let {
            offset?.let { offset ->
                it.add(offset)
            }

            relativeOffset?.let { relativeOffset ->
                val direction = it.direction
                it.add(direction.clone().rotateAroundY(Math.toRadians(90.0)).multiply(relativeOffset.x))
                it.add(direction.clone().multiply(relativeOffset.z))
                it.add(Vector(0.0, relativeOffset.y, 0.0))
            }
        }

        actions(cloneContext)

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        target = section.getString("target")
        location = section.getStringLocation("location")
        offset = section.getBukkitVector("offset")
        relativeOffset = section.getBukkitVector("relativeOffset")
        actions = section.getConfigExecute("actions", actions)
    }
}