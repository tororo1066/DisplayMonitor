package tororo1066.displaymonitorapi.actions;

import org.jetbrains.annotations.NotNull;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection;

/**
 * Actionを実行するためのクラス
 */
public interface IAbstractAction {
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
