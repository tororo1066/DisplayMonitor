package tororo1066.displaymonitorapi;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfiguration;

public interface IDisplayUtils {

    @NotNull IAdvancedConfiguration toAdvancedConfiguration(@NotNull YamlConfiguration config);
}
