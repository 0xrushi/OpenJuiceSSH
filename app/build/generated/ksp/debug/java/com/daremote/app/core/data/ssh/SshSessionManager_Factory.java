package com.daremote.app.core.data.ssh;

import com.daremote.app.core.security.CredentialManager;
import com.daremote.app.core.security.SshKeyManager;
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
public final class SshSessionManager_Factory implements Factory<SshSessionManager> {
  private final Provider<CredentialManager> credentialManagerProvider;

  private final Provider<SshKeyManager> sshKeyManagerProvider;

  public SshSessionManager_Factory(Provider<CredentialManager> credentialManagerProvider,
      Provider<SshKeyManager> sshKeyManagerProvider) {
    this.credentialManagerProvider = credentialManagerProvider;
    this.sshKeyManagerProvider = sshKeyManagerProvider;
  }

  @Override
  public SshSessionManager get() {
    return newInstance(credentialManagerProvider.get(), sshKeyManagerProvider.get());
  }

  public static SshSessionManager_Factory create(
      Provider<CredentialManager> credentialManagerProvider,
      Provider<SshKeyManager> sshKeyManagerProvider) {
    return new SshSessionManager_Factory(credentialManagerProvider, sshKeyManagerProvider);
  }

  public static SshSessionManager newInstance(CredentialManager credentialManager,
      SshKeyManager sshKeyManager) {
    return new SshSessionManager(credentialManager, sshKeyManager);
  }
}
