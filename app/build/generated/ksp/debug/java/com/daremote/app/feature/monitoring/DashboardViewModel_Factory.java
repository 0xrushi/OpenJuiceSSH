package com.daremote.app.feature.monitoring;

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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<SshCommandExecutor> commandExecutorProvider;

  private final Provider<ServerRepository> serverRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public DashboardViewModel_Factory(Provider<SshCommandExecutor> commandExecutorProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.commandExecutorProvider = commandExecutorProvider;
    this.serverRepositoryProvider = serverRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(commandExecutorProvider.get(), serverRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static DashboardViewModel_Factory create(
      Provider<SshCommandExecutor> commandExecutorProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new DashboardViewModel_Factory(commandExecutorProvider, serverRepositoryProvider, savedStateHandleProvider);
  }

  public static DashboardViewModel newInstance(SshCommandExecutor commandExecutor,
      ServerRepository serverRepository, SavedStateHandle savedStateHandle) {
    return new DashboardViewModel(commandExecutor, serverRepository, savedStateHandle);
  }
}
