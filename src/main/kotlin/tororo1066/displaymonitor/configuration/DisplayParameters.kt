package tororo1066.displaymonitor.configuration

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display.Billboard
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.displaymonitor.Utils.getEnum
import tororo1066.displaymonitor.Utils.getQuaternion
import tororo1066.displaymonitor.Utils.getVector3f

data class DisplayParameters(
    var translation: Vector3f = Vector3f(),
    var scale: Vector3f = Vector3f(1f, 1f, 1f),
    var leftRotation: Quaternionf = Quaternionf(),
    var rightRotation: Quaternionf = Quaternionf(),
    var billboard: Billboard = Billboard.FIXED,
    var interpolationDuration: Int = 0,
    var interpolationDelay: Int = 0,
    var teleportDuration: Int = 0
) {

    fun getTransformation(): Transformation {
        return Transformation(
            translation,
            leftRotation,
            scale,
            rightRotation
        )
    }

    fun edit(edit: ConfigurationSection) {
        translation = edit.getVector3f("translation") ?: translation
        scale = edit.getVector3f("scale") ?: scale
        leftRotation = edit.getQuaternion("leftRotation") ?: leftRotation
        rightRotation = edit.getQuaternion("rightRotation") ?: rightRotation
        billboard = edit.getEnum<Billboard>("billboard") ?: billboard
        interpolationDuration = if (edit.isInt("interpolationDuration")) edit.getInt("interpolationDuration") else interpolationDuration
        interpolationDelay = if (edit.isInt("interpolationDelay")) edit.getInt("interpolationDelay") else interpolationDelay
        teleportDuration = if (edit.isInt("teleportDuration")) edit.getInt("teleportDuration") else teleportDuration
    }

    companion object {
        fun fromConfig(config: ConfigurationSection): DisplayParameters {
            val translation = config.getVector3f("translation") ?: Vector3f()
            val scale = config.getVector3f("scale") ?: Vector3f(1f, 1f, 1f)
            val leftRotation = config.getQuaternion("leftRotation") ?: Quaternionf()
            val rightRotation = config.getQuaternion("rightRotation") ?: Quaternionf()
            val billboard = config.getEnum<Billboard>("billboard") ?: Billboard.FIXED
            val interpolationDuration = config.getInt("interpolationDuration")
            val interpolationDelay = config.getInt("interpolationDelay")
            val teleportDuration = config.getInt("teleportDuration")
            return DisplayParameters(
                translation,
                scale,
                leftRotation,
                rightRotation,
                billboard,
                interpolationDuration,
                interpolationDelay,
                teleportDuration
            )
        }
    }
}