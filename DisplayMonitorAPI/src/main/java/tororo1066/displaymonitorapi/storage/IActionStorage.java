package tororo1066.displaymonitorapi.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tororo1066.displaymonitorapi.actions.IAbstractAction;
import tororo1066.displaymonitorapi.actions.IActionContext;
import tororo1066.displaymonitorapi.actions.IPublicActionContext;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection;

import java.util.function.Function;

public interface IActionStorage {

    void registerAction(@NotNull String name, @NotNull Class<? extends @NotNull IAbstractAction> action);

    default void registerAction(@NotNull Class<? extends @NotNull IAbstractAction> action) {
        registerAction(action.getSimpleName(), action);
    }

    @NotNull IPublicActionContext createPublicContext();

    @NotNull IActionContext createActionContext(@NotNull IPublicActionContext publicContext);

    void trigger(@NotNull String name, @NotNull IActionContext context, @Nullable Function<@NotNull IAdvancedConfigurationSection, @NotNull Boolean> condition);
}
