package tororo1066.displaymonitor.elements

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.StringList
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import tororo1066.displaymonitorapi.elements.CustomSettable
import tororo1066.tororopluginapi.otherClass.AnyObject
import java.util.UUID

class AllowedPlayers: CustomSettable {

    @ParameterDoc(
        name = "allowedPlayers",
        description = "許可するプレイヤーのリスト。UUIDまたは名前を指定する。",
        type = StringList::class
    )
    val allowedPlayers: ArrayList<AnyObject> = ArrayList()
    @ParameterDoc(
        name = "disallowedPlayers",
        description = "拒否するプレイヤーのリスト。UUIDまたは名前を指定する。",
        type = StringList::class
    )
    val disallowedPlayers: ArrayList<AnyObject> = ArrayList()

    override fun load(section: IAdvancedConfigurationSection) {
        fun uuidOrName(value: String): AnyObject {
            return try {
                AnyObject(UUID.fromString(value))
            } catch (_: IllegalArgumentException) {
                AnyObject(value)
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
        if (allowedPlayers.isEmpty()) {
            if (disallowedPlayers.isEmpty()) return true
            return !disallowedPlayers.any {
                (it.instanceOf<UUID>() && it.asUUID() == player.uniqueId) ||
                        (it.instanceOf<String>() && it.asString() == player.name)
            }
        }

        return allowedPlayers.any {
            (it.instanceOf<UUID>() && it.asUUID() == player.uniqueId) ||
                    (it.instanceOf<String>() && it.asString() == player.name)
        } && !disallowedPlayers.any {
            (it.instanceOf<UUID>() && it.asUUID() == player.uniqueId) ||
                    (it.instanceOf<String>() && it.asString() == player.name)
        }
    }

    fun allowedPlayersAction(unit: (Player) -> Unit) {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (isAllowed(player)) {
                unit(player)
            }
        }
    }

    fun disallowedPlayersAction(unit: (Player) -> Unit) {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (!isAllowed(player)) {
                unit(player)
            }
        }
    }
}