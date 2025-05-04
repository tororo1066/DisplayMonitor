package tororo1066.displaymonitorapi.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tororo1066.displaymonitorapi.actions.IActionContext;

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
     * Actionを実行する
     * @param context {@link IActionContext}
     * @param async 非同期で実行するかどうか
     * @param actionName Actionの名前
     */
    void run(@NotNull IActionContext context, boolean async, @Nullable String actionName);
}
