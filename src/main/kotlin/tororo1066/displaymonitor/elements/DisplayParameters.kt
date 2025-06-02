package tororo1066.displaymonitor.elements

import org.bukkit.entity.Display
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitor.documentation.ParameterType
import tororo1066.displaymonitorapi.elements.Settable

data class DisplayParameters(
    @ParameterDoc(
        name = "translation",
        description = "オフセット"
    )
    @Settable
    var translation: Vector3f = Vector3f(),

    @ParameterDoc(
        name = "scale",
        description = "大きさ"
    )
    @Settable
    var scale: Vector3f = Vector3f(1f, 1f, 1f),

    @ParameterDoc(
        name = "leftRotation",
        description = "左回転"
    )
    @Settable
    var leftRotation: Quaternionf = Quaternionf(),

    @ParameterDoc(
        name = "rightRotation",
        description = "右回転"
    )
    @Settable
    var rightRotation: Quaternionf = Quaternionf(),

    @ParameterDoc(
        name = "billboard",
        description = "ビルボード"
    )
    @Settable var billboard: Display.Billboard = Display.Billboard.FIXED,

    @ParameterDoc(
        name = "interpolationDuration",
        description = "補間時間"
    )
    @Settable
    var interpolationDuration: Int = 0,

    @ParameterDoc(
        name = "interpolationDelay",
        description = "補間遅延"
    )
    @Settable
    var interpolationDelay: Int = 0,

    @ParameterDoc(
        name = "teleportDuration",
        description = "テレポート時間"
    )
    @Settable
    var teleportDuration: Int = 0,

    @ParameterDoc(
        name = "shadowRadius",
        description = "影の半径"
    )
    @Settable
    var shadowRadius: Float = 0f,

    @ParameterDoc(
        name = "shadowStrength",
        description = "影の強さ"
    )
    @Settable
    var shadowStrength: Float = 1f,

    @ParameterDoc(
        name = "brightness",
        description = "明るさ"
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