package tororo1066.displaymonitorapi.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection;
import tororo1066.displaymonitorapi.elements.IAbstractElement;

import java.io.File;
import java.util.Map;

public interface IElementStorage {

    /**
     * Elementを登録する<br>
     * {@link tororo1066.displaymonitorapi.events.ElementRegisteringEvent}で使用する
     * @param key キー
     * @param element Elementのクラス
     */
    void registerElement(@NotNull String key, @NotNull Class<? extends @NotNull IAbstractElement> element);

    /**
     * Elementをディレクトリから読み込む
     * @param directory ディレクトリ
     */
    void loadElements(@NotNull File directory);

    /**
     * Elementを読み込む
     * @param configuration 読み込むデータ
     */
    void loadElement(@NotNull IAdvancedConfigurationSection configuration);

    /**
     * 新規にElementを作成する
     * @param presetName プリセット名
     * @param clazz Elementのクラス - presetNameがnullの場合は必須
     * @param overrideParameters 上書きするパラメータ
     * @param context {@link tororo1066.displaymonitorapi.actions.IActionContext}
     * @return 作成したElement
     */
    @Nullable IAbstractElement createElement(@Nullable String presetName, @Nullable String clazz, @Nullable IAdvancedConfigurationSection overrideParameters, @NotNull String context);

    /**
     * 変数を参照しながら新規にElementを作成する
     *
     * @param presetName プリセット名
     * @param clazz Elementのクラス - presetNameがnullの場合は必須
     * @param overrideParameters 上書きするパラメータ
     * @param variables プリセット設定の評価に使用する変数
     * @param context 呼び出し元のコンテキスト
     * @return 作成したElement
     */
    @Nullable
    default IAbstractElement createElement(
            @Nullable String presetName,
            @Nullable String clazz,
            @Nullable IAdvancedConfigurationSection overrideParameters,
            @NotNull Map<String, Object> variables,
            @NotNull String context
    ) {
        return createElement(presetName, clazz, overrideParameters, context);
    }
}
