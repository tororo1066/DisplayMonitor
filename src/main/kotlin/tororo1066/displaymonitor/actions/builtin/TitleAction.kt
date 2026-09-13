package tororo1066.displaymonitor.actions.builtin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import tororo1066.displaymonitor.actions.AbstractAction
import tororo1066.displaymonitor.documentation.ClassDoc
import tororo1066.displaymonitor.documentation.ParameterDoc
import tororo1066.displaymonitorapi.actions.ActionResult
import tororo1066.displaymonitorapi.actions.IActionContext
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection
import java.time.Duration

@ClassDoc(
    name = "Title",
    description = "対象にタイトルを送信する。"
)
class TitleAction: AbstractAction() {

    @ParameterDoc(
        name = "title",
        description = "送信するメッセージ。"
    )
    var title: Component? = null
    @ParameterDoc(
        name = "subtitle",
        description = "送信するサブタイトル。"
    )
    var subtitle: Component? = null
    @ParameterDoc(
        name = "fadeIn",
        description = "フェードインの時間。tick単位。",
        default = "0"
    )
    var fadeIn: Int = 0
    @ParameterDoc(
        name = "stay",
        description = "表示時間。tick単位。",
        default = "60"
    )
    var stay: Int = 60
    @ParameterDoc(
        name = "fadeOut",
        description = "フェードアウトの時間。tick単位。",
        default = "0"
    )
    var fadeOut: Int = 0


    override fun run(context: IActionContext): ActionResult {
        val target = context.target ?: return ActionResult.targetRequired()
        val title = title
        val subtitle = subtitle
        if (title == null && subtitle == null) {
            return ActionResult.noParameters("Title and subtitle are both null")
        }
        target.showTitle(Title.title(
            title ?: Component.empty(),
            subtitle ?: Component.empty(),
            Title.Times.times(Duration.ofMillis(fadeIn.toLong() * 50), Duration.ofMillis(stay.toLong() * 50), Duration.ofMillis(fadeOut.toLong() * 50))
        ))

        return ActionResult.success()
    }

    override fun prepare(section: IAdvancedConfigurationSection) {
        title = section.getString("title")?.let { MiniMessage.miniMessage().deserialize(it) }
        subtitle = section.getString("subtitle")?.let { MiniMessage.miniMessage().deserialize(it) }
        fadeIn = section.getInt("fadeIn", 0)
        stay = section.getInt("stay", 60)
        fadeOut = section.getInt("fadeOut", 0)
    }
}