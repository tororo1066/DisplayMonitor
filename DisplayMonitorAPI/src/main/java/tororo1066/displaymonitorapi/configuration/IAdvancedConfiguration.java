package tororo1066.displaymonitorapi.configuration;

import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tororo1066.displaymonitorapi.actions.IPublicActionContext;

import java.util.Map;

public interface IAdvancedConfiguration extends IAdvancedConfigurationSection, Configuration, Cloneable {

    @NotNull Character SEPARATOR = '\u0BEC';

    @NotNull Map<String, Object> getParameters();

    void setParameters(@NotNull Map<String, Object> parameters);

    @Nullable IPublicActionContext getPublicContext();

    void setPublicContext(@Nullable IPublicActionContext publicContext);

    @NotNull Object evaluate(@NotNull String value);

    @NotNull IAdvancedConfiguration clone();
}