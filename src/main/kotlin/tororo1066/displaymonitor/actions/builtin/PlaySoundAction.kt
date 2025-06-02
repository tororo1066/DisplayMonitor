package tororo1066.displaymonitor.actions.builtin

import org.bukkit.entity.Player
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "PlaySound",
    description = "指定したサウンドを対象に再生する。"
)
class PlaySoundAction: AbstractAction() {

    @ParameterDoc(
        name = "sound",
        description = "再生するサウンドの名前。例: minecraft:entity.player.levelup"
    )
    var sound: String = ""
    @ParameterDoc(
        name = "volume",
        description = "サウンドの音量。",
        default = "1.0"
    )
    var volume: Float = 1f
    @ParameterDoc(
        name = "pitch",
        description = "サウンドのピッチ。",
        default = "1.0"
    )
    var pitch: Float = 1f
    @ParameterDoc(
        name = "public",
        description = "サウンドが周りのプレイヤーにも聞こえるかどうか。",
        default = "false",
    )
    var public: Boolean = false
    @ParameterDoc(
        name = "targetLocation",
        description = "サウンドを再生する位置を対象の位置にするかどうか。",
        default = "true",
    )
    var targetLocation: Boolean = true

    override fun run(context: IActionContext): ActionResult {
        if (sound.isBlank()) return ActionResult.noParameters("Sound is empty")
        val location = if (targetLocation) {
            context.target?.location ?: return ActionResult.targetRequired()
        } else {
            context.location ?: return ActionResult.noParameters("Location is required when targetLocation is false")
        }
        if (public) {
            val world = location.world ?: return ActionResult.failed("World is null")
            runTask {
                world.playSound(location, sound, volume, pitch)
            }
        } else {
            val target = context.target ?: return ActionResult.targetRequired()
            if (target !is Player) {
                return ActionResult.failed("Target is not a player")
            }
            runTask {
                target.playSound(location, sound, volume, pitch)
            }
        }
        return ActionResult.success()
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        sound = configuration.getString("sound", "")!!
        volume = configuration.getDouble("volume", 1.0).toFloat()
        pitch = configuration.getDouble("pitch", 1.0).toFloat()
        public = configuration.getBoolean("public", false)
        targetLocation = configuration.getBoolean("targetLocation", true)
    }
}