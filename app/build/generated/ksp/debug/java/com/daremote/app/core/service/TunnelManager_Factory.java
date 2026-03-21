package com.daremote.app.core.service;

import com.daremote.app.core.data.ssh.SshSessionManager;
import com.daremote.app.core.domain.repository.ForwardingRepository;
import com.daremote.app.core.domain.repository.ServerRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class TunnelManager_Factory implements Factory<TunnelManager> {
  private final Provider<SshSessionManager> sessionManagerProvider;

  private final Provider<ForwardingRepository> forwardingRepositoryProvider;

  private final Provider<ServerRepository> serverRepositoryProvider;

  public TunnelManager_Factory(Provider<SshSessionManager> sessionManagerProvider,
      Provider<ForwardingRepository> forwardingRepositoryProvider,
      Provider<ServerRepository> serverRepositoryProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.forwardingRepositoryProvider = forwardingRepositoryProvider;
    this.serverRepositoryProvider = serverRepositoryProvider;
  }

  @Override
  public TunnelManager get() {
    return newInstance(sessionManagerProvider.get(), forwardingRepositoryProvider.get(), serverRepositoryProvider.get());
  }

  public static TunnelManager_Factory create(Provider<SshSessionManager> sessionManagerProvider,
      Provider<ForwardingRepository> forwardingRepositoryProvider,
      Provider<ServerRepository> serverRepositoryProvider) {
    return new TunnelManager_Factory(sessionManagerProvider, forwardingRepositoryProvider, serverRepositoryProvider);
  }

  public static TunnelManager newInstance(SshSessionManager sessionManager,
      ForwardingRepository forwardingRepository, ServerRepository serverRepository) {
    return new TunnelManager(sessionManager, forwardingRepository, serverRepository);
  }
}
