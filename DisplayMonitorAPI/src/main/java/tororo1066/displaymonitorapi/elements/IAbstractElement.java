package tororo1066.displaymonitorapi.elements;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tororo1066.displaymonitorapi.IDisplayMonitor;
import tororo1066.displaymonitorapi.configuration.IAdvancedConfigurationSection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    void startTick(@Nullable Entity entity);

    void stopTick();

    void attachEntity(@NotNull Entity entity);

    void move(@NotNull Location location);

    default void prepare(@NotNull IAdvancedConfigurationSection configuration) {
        ISettableProcessor processor = IDisplayMonitor.DisplayMonitorInstance.getInstance().getSettableProcessor();
        processor.prepareSettableFields(configuration, this);
    }

    void applyChanges();

    default void edit(@NotNull IAdvancedConfigurationSection configuration) {
        prepare(configuration);
        applyChanges();
    }

    default boolean hasChildren() {
        return false;
    }

    default @NotNull Map<@NotNull String, @NotNull IAbstractElement> getChildren() {
        return Map.of();
    }

    @NotNull IAbstractElement clone();
}
