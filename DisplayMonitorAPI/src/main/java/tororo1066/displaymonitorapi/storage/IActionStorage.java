package tororo1066.displaymonitorapi.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tororo1066.displaymonitorapi.actions.IAbstractAction;
import tororo1066.displaymonitorapi.actions.IActionContext;
import tororo1066.displaymonitorapi.actions.IPublicActionContext;
import tororo1066.displaymonitorapi.configuration.IActionConfiguration;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public interface IActionStorage {

    /**
     * Actionを登録する
     * @param name Actionの名前
     * @param action Actionのクラス
     */
    void registerAction(@NotNull String name, @NotNull Class<? extends @NotNull IAbstractAction> action);

    /**
     * Actionを登録する
     * @param action Actionのクラス
     */
    default void registerAction(@NotNull Class<? extends @NotNull IAbstractAction> action) {
        registerAction(action.getSimpleName(), action);
    }

    /**
     * Actionを取得する
     * @param key Actionのキー
     * @param section {@link IAdvancedConfigurationSection}
     * @return {@link IActionConfiguration}
     */
    @NotNull IActionConfiguration getActionConfiguration(@NotNull String key, @NotNull IAdvancedConfigurationSection section);

    /**
     * ActionをIAdvancedConfigurationから取得する
     * @param configuration {@link IAdvancedConfiguration}
     * @return {@link IActionConfiguration}のリスト
     */
    @NotNull List<@NotNull IActionConfiguration> getActionConfigurations(@NotNull IAdvancedConfiguration configuration);

    /**
     * Actionをファイルから取得する
     * @param file ファイル
     * @return {@link IActionConfiguration}のリスト
     */
    @NotNull List<@NotNull IActionConfiguration> getActionConfigurations(@NotNull File file);



    /**
     * 空の{@link IPublicActionContext}を作成する
     * @return {@link IPublicActionContext}
     */
    @NotNull IPublicActionContext createPublicContext();

    /**
     * {@link IPublicActionContext}から{@link IActionContext}を作成する
     * @param publicContext {@link IPublicActionContext}
     * @return {@link IActionContext}
     */
    @NotNull IActionContext createActionContext(@NotNull IPublicActionContext publicContext);

    /**
     * Triggerを発生させる
     * @param name Triggerの名前
     * @param context {@link IActionContext}
     * @param condition 条件
     */
    void trigger(@NotNull String name, @NotNull IActionContext context, @Nullable Function<@NotNull IAdvancedConfigurationSection, @NotNull Boolean> condition);

    /**
     * Triggerを発生させる
     * @param storedActions 対象にしたいActionのリスト
     * @param name Triggerの名前
     * @param context {@link IActionContext}
     * @param condition 条件
     */
    void trigger(@NotNull List<@NotNull IActionConfiguration> storedActions, @NotNull String name, @NotNull IActionContext context, @Nullable Function<@NotNull IAdvancedConfigurationSection, @NotNull Boolean> condition);
}
