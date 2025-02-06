package tororo1066.displaymonitorapi.actions;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Actionの実行に関する情報を保持するクラス
 */
public interface IActionContext extends Cloneable {

    /**
     * PublicActionContextを取得する
     *
     * @return {@link IPublicActionContext}
     */
    @NotNull IPublicActionContext getPublicContext();

    /**
     * contextに紐づいているグループのUUIDを取得する
     * @return グループのUUID
     */
    @NotNull UUID getGroupUUID();

    /**
     * contextに紐づいているグループのUUIDを設定する<br>
     * 基本的には変更しないべき
     * @param groupUUID グループのUUID
     */
    void setGroupUUID(@NotNull UUID groupUUID);

    /**
     * contextのUUIDを取得する
     * @return contextのUUID
     */
    @NotNull UUID getUUID();

    /**
     * contextのUUIDを設定する<br>
     * 基本的には変更しないべき
     * @param uuid contextのUUID
     */
    void setUUID(@NotNull UUID uuid);

    /**
     * 実行情報が格納されている{@link IAdvancedConfiguration}を取得する
     */
    @Nullable IAdvancedConfiguration getConfiguration();

    /**
     * 実行情報が格納されている{@link IAdvancedConfiguration}を設定する<br>
     * 基本的には変更しないべき
     * @param configuration データ
     */
    void setConfiguration(@Nullable IAdvancedConfiguration configuration);

    /**
     * Actionの実行者を取得する
     * @return 実行者
     */
    @Nullable
    Entity getCaster();

    /**
     * Actionの実行者を設定する
     * @param caster 実行者
     */
    void setCaster(@Nullable Entity caster);

    /**
     * Actionの対象を取得する
     * @return 対象
     */
    @Nullable
    Entity getTarget();

    /**
     * Actionの対象を設定する
     * @param target 対象
     */
    void setTarget(@Nullable Entity target);

    /**
     * Actionのターゲット位置を取得する<br>
     * 必ずしも対象の位置とは限らない
     */
    @Nullable Location getLocation();

    /**
     * Actionのターゲット位置を設定する
     * @param location 位置
     */
    void setLocation(@Nullable Location location);

    /**
     * Actionの実行前に挿入されるパラメータを取得する<br>
     * 実行中に変更しても反映されない
     * @return パラメータ
     */
    @NotNull HashMap<String, Object> getPrepareParameters();

    /**
     * 次のAction実行前に停止するかどうかを取得する
     * @return 停止するかどうか
     */
    boolean getStop();

    /**
     * 次のAction実行前に停止するかどうかを設定する
     * @param stop 停止するかどうか
     */
    void setStop(@NotNull Boolean stop);

    /**
     * 新しいGroupUUIDとUUIDを持つIActionContextを複製する
     * @return 複製されたIActionContext
     */
    @NotNull IActionContext cloneWithRandomUUID();

    /**
     * デフォルトのパラメータを取得する
     * @return パラメータ
     */
    @NotNull Map<String, Object> getDefaultParameters();

    /**
     * IActionContextを複製する
     * @return 複製されたIActionContext
     */
    @NotNull IActionContext clone();
}
