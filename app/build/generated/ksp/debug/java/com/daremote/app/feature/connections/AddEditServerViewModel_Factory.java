package com.daremote.app.feature.connections;

import androidx.lifecycle.SavedStateHandle;
import com.daremote.app.core.domain.repository.ServerRepository;
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
public final class AddEditServerViewModel_Factory implements Factory<AddEditServerViewModel> {
  private final Provider<ServerRepository> serverRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public AddEditServerViewModel_Factory(Provider<ServerRepository> serverRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.serverRepositoryProvider = serverRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public AddEditServerViewModel get() {
    return newInstance(serverRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static AddEditServerViewModel_Factory create(
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new AddEditServerViewModel_Factory(serverRepositoryProvider, savedStateHandleProvider);
  }

  public static AddEditServerViewModel newInstance(ServerRepository serverRepository,
      SavedStateHandle savedStateHandle) {
    return new AddEditServerViewModel(serverRepository, savedStateHandle);
  }
}
