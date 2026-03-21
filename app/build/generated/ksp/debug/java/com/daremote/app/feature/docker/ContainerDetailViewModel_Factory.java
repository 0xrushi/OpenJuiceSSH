package com.daremote.app.feature.docker;

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
public final class ContainerDetailViewModel_Factory implements Factory<ContainerDetailViewModel> {
  private final Provider<SshCommandExecutor> commandExecutorProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public ContainerDetailViewModel_Factory(Provider<SshCommandExecutor> commandExecutorProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.commandExecutorProvider = commandExecutorProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public ContainerDetailViewModel get() {
    return newInstance(commandExecutorProvider.get(), savedStateHandleProvider.get());
  }

  public static ContainerDetailViewModel_Factory create(
      Provider<SshCommandExecutor> commandExecutorProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new ContainerDetailViewModel_Factory(commandExecutorProvider, savedStateHandleProvider);
  }

  public static ContainerDetailViewModel newInstance(SshCommandExecutor commandExecutor,
      SavedStateHandle savedStateHandle) {
    return new ContainerDetailViewModel(commandExecutor, savedStateHandle);
  }
}
