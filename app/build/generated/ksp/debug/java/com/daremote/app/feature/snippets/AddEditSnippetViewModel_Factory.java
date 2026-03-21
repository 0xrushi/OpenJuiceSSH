package com.daremote.app.feature.snippets;

import androidx.lifecycle.SavedStateHandle;
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
public final class AddEditSnippetViewModel_Factory implements Factory<AddEditSnippetViewModel> {
  private final Provider<SnippetRepository> snippetRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public AddEditSnippetViewModel_Factory(Provider<SnippetRepository> snippetRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.snippetRepositoryProvider = snippetRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public AddEditSnippetViewModel get() {
    return newInstance(snippetRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static AddEditSnippetViewModel_Factory create(
      Provider<SnippetRepository> snippetRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new AddEditSnippetViewModel_Factory(snippetRepositoryProvider, savedStateHandleProvider);
  }

  public static AddEditSnippetViewModel newInstance(SnippetRepository snippetRepository,
      SavedStateHandle savedStateHandle) {
    return new AddEditSnippetViewModel(snippetRepository, savedStateHandle);
  }
}
