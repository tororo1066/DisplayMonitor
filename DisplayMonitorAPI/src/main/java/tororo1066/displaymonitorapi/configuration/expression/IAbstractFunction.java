package tororo1066.displaymonitorapi.configuration.expression;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface IAbstractFunction {

    @NotNull String getName();

    @NotNull Object eval(@NotNull List<@NotNull Object> args, @NotNull Map<@NotNull String, @NotNull Object> parameters);
}
