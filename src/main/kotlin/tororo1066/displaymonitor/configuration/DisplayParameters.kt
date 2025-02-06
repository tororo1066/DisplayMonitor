package tororo1066.displaymonitor.configuration

import org.bukkit.entity.Display
import org.bukkit.entity.Display.Billboard
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.displaymonitorapi.elements.Settable

data class DisplayParameters(
    @Settable var translation: Vector3f = Vector3f(),
    @Settable var scale: Vector3f = Vector3f(1f, 1f, 1f),
    @Settable var leftRotation: Quaternionf = Quaternionf(),
    @Settable var rightRotation: Quaternionf = Quaternionf(),
    @Settable var billboard: Billboard = Billboard.FIXED,
    @Settable var interpolationDuration: Int = 0,
    @Settable var interpolationDelay: Int = 0,
    @Settable var teleportDuration: Int = 0,
    @Settable var shadowRadius: Float = 0f,
    @Settable var shadowStrength: Float = 1f,
    @Settable var brightness: Display.Brightness? = null,
) {

    fun getTransformation(): Transformation {
        return Transformation(
            translation,
            leftRotation,
            scale,
            rightRotation
        )
    }
}