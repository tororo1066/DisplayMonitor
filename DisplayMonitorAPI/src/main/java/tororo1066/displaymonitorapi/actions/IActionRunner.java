package tororo1066.displaymonitorapi.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection;

import java.util.List;

public interface IActionRunner {

    /**
     * Actionを実行する
     *
     * @param root            Configurationのルート
     * @param actions         実行するActionのリスト
     * @param context         Actionの実行に関する情報 {@link IActionContext}
     * @param actionName
     * @param async           非同期で実行するかどうか
     * @param disableAutoStop 自動停止を無効にするかどうか
     */
    void run(
            @NotNull IAdvancedConfiguration root,
            @NotNull List<IAdvancedConfigurationSection> actions,
            @NotNull IActionContext context,
            @Nullable String actionName,
            @NotNull Boolean async,
            @NotNull Boolean disableAutoStop
    );
}
