package com.daremote.app.feature.terminal;

import androidx.lifecycle.SavedStateHandle;
import com.daremote.app.core.data.ssh.SshSessionManager;
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
public final class TerminalViewModel_Factory implements Factory<TerminalViewModel> {
  private final Provider<SshSessionManager> sessionManagerProvider;

  private final Provider<ServerRepository> serverRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public TerminalViewModel_Factory(Provider<SshSessionManager> sessionManagerProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.serverRepositoryProvider = serverRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public TerminalViewModel get() {
    return newInstance(sessionManagerProvider.get(), serverRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static TerminalViewModel_Factory create(Provider<SshSessionManager> sessionManagerProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new TerminalViewModel_Factory(sessionManagerProvider, serverRepositoryProvider, savedStateHandleProvider);
  }

  public static TerminalViewModel newInstance(SshSessionManager sessionManager,
      ServerRepository serverRepository, SavedStateHandle savedStateHandle) {
    return new TerminalViewModel(sessionManager, serverRepository, savedStateHandle);
  }
}
