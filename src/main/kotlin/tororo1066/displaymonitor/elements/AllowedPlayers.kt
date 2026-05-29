package tororo1066.displaymonitor.elements

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.StringList
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.displaymonitorapi.elements.CustomSettable
import java.util.UUID

class AllowedPlayers: CustomSettable {

    @ParameterDoc(
        name = "allowedPlayers",
        description = "許可するプレイヤーのリスト。UUIDまたは名前を指定する。",
        type = StringList::class
    )
    val allowedPlayers: ArrayList<Any> = ArrayList()
    @ParameterDoc(
        name = "disallowedPlayers",
        description = "拒否するプレイヤーのリスト。UUIDまたは名前を指定する。",
        type = StringList::class
    )
    val disallowedPlayers: ArrayList<Any> = ArrayList()

    override fun load(section: IAdvancedConfigurationSection) {
        fun uuidOrName(value: String): Any {
            return try {
                UUID.fromString(value)
            } catch (_: IllegalArgumentException) {
                value
            }
        }

        section.getStringList("allowedPlayers").forEach {
            allowedPlayers.add(uuidOrName(it))
        }

        section.getStringList("disallowedPlayers").forEach {
            disallowedPlayers.add(uuidOrName(it))
        }
    }

    fun isAllowed(player: Player): Boolean {
        fun matches(any: Any): Boolean {
            return when (any) {
                is UUID -> any == player.uniqueId
                is String -> any == player.name
                else -> false
            }
        }

        if (allowedPlayers.isEmpty()) {
            if (disallowedPlayers.isEmpty()) return true
            return !disallowedPlayers.any {
                matches(it)
            }
        }

        return allowedPlayers.any { matches(it) } && !disallowedPlayers.any { matches(it) }
    }

    fun allowedPlayers(): List<Player> {
        return Bukkit.getOnlinePlayers().filter { player ->
            isAllowed(player)
        }
    }

    fun disallowedPlayers(): List<Player> {
        return Bukkit.getOnlinePlayers().filter { player ->
            !isAllowed(player)
        }
    }

    fun allowedPlayersAction(unit: (Player) -> Unit) {
        // tick 等で高頻度に呼ばれるため、中間 List を生成しない
        Bukkit.getOnlinePlayers().forEach { player ->
            if (isAllowed(player)) {
                unit(player)
            }
        }
    }

    fun disallowedPlayersAction(unit: (Player) -> Unit) {
        // tick 等で高頻度に呼ばれるため、中間 List を生成しない
        Bukkit.getOnlinePlayers().forEach { player ->
            if (!isAllowed(player)) {
                unit(player)
            }
        }
    }
}