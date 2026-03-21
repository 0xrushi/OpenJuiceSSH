package com.daremote.app.feature.snippets;

import com.daremote.app.core.data.ssh.SshCommandExecutor;
import com.daremote.app.core.domain.repository.ServerRepository;
import com.daremote.app.core.domain.repository.SnippetRepository;
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
public final class SnippetListViewModel_Factory implements Factory<SnippetListViewModel> {
  private final Provider<SnippetRepository> snippetRepositoryProvider;

  private final Provider<ServerRepository> serverRepositoryProvider;

  private final Provider<SshCommandExecutor> commandExecutorProvider;

  public SnippetListViewModel_Factory(Provider<SnippetRepository> snippetRepositoryProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SshCommandExecutor> commandExecutorProvider) {
    this.snippetRepositoryProvider = snippetRepositoryProvider;
    this.serverRepositoryProvider = serverRepositoryProvider;
    this.commandExecutorProvider = commandExecutorProvider;
  }

  @Override
  public SnippetListViewModel get() {
    return newInstance(snippetRepositoryProvider.get(), serverRepositoryProvider.get(), commandExecutorProvider.get());
  }

  public static SnippetListViewModel_Factory create(
      Provider<SnippetRepository> snippetRepositoryProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SshCommandExecutor> commandExecutorProvider) {
    return new SnippetListViewModel_Factory(snippetRepositoryProvider, serverRepositoryProvider, commandExecutorProvider);
  }

  public static SnippetListViewModel newInstance(SnippetRepository snippetRepository,
      ServerRepository serverRepository, SshCommandExecutor commandExecutor) {
    return new SnippetListViewModel(snippetRepository, serverRepository, commandExecutor);
  }
}
