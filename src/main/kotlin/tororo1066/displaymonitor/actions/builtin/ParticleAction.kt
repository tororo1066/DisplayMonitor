package tororo1066.displaymonitor.actions.builtin

import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.util.Vector
import tororo1066.displaymonitor.Utils
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.elements.AllowedPlayers
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection

@ClassDoc(
    name = "Particle",
    description = "指定したパーティクルを表示する。",
)
class ParticleAction: AbstractAction() {

    @ParameterDoc(
        name = "particle",
        description = "表示するパーティクルの種類。",
        default = "null"
    )
    var particle: Particle? = null
    @ParameterDoc(
        name = "count",
        description = "表示するパーティクルの数。",
        default = "1"
    )
    var count: Int = 1
    @ParameterDoc(
        name = "offset",
        description = "パーティクルのぶれ。",
        default = "0.0,0.0,0.0"
    )
    var offset: Vector = Vector(0.0, 0.0, 0.0)
    @ParameterDoc(
        name = "extra",
        description = "パーティクルの追加データ。パーティクルによって意味が異なる。",
        default = "0.0"
    )
    var extra: Double = 0.0

    @ParameterDoc(
        name = "dust.color",
        description = "パーティクルの色。hex形式で指定。",
        default = "null"
    )
    var dustColor: Color? = null
    @ParameterDoc(
        name = "dust.size",
        description = "パーティクルの大きさ。",
        default = "1.0"
    )
    var dustSize: Float = 1.0f

    @ParameterDoc(
        name = "radius",
        description = "パーティクルを表示する範囲の半径。0の場合はreceiversの設定が使われる。",
        default = "0"
    )
    var radius: Int = 0
    @ParameterDoc(
        name = "receivers",
        description = "パーティクルを受け取るプレイヤーの設定。radiusが0の場合に使用される。",
    )
    var receivers: AllowedPlayers = AllowedPlayers()

    override fun run(context: IActionContext): ActionResult {
        val location = context.location ?: return ActionResult.noParameters("No location found")
        val particle = particle ?: return ActionResult.noParameters("No particle specified")

        val builder = particle.builder()
            .count(count)
            .offset(offset.x, offset.y, offset.z)
            .extra(extra)
            .location(location)

        if (particle.dataType == Particle.DustOptions::class.java) {
            dustColor?.let {
                val dustOptions = Particle.DustOptions(it, dustSize)
                builder.data(dustOptions)
            }
        }

        if (radius > 0) {
            builder.receivers(radius)
        } else {
            builder.receivers(receivers.allowedPlayers())
        }

        builder.spawn()

        return ActionResult.success()
    }

    override fun prepare(configuration: IAdvancedConfigurationSection) {
        particle = configuration.getEnum("particle", Particle::class.java)
        count = configuration.getInt("count", 1)
        offset = configuration.getBukkitVector("offset", Vector(0.0, 0.0, 0.0))
        extra = configuration.getDouble("extra", 0.0)

        val dustConfig = configuration.getAdvancedConfigurationSection("dust")
        if (dustConfig != null) {
            dustColor = Utils.hexToBukkitColor(dustConfig.getString("color") ?: "#FFFFFF")
            dustSize = dustConfig.getDouble("size", 1.0).toFloat()
        }

        radius = configuration.getInt("radius", 0)
        val receiversConfig = configuration.getAdvancedConfigurationSection("receivers")
        if (receiversConfig != null) {
            receivers = AllowedPlayers().also { it.load(receiversConfig) }
        }
    }
}