package tororo1066.displaymonitorapi;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import tororo1066.displaymonitorapi.elements.ISettableProcessor;
import tororo1066.displaymonitorapi.storage.IActionStorage;
import tororo1066.displaymonitorapi.storage.IElementStorage;
import tororo1066.displaymonitorapi.storage.IWorkspaceStorage;

public interface IDisplayMonitor extends Plugin {

    class DisplayMonitorInstance {
        private static IDisplayMonitor instance;

        public static IDisplayMonitor getInstance() {
            return instance;
        }

        public static void setInstance(IDisplayMonitor instance) {
            DisplayMonitorInstance.instance = instance;
        }
    }

    /**
     * ActionStorageを取得する
     * @return {@link IActionStorage}
     */
    @NotNull IActionStorage getActionStorage();

    /**
     * ElementStorageを取得する
     * @return {@link IElementStorage}
     */
    @NotNull IElementStorage getElementStorage();

    /**
     * WorkspaceStorageを取得する
     * @return {@link IWorkspaceStorage}
     */
    @NotNull IWorkspaceStorage getWorkspaceStorage();

    /**
     * SettableProcessorを取得する
     * @return {@link ISettableProcessor}
     */
    @NotNull ISettableProcessor getSettableProcessor();
}
