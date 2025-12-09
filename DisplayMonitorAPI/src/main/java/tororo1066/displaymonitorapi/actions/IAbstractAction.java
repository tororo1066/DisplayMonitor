package tororo1066.displaymonitorapi.actions;

import org.jetbrains.annotations.NotNull;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection;

/**
 * Actionを実行するためのクラス
 */
public interface IAbstractAction {

    /**
     * 処理の完了時自動的に停止(削除)するかどうか
     * @return true: 自動停止する, false: 自動停止しない
     */
    default boolean allowedAutoStop() {
        return true;
    }

    /**
     * Actionの実行時に呼び出される関数
     * @param context Actionの実行に関する情報 {@link IActionContext}
     * @return Actionの実行結果 {@link ActionResult}
     */
    @NotNull ActionResult run(@NotNull IActionContext context);

    /**
     * Actionの実行前に呼び出される関数
     * @param configuration このクラスのConfigurationSection {@link IAdvancedConfigurationSection}
     */
    void prepare(@NotNull IAdvancedConfigurationSection configuration);
}
