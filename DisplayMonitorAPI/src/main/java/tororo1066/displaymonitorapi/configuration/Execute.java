package tororo1066.displaymonitorapi.configuration;

import tororo1066.displaymonitorapi.actions.IActionContext;

/**
 * {@link IActionContext}を元に処理を行う
 */
@FunctionalInterface
public interface Execute {
    /**
     * 処理を実行する
     * @param context {@link IActionContext}
     */
    void invoke(IActionContext context);

    /**
     * 空のExecuteを取得する
     * @return 空のExecute
     */
    static Execute empty() {
        return context -> {};
    }
}