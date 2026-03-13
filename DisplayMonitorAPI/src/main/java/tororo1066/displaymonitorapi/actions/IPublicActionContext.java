package tororo1066.displaymonitorapi.actions;

import org.jetbrains.annotations.NotNull;
import tororo1066.displaymonitorapi.elements.IAbstractElement;
import tororo1066.displaymonitorapi.workspace.IAbstractWorkspace;

import java.util.Map;

/**
 * Actionの実行に関する情報を保持するクラス
 * 実行中の全ての{@link IAbstractAction}からアクセスできる
 */
public interface IPublicActionContext {

    /**
     * {@link IAbstractElement}のMapを取得する
     *
     * @return {@link IAbstractElement}のMap
     */
    @NotNull
    Map<@NotNull String, @NotNull IAbstractElement> getElements();

    /**
     * 次のAction実行前に停止するかどうかを取得する
     * @return 停止するかどうか
     */
    boolean getStop();

    /**
     * 次のAction実行前に停止するかどうかを設定する
     * @param stop 停止するかどうか
     */
    void setStop(boolean stop);

    @NotNull Map<@NotNull String, @NotNull Object> getParameters();

    void setParameters(@NotNull Map<@NotNull String, @NotNull Object> parameters);

    @NotNull IAbstractWorkspace getWorkspace();

    void setWorkspace(@NotNull IAbstractWorkspace workspace);

    @NotNull Map<@NotNull String, @NotNull IAbstractElement> getAllElements();

    @NotNull IPublicActionContext shallowCopy();
}
