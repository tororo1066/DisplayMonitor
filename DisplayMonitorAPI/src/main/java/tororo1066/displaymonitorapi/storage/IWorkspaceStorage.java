package tororo1066.displaymonitorapi.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tororo1066.displaymonitorapi.workspace.IAbstractWorkspace;

import java.util.Map;

public interface IWorkspaceStorage {

    @NotNull Map<@NotNull String, @NotNull IAbstractWorkspace> getWorkspaces();

    @Nullable IAbstractWorkspace getWorkspace(@NotNull String name);

    void registerWorkspace(@NotNull IAbstractWorkspace workspace);
}
