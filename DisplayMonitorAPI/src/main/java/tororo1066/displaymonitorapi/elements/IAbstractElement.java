package tororo1066.displaymonitorapi.elements;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection;

import java.util.UUID;

public interface IAbstractElement extends Cloneable {

    boolean syncGroup();

    @Nullable UUID getGroupUUID();

    void setGroupUUID(@Nullable UUID groupUUID);

    @Nullable UUID getContextUUID();

    void setContextUUID(@Nullable UUID contextUUID);

    void spawn(@Nullable Entity entity, @NotNull Location location);

    void remove();

    void tick(@Nullable Entity entity);

    void attachEntity(@NotNull Entity entity);

    void move(@NotNull Location location);

    void prepare(@NotNull IAdvancedConfigurationSection configuration);

    void applyChanges();

    default void edit(@NotNull IAdvancedConfigurationSection configuration) {
        prepare(configuration);
        applyChanges();
    }

    @NotNull IAbstractElement clone();
}
