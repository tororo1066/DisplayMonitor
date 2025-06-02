package tororo1066.displaymonitorapi.storage;

import org.jetbrains.annotations.NotNull;
import tororo1066.displaymonitorapi.configuration.expression.IAbstractFunction;

public interface IFunctionStorage {

    void addFunction(@NotNull IAbstractFunction function);
}
