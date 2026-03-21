package com.daremote.app.feature.connections;

import com.daremote.app.core.domain.repository.ServerRepository;
import com.daremote.app.core.domain.repository.SshConnectionRepository;
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
public final class ConnectionListViewModel_Factory implements Factory<ConnectionListViewModel> {
  private final Provider<ServerRepository> serverRepositoryProvider;

  private final Provider<SshConnectionRepository> sshConnectionRepositoryProvider;

  public ConnectionListViewModel_Factory(Provider<ServerRepository> serverRepositoryProvider,
      Provider<SshConnectionRepository> sshConnectionRepositoryProvider) {
    this.serverRepositoryProvider = serverRepositoryProvider;
    this.sshConnectionRepositoryProvider = sshConnectionRepositoryProvider;
  }

  @Override
  public ConnectionListViewModel get() {
    return newInstance(serverRepositoryProvider.get(), sshConnectionRepositoryProvider.get());
  }

  public static ConnectionListViewModel_Factory create(
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SshConnectionRepository> sshConnectionRepositoryProvider) {
    return new ConnectionListViewModel_Factory(serverRepositoryProvider, sshConnectionRepositoryProvider);
  }

  public static ConnectionListViewModel newInstance(ServerRepository serverRepository,
      SshConnectionRepository sshConnectionRepository) {
    return new ConnectionListViewModel(serverRepository, sshConnectionRepository);
  }
}
