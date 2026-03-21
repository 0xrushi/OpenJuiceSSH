package com.daremote.app.core.di;

import com.daremote.app.core.data.ssh.SshSessionManager;
import com.daremote.app.core.security.CredentialManager;
import com.daremote.app.core.security.SshKeyManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class SshModule_ProvideSshSessionManagerFactory implements Factory<SshSessionManager> {
  private final Provider<CredentialManager> credentialManagerProvider;

  private final Provider<SshKeyManager> sshKeyManagerProvider;

  public SshModule_ProvideSshSessionManagerFactory(
      Provider<CredentialManager> credentialManagerProvider,
      Provider<SshKeyManager> sshKeyManagerProvider) {
    this.credentialManagerProvider = credentialManagerProvider;
    this.sshKeyManagerProvider = sshKeyManagerProvider;
  }

  @Override
  public SshSessionManager get() {
    return provideSshSessionManager(credentialManagerProvider.get(), sshKeyManagerProvider.get());
  }

  public static SshModule_ProvideSshSessionManagerFactory create(
      Provider<CredentialManager> credentialManagerProvider,
      Provider<SshKeyManager> sshKeyManagerProvider) {
    return new SshModule_ProvideSshSessionManagerFactory(credentialManagerProvider, sshKeyManagerProvider);
  }

  public static SshSessionManager provideSshSessionManager(CredentialManager credentialManager,
      SshKeyManager sshKeyManager) {
    return Preconditions.checkNotNullFromProvides(SshModule.INSTANCE.provideSshSessionManager(credentialManager, sshKeyManager));
  }
}
