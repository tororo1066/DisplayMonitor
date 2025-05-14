package tororo1066.displaymonitorapi.workspace;

import org.jetbrains.annotations.NotNull;
import tororo1066.displaymonitorapi.configuration.IActionConfiguration;

import java.util.Collection;
import java.util.Map;

public interface IAbstractWorkspace {

    @NotNull String getName();

    @NotNull Map<@NotNull String, @NotNull IActionConfiguration> getActionConfigurations();
}
