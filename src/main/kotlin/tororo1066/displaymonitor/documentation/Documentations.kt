package tororo1066.displaymonitor.documentation

@Target(AnnotationTarget.CLASS)
annotation class ClassDoc(
    val name: String,
    val description: String,
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParameterDoc(
    val name: String,
    val description: String,
    val type: ParameterType,
)

@Target(AnnotationTarget.PROPERTY)
annotation class ParameterTypeDoc(
    val name: String,
)

enum class ParameterType {
    @ParameterTypeDoc("文字列")
    String {
        override val example = "\"example\""
    },
    @ParameterTypeDoc("文字列のリスト")
    StringList {
        override val example = """
            - example1
            - example2
        """.trimIndent()
    },
    @ParameterTypeDoc("真偽値")
    Boolean {
        override val example = "true"
    },
    @ParameterTypeDoc("整数")
    Int {
        override val example = "1000"
    },
    @ParameterTypeDoc("整数")
    Long {
        override val example = "1000"
    },
    @ParameterTypeDoc("装飾可能な文字列")
    Component {
        override val example = "\"<red>Hello!</red>\""
    },
    @ParameterTypeDoc("位置")
    Location {
        override val example = """
            <world>,<x>,<y>,<z>
            <world>,<x>,<y>,<z>,<yaw>,<pitch>
            <x>,<y>,<z>
            <x>,<y>,<z>,<yaw>,<pitch>
        """.trimIndent()
    },
    @ParameterTypeDoc("ベクトル")
    Vector {
        override val example = """
            <x>,<y>,<z>
        """.trimIndent()
    },
    @ParameterTypeDoc("Actionのリスト")
    Actions {
        override val example = """
            - class: Message
              message: "Hello!"
        """.trimIndent()
    },
    @ParameterTypeDoc("セクション")
    AdvancedConfigurationSection {
        override val example = """
            key: value
        """.trimIndent()
    };

    abstract val example: kotlin.String
}