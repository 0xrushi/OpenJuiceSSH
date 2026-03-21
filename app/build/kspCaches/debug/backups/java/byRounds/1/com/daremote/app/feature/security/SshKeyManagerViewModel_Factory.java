package com.daremote.app.feature.security;

import com.daremote.app.core.security.SshKeyManager;
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
public final class SshKeyManagerViewModel_Factory implements Factory<SshKeyManagerViewModel> {
  private final Provider<SshKeyManager> sshKeyManagerProvider;

  public SshKeyManagerViewModel_Factory(Provider<SshKeyManager> sshKeyManagerProvider) {
    this.sshKeyManagerProvider = sshKeyManagerProvider;
  }

  @Override
  public SshKeyManagerViewModel get() {
    return newInstance(sshKeyManagerProvider.get());
  }

  public static SshKeyManagerViewModel_Factory create(
      Provider<SshKeyManager> sshKeyManagerProvider) {
    return new SshKeyManagerViewModel_Factory(sshKeyManagerProvider);
  }

  public static SshKeyManagerViewModel newInstance(SshKeyManager sshKeyManager) {
    return new SshKeyManagerViewModel(sshKeyManager);
  }
}
