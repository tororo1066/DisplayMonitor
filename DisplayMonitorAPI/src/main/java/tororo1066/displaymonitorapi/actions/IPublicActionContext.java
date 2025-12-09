package tororo1066.displaymonitorapi.actions;

import org.jetbrains.annotations.NotNull;
import tororo1066.displaymonitorapi.elements.IAbstractElement;
import tororo1066.displaymonitorapi.workspace.IAbstractWorkspace;

import java.util.HashMap;
import java.util.Map;

/**
 * Actionの実行に関する情報を保持するクラス
 * 実行中の全ての{@link IAbstractAction}からアクセスできる
 */
public interface IPublicActionContext {

    /**
     * {@link IAbstractElement}のHashMapを取得する
     * @return {@link IAbstractElement}のHashMap
     */
    @NotNull
    HashMap<@NotNull String, @NotNull IAbstractElement> getElements();

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

    /**
     * 全てのAction実行後、自動的に停止(削除)するかどうかを取得する
     * @return 自動的に停止(削除)するかどうか
     */
    boolean getShouldAutoStop();

    /**
     * 全てのAction実行後、自動的に停止(削除)するかどうかを設定する
     * @param shouldStop 自動的に停止(削除)するかどうか
     */
    void setShouldAutoStop(@NotNull Boolean shouldStop);

    @NotNull Map<@NotNull String, @NotNull Object> getParameters();

    void setParameters(@NotNull Map<@NotNull String, @NotNull Object> parameters);

    @NotNull IAbstractWorkspace getWorkspace();

    void setWorkspace(@NotNull IAbstractWorkspace workspace);

    @NotNull IPublicActionContext shallowCopy();

    private @NotNull Map<@NotNull String, @NotNull IAbstractElement> getChildElements(@NotNull IAbstractElement element, @NotNull String parentKey) {
        Map<@NotNull String, @NotNull IAbstractElement> allElements = new HashMap<>();
        if (element.hasChildren()) {
            Map<@NotNull String, @NotNull IAbstractElement> children = new HashMap<>(element.getChildren());
            for (Map.Entry<@NotNull String, @NotNull IAbstractElement> child: children.entrySet()) {
                String key = parentKey + "." + child.getKey();
                allElements.put(key, child.getValue());
                allElements.putAll(getChildElements(child.getValue(), key));
            }
        }
        return allElements;
    }

    default @NotNull Map<@NotNull String, @NotNull IAbstractElement> getAllElements() {
        Map<@NotNull String, @NotNull IAbstractElement> allElements = new HashMap<>();
        //コピーしてから回さないとConcurrentModificationExceptionが出る
        Map<@NotNull String, @NotNull IAbstractElement> elements = new HashMap<>(getElements());
        for (Map.Entry<@NotNull String, @NotNull IAbstractElement> entry : elements.entrySet()) {
            allElements.put(entry.getKey(), entry.getValue());
            allElements.putAll(getChildElements(entry.getValue(), entry.getKey()));
        }
        return allElements;
    }
}
