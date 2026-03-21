package com.daremote.app.core.service;

import com.daremote.app.core.data.ssh.SshSessionManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SshConnectionService_MembersInjector implements MembersInjector<SshConnectionService> {
  private final Provider<SshSessionManager> sessionManagerProvider;

  private final Provider<TunnelManager> tunnelManagerProvider;

  public SshConnectionService_MembersInjector(Provider<SshSessionManager> sessionManagerProvider,
      Provider<TunnelManager> tunnelManagerProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.tunnelManagerProvider = tunnelManagerProvider;
  }

  public static MembersInjector<SshConnectionService> create(
      Provider<SshSessionManager> sessionManagerProvider,
      Provider<TunnelManager> tunnelManagerProvider) {
    return new SshConnectionService_MembersInjector(sessionManagerProvider, tunnelManagerProvider);
  }

  @Override
  public void injectMembers(SshConnectionService instance) {
    injectSessionManager(instance, sessionManagerProvider.get());
    injectTunnelManager(instance, tunnelManagerProvider.get());
  }

  @InjectedFieldSignature("com.daremote.app.core.service.SshConnectionService.sessionManager")
  public static void injectSessionManager(SshConnectionService instance,
      SshSessionManager sessionManager) {
    instance.sessionManager = sessionManager;
  }

  @InjectedFieldSignature("com.daremote.app.core.service.SshConnectionService.tunnelManager")
  public static void injectTunnelManager(SshConnectionService instance,
      TunnelManager tunnelManager) {
    instance.tunnelManager = tunnelManager;
  }
}
