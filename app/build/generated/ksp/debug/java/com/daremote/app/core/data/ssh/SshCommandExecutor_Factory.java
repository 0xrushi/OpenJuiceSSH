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
public final class SshCommandExecutor_Factory implements Factory<SshCommandExecutor> {
  private final Provider<SshSessionManager> sessionManagerProvider;

  public SshCommandExecutor_Factory(Provider<SshSessionManager> sessionManagerProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public SshCommandExecutor get() {
    return newInstance(sessionManagerProvider.get());
  }

  public static SshCommandExecutor_Factory create(
      Provider<SshSessionManager> sessionManagerProvider) {
    return new SshCommandExecutor_Factory(sessionManagerProvider);
  }

  public static SshCommandExecutor newInstance(SshSessionManager sessionManager) {
    return new SshCommandExecutor(sessionManager);
  }
}
