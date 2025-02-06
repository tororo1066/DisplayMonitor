package tororo1066.displaymonitorapi;

import tororo1066.displaymonitorapi.storage.IActionStorage;
import tororo1066.displaymonitorapi.storage.IElementStorage;

public interface IDisplayMonitor {

    IActionStorage getActionStorage();

    IElementStorage getElementStorage();
}
