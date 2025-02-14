package tororo1066.displaymonitor.configuration

import org.bukkit.entity.Display
import org.bukkit.entity.Display.Billboard
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.elements.Settable

data class DisplayParameters(
    @ParameterDoc(
        name = "translation",
        description = "オフセット",
        type = ParameterType.Vector3f
    )
    @Settable
    var translation: Vector3f = Vector3f(),

    @ParameterDoc(
        name = "scale",
        description = "大きさ",
        type = ParameterType.Vector3f
    )
    @Settable
    var scale: Vector3f = Vector3f(1f, 1f, 1f),

    @ParameterDoc(
        name = "leftRotation",
        description = "左回転",
        type = ParameterType.Rotation
    )
    @Settable
    var leftRotation: Quaternionf = Quaternionf(),

    @ParameterDoc(
        name = "rightRotation",
        description = "右回転",
        type = ParameterType.Rotation
    )
    @Settable
    var rightRotation: Quaternionf = Quaternionf(),

    @ParameterDoc(
        name = "billboard",
        description = "ビルボード",
        type = ParameterType.Billboard
    )
    @Settable var billboard: Billboard = Billboard.FIXED,

    @ParameterDoc(
        name = "interpolationDuration",
        description = "補間時間",
        type = ParameterType.Int
    )
    @Settable
    var interpolationDuration: Int = 0,

    @ParameterDoc(
        name = "interpolationDelay",
        description = "補間遅延",
        type = ParameterType.Int
    )
    @Settable
    var interpolationDelay: Int = 0,

    @ParameterDoc(
        name = "teleportDuration",
        description = "テレポート時間",
        type = ParameterType.Int
    )
    @Settable
    var teleportDuration: Int = 0,

    @ParameterDoc(
        name = "shadowRadius",
        description = "影の半径",
        type = ParameterType.Float
    )
    @Settable
    var shadowRadius: Float = 0f,

    @ParameterDoc(
        name = "shadowStrength",
        description = "影の強さ",
        type = ParameterType.Float
    )
    @Settable
    var shadowStrength: Float = 1f,

    @ParameterDoc(
        name = "brightness",
        description = "明るさ",
        type = ParameterType.Brightness
    )
    @Settable
    var brightness: Display.Brightness? = null,
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