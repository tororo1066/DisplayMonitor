package tororo1066.displaymonitorapi.configuration;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 拡張されたConfigurationSection
 */
public interface IAdvancedConfigurationSection extends ConfigurationSection {

    /**
     * 指定されたパスのAdvancedConfigurationSectionを取得する
     * @param path パス
     * @return {@link IAdvancedConfigurationSection}
     */
    @Nullable IAdvancedConfigurationSection getAdvancedConfigurationSection(@NotNull String path);

    /**
     * 指定されたパスのAdvancedConfigurationSectionのリストを取得する
     * @param path パス
     * @return {@link IAdvancedConfigurationSection}のリスト
     */
    @NotNull List<IAdvancedConfigurationSection> getAdvancedConfigurationSectionList(@NotNull String path);

    /**
     * BukkitのVectorを<code>x,y,z</code>の形式で取得する
     * @param path パス
     * @return {@link Vector}
     */
    @Nullable Vector getBukkitVector(@NotNull String path);

    /**
     * BukkitのVectorを<code>x,y,z</code>の形式で取得する
     * @param path パス
     * @param def デフォルト値
     * @return {@link Vector}
     */
    @NotNull Vector getBukkitVector(@NotNull String path, @NotNull Vector def);

    /**
     * BukkitのLocationを取得する<br>
     * <code>
     *     world,x,y,z<br>
     *     world,x,y,z,yaw,pitch<br>
     *     x,y,z<br>
     *     x,y,z,yaw,pitch<br>
     * </code>
     * のいずれかの形式で取得する
     * @param path パス
     * @return {@link Location}
     */
    @Nullable Location getStringLocation(@NotNull String path);


    /**
     * BukkitのLocationを取得する<br>
     * <code>
     *     world,x,y,z<br>
     *     world,x,y,z,yaw,pitch<br>
     *     x,y,z<br>
     *     x,y,z,yaw,pitch<br>
     * </code>
     * のいずれかの形式で取得する
     * @param path パス
     * @param def デフォルト値
     * @return {@link Location}
     */
    @NotNull Location getStringLocation(@NotNull String path, @NotNull Location def);

    /**
     * JOMLのVector3fを取得する
     * @param path パス
     * @return {@link Vector3f}
     */
    @Nullable Vector3f getVector3f(@NotNull String path);

    /**
     * JOMLのVector3fを取得する
     * @param path パス
     * @param def デフォルト値
     * @return {@link Vector3f}
     */
    @NotNull Vector3f getVector3f(@NotNull String path, @NotNull Vector3f def);

    /**
     * Enumを取得する
     * @param path パス
     * @param clazz Enumのクラス
     * @return Enum
     * @param <T> Enumの型
     */
    @Nullable <T extends @NotNull Enum<T>> T getEnum(@NotNull String path, @NotNull Class<T> clazz);

    /**
     * Enumを取得する
     * @param path パス
     * @param clazz Enumのクラス
     * @param def デフォルト値
     * @return Enum
     * @param <T> Enumの型
     */
    @NotNull <T extends @NotNull Enum<T>> T getEnum(@NotNull String path, @NotNull Class<T> clazz, @NotNull T def);

    /**
     * ItemStackを取得する<br>
     * <code>
     *     minecraft:stone{CustomModelData:1,display:{Name:"{\"text\":\"test\"}"}}<br>
     * </code>
     * の形式で取得する
     * @param path パス
     * @return {@link ItemStack}
     */
    @Nullable ItemStack getStringItemStack(@NotNull String path);

    /**
     * ItemStackを取得する<br>
     * <code>
     *     minecraft:stone{CustomModelData:1,display:{Name:"{\"text\":\"test\"}"}}<br>
     * </code>
     * の形式で取得する
     * @param path パス
     * @param def デフォルト値
     * @return {@link ItemStack}
     */
    @NotNull ItemStack getStringItemStack(@NotNull String path, @NotNull ItemStack def);

    /**
     * Executeを取得する
     * @param path パス
     * @return {@link Execute}
     */
    @Nullable Execute getConfigExecute(@NotNull String path);

    /**
     * Executeを取得する
     * @param path パス
     * @param def デフォルト値
     * @return {@link Execute}
     */
    @NotNull Execute getConfigExecute(@NotNull String path, @NotNull Execute def);

    /**
     * AsyncExecuteを取得する
     * @param path パス
     * @return {@link AsyncExecute}
     */
    @Nullable AsyncExecute getAsyncConfigExecute(@NotNull String path);

    /**
     * AsyncExecuteを取得する
     * @param path パス
     * @param def デフォルト値
     * @return {@link AsyncExecute}
     */
    @NotNull AsyncExecute getAsyncConfigExecute(@NotNull String path, @NotNull AsyncExecute def);

    /**
     * JOMLのQuaternionfを取得する<br>
     * <code>
     *     x,y,z,w<br>
     *     "eular",x,y,z<br>
     *     "axis",angle,x,y,z<br>
     * </code>
     * @param path パス
     * @return {@link Quaternionf}
     */
    @Nullable Quaternionf getRotation(@NotNull String path);

    /**
     * JOMLのQuaternionfを取得する<br>
     * <code>
     *     x,y,z,w<br>
     *     "eular",x,y,z<br>
     *     "axis",angle,x,y,z<br>
     * </code>
     * @param path パス
     * @param def デフォルト値
     * @return {@link Quaternionf}
     */
    @NotNull Quaternionf getRotation(@NotNull String path, @NotNull Quaternionf def);

    /**
     * 一時的なパラメータを設定して処理を行う
     * @param parameters パラメータ
     * @param function 処理
     * @param <T> 戻り値の型
     * @return 戻り値
     */
    @Nullable <T extends @NotNull Object>  T withParameters(@NotNull Map<String, Object> parameters, @NotNull Function<IAdvancedConfigurationSection, @Nullable T> function);

    @NotNull Map<@NotNull String, @Nullable Object> getEvaluatedValues(boolean deep);
}
