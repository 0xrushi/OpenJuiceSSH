package com.daremote.app.core.data.repository;

import com.daremote.app.core.database.dao.ServerDao;
import com.daremote.app.core.database.dao.ServerGroupDao;
import com.daremote.app.core.security.CredentialManager;
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
public final class ServerRepositoryImpl_Factory implements Factory<ServerRepositoryImpl> {
  private final Provider<ServerDao> serverDaoProvider;

  private final Provider<ServerGroupDao> serverGroupDaoProvider;

  private final Provider<CredentialManager> credentialManagerProvider;

  public ServerRepositoryImpl_Factory(Provider<ServerDao> serverDaoProvider,
      Provider<ServerGroupDao> serverGroupDaoProvider,
      Provider<CredentialManager> credentialManagerProvider) {
    this.serverDaoProvider = serverDaoProvider;
    this.serverGroupDaoProvider = serverGroupDaoProvider;
    this.credentialManagerProvider = credentialManagerProvider;
  }

  @Override
  public ServerRepositoryImpl get() {
    return newInstance(serverDaoProvider.get(), serverGroupDaoProvider.get(), credentialManagerProvider.get());
  }

  public static ServerRepositoryImpl_Factory create(Provider<ServerDao> serverDaoProvider,
      Provider<ServerGroupDao> serverGroupDaoProvider,
      Provider<CredentialManager> credentialManagerProvider) {
    return new ServerRepositoryImpl_Factory(serverDaoProvider, serverGroupDaoProvider, credentialManagerProvider);
  }

  public static ServerRepositoryImpl newInstance(ServerDao serverDao, ServerGroupDao serverGroupDao,
      CredentialManager credentialManager) {
    return new ServerRepositoryImpl(serverDao, serverGroupDao, credentialManager);
  }
}
