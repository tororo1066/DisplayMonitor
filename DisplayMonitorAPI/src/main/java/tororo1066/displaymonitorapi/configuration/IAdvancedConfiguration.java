package tororo1066.displaymonitorapi.configuration;

import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface IAdvancedConfiguration extends IAdvancedConfigurationSection, Configuration, Cloneable {

    @NotNull Map<String, Object> getParameters();

    void setParameters(@NotNull Map<String, Object> parameters);
}