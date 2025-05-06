package tororo1066.displaymonitorapi.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tororo1066.displaymonitorapi.actions.IActionContext;

import java.util.List;
import java.util.Map;

/**
 * 1つのActionを纏めたクラス
 */
public interface IActionConfiguration {

    /**
     * ActionのKeyを取得する
     * @return ActionのKey
     */
    @NotNull String getKey();

    /**
     * 実行するActionのリストを取得する
     * @return {@link IAdvancedConfigurationSection}のリスト
     */
    @NotNull List<@NotNull IAdvancedConfigurationSection> getActions();

    /**
     * Triggerのマップを取得する
     * @return Triggerのマップ
     */
    @NotNull Map<@NotNull String, @NotNull IAdvancedConfigurationSection> getTriggers();

    /**
     * Actionを実行する
     * @param context {@link IActionContext}
     * @param async 非同期で実行するかどうか
     * @param actionName Actionの名前
     */
    void run(@NotNull IActionContext context, boolean async, @Nullable String actionName);
}
