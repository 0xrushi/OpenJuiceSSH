package com.daremote.app.core.security;

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
public final class SshKeyManager_Factory implements Factory<SshKeyManager> {
  private final Provider<CredentialManager> credentialManagerProvider;

  public SshKeyManager_Factory(Provider<CredentialManager> credentialManagerProvider) {
    this.credentialManagerProvider = credentialManagerProvider;
  }

  @Override
  public SshKeyManager get() {
    return newInstance(credentialManagerProvider.get());
  }

  public static SshKeyManager_Factory create(
      Provider<CredentialManager> credentialManagerProvider) {
    return new SshKeyManager_Factory(credentialManagerProvider);
  }

  public static SshKeyManager newInstance(CredentialManager credentialManager) {
    return new SshKeyManager(credentialManager);
  }
}
