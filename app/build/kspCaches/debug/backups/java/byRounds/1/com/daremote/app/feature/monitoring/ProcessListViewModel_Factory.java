package com.daremote.app.feature.monitoring;

import androidx.lifecycle.SavedStateHandle;
import com.daremote.app.core.data.ssh.SshCommandExecutor;
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
public final class ProcessListViewModel_Factory implements Factory<ProcessListViewModel> {
  private final Provider<SshCommandExecutor> commandExecutorProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public ProcessListViewModel_Factory(Provider<SshCommandExecutor> commandExecutorProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.commandExecutorProvider = commandExecutorProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public ProcessListViewModel get() {
    return newInstance(commandExecutorProvider.get(), savedStateHandleProvider.get());
  }

  public static ProcessListViewModel_Factory create(
      Provider<SshCommandExecutor> commandExecutorProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new ProcessListViewModel_Factory(commandExecutorProvider, savedStateHandleProvider);
  }

  public static ProcessListViewModel newInstance(SshCommandExecutor commandExecutor,
      SavedStateHandle savedStateHandle) {
    return new ProcessListViewModel(commandExecutor, savedStateHandle);
  }
}
