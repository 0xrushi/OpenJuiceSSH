package com.daremote.app.core.data.repository;

import com.daremote.app.core.data.ssh.SshSessionManager;
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
public final class SshConnectionRepositoryImpl_Factory implements Factory<SshConnectionRepositoryImpl> {
  private final Provider<SshSessionManager> sessionManagerProvider;

  public SshConnectionRepositoryImpl_Factory(Provider<SshSessionManager> sessionManagerProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public SshConnectionRepositoryImpl get() {
    return newInstance(sessionManagerProvider.get());
  }

  public static SshConnectionRepositoryImpl_Factory create(
      Provider<SshSessionManager> sessionManagerProvider) {
    return new SshConnectionRepositoryImpl_Factory(sessionManagerProvider);
  }

  public static SshConnectionRepositoryImpl newInstance(SshSessionManager sessionManager) {
    return new SshConnectionRepositoryImpl(sessionManager);
  }
}
