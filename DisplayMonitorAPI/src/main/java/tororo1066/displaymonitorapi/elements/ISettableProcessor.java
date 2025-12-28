package tororo1066.displaymonitorapi.elements;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 変数や値を処理するためのインターフェース
 */
public interface ISettableProcessor {

    /**
     * カスタム変数処理一覧を取得する<br>
     * 例
     * <pre>{@code
     * getCustomVariableProcessors().put(Player.class, player -> {
     *    Map<String, Object> map = new HashMap<>();
     *    map.put("name", player.getName());
     *    map.put("uuid", player.getUniqueId().toString());
     *    return map;
     * });
     * }</pre>
     */
    @NotNull Map<Class<?>, Function<Object, Map<String, Object>>> getCustomVariableProcessors();

    /**
     * カスタム値処理一覧を取得する<br>
     * 例
     * <pre>{@code
     * getCustomValueProcessors().put(Player.class, (configuration, key) -> {
     *    return Bukkit.getPlayer(UUID.fromString(configuration.getString(key)));
     * });
     * }</pre>
     */
    @NotNull Map<Class<?>, BiFunction<IAdvancedConfigurationSection, String, Object>> getCustomValueProcessors();

    /**
     * 変数を処理する
     * @param value 変数
     * @return 処理後の変数
     */
    @NotNull Map<String, Object> processVariable(@Nullable Object value);

    /**
     * 値を処理する
     * @param configuration 設定
     * @param key キー
     * @param clazz クラス
     * @return 処理後の値
     */
    @Nullable <T extends @NotNull Object> T processValue(@NotNull IAdvancedConfigurationSection configuration, @NotNull String key, @NotNull Class<T> clazz);

    @NotNull List<Field> getSettableFields(@NotNull Class<?> clazz);

    void prepareSettableFields(@NotNull IAdvancedConfigurationSection section, @NotNull Object instance);
}
