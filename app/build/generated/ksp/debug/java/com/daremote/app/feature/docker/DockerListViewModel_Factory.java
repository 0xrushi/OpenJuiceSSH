package com.daremote.app.feature.docker;

import androidx.lifecycle.SavedStateHandle;
import com.daremote.app.core.data.ssh.SshCommandExecutor;
import com.daremote.app.core.domain.repository.ServerRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DockerListViewModel_Factory implements Factory<DockerListViewModel> {
  private final Provider<SshCommandExecutor> commandExecutorProvider;

  private final Provider<ServerRepository> serverRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public DockerListViewModel_Factory(Provider<SshCommandExecutor> commandExecutorProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.commandExecutorProvider = commandExecutorProvider;
    this.serverRepositoryProvider = serverRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public DockerListViewModel get() {
    return newInstance(commandExecutorProvider.get(), serverRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static DockerListViewModel_Factory create(
      Provider<SshCommandExecutor> commandExecutorProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new DockerListViewModel_Factory(commandExecutorProvider, serverRepositoryProvider, savedStateHandleProvider);
  }

  public static DockerListViewModel newInstance(SshCommandExecutor commandExecutor,
      ServerRepository serverRepository, SavedStateHandle savedStateHandle) {
    return new DockerListViewModel(commandExecutor, serverRepository, savedStateHandle);
  }
}
