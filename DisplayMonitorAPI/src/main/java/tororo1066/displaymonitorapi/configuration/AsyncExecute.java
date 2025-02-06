package tororo1066.displaymonitorapi.configuration;

/**
 * {@link tororo1066.displaymonitorapi.actions.IActionContext}を元に処理を行う<br>
 * Asyncはあくまで識別のためのものであり、適切に動作させるには非同期上で実行する必要がある
 */
@FunctionalInterface
public interface AsyncExecute extends Execute {

    /**
     * 空のAsyncExecuteを取得する
     * @return 空のAsyncExecute
     */
    static AsyncExecute empty() {
        return context -> {};
    }
}
