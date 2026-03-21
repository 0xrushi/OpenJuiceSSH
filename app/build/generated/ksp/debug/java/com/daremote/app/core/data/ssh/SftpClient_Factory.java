package com.daremote.app.core.data.ssh;

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
public final class SftpClient_Factory implements Factory<SftpClient> {
  private final Provider<SshSessionManager> sessionManagerProvider;

  public SftpClient_Factory(Provider<SshSessionManager> sessionManagerProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public SftpClient get() {
    return newInstance(sessionManagerProvider.get());
  }

  public static SftpClient_Factory create(Provider<SshSessionManager> sessionManagerProvider) {
    return new SftpClient_Factory(sessionManagerProvider);
  }

  public static SftpClient newInstance(SshSessionManager sessionManager) {
    return new SftpClient(sessionManager);
  }
}
