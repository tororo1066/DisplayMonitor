package tororo1066.displaymonitorapi.configuration.expression;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IAbstractFunction {

    @NotNull String getName();

    @NotNull Object eval(@NotNull List<@NotNull Object> args);
}
