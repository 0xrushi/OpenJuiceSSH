package com.daremote.app.core.service;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.daremote.app.core.data.ssh.SshCommandExecutor;
import com.daremote.app.core.data.ssh.SshSessionManager;
import com.daremote.app.core.domain.repository.AlertRepository;
import com.daremote.app.core.domain.repository.ServerRepository;
import dagger.internal.DaggerGenerated;
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
public final class AlertWorker_Factory {
  private final Provider<AlertRepository> alertRepositoryProvider;

  private final Provider<ServerRepository> serverRepositoryProvider;

  private final Provider<SshSessionManager> sessionManagerProvider;

  private final Provider<SshCommandExecutor> commandExecutorProvider;

  public AlertWorker_Factory(Provider<AlertRepository> alertRepositoryProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SshSessionManager> sessionManagerProvider,
      Provider<SshCommandExecutor> commandExecutorProvider) {
    this.alertRepositoryProvider = alertRepositoryProvider;
    this.serverRepositoryProvider = serverRepositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.commandExecutorProvider = commandExecutorProvider;
  }

  public AlertWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, alertRepositoryProvider.get(), serverRepositoryProvider.get(), sessionManagerProvider.get(), commandExecutorProvider.get());
  }

  public static AlertWorker_Factory create(Provider<AlertRepository> alertRepositoryProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SshSessionManager> sessionManagerProvider,
      Provider<SshCommandExecutor> commandExecutorProvider) {
    return new AlertWorker_Factory(alertRepositoryProvider, serverRepositoryProvider, sessionManagerProvider, commandExecutorProvider);
  }

  public static AlertWorker newInstance(Context appContext, WorkerParameters workerParams,
      AlertRepository alertRepository, ServerRepository serverRepository,
      SshSessionManager sessionManager, SshCommandExecutor commandExecutor) {
    return new AlertWorker(appContext, workerParams, alertRepository, serverRepository, sessionManager, commandExecutor);
  }
}
