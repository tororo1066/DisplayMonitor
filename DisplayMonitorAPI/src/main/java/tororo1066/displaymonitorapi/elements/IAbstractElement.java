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

    private boolean isNullable(Field field) {
        return Arrays.stream(field.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().getSimpleName().equals("Nullable"));
    }

    private void prepareChild(IAdvancedConfigurationSection section, Field field, Object instance) throws IllegalAccessException {
        ISettableProcessor processor = IDisplayMonitor.DisplayMonitorInstance.getInstance().getSettableProcessor();
        boolean defaultAccessible = field.canAccess(instance);
        field.setAccessible(true);
        Settable annotation = field.getAnnotation(Settable.class);
        if (annotation == null) return;
        String key = annotation.name().isEmpty() ? field.getName() : annotation.name();
        if (annotation.childOnly()) {
            IAdvancedConfigurationSection newSection = section.getAdvancedConfigurationSection(key);
            if (newSection == null) return;
            Object newInstance = field.get(instance);
            List<Field> newFields = processor.getSettableFields(newInstance.getClass());
            for (Field newField : newFields) {
                prepareChild(newSection, newField, newInstance);
            }
        } else {
            Object value = section.withParameters(processor.processVariable(field.get(instance)),
                    (IAdvancedConfigurationSection sec) -> processor.processValue(sec, key, field.getType()));
            if (value != null || isNullable(field)) {
                field.set(instance, value);
            }
        }
        field.setAccessible(defaultAccessible);
    }

    default void prepare(@NotNull IAdvancedConfigurationSection configuration) {
        ISettableProcessor processor = IDisplayMonitor.DisplayMonitorInstance.getInstance().getSettableProcessor();
        List<Field> settableFields = processor.getSettableFields(this.getClass());
        for (Field field : settableFields) {
            try {
                prepareChild(configuration, field, this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
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
