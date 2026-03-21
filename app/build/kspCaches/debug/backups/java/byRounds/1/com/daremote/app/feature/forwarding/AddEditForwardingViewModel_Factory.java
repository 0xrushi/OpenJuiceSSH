package com.daremote.app.feature.forwarding;

import androidx.lifecycle.SavedStateHandle;
import com.daremote.app.core.domain.repository.ForwardingRepository;
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
public final class AddEditForwardingViewModel_Factory implements Factory<AddEditForwardingViewModel> {
  private final Provider<ForwardingRepository> forwardingRepositoryProvider;

  private final Provider<ServerRepository> serverRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public AddEditForwardingViewModel_Factory(
      Provider<ForwardingRepository> forwardingRepositoryProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.forwardingRepositoryProvider = forwardingRepositoryProvider;
    this.serverRepositoryProvider = serverRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public AddEditForwardingViewModel get() {
    return newInstance(forwardingRepositoryProvider.get(), serverRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static AddEditForwardingViewModel_Factory create(
      Provider<ForwardingRepository> forwardingRepositoryProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new AddEditForwardingViewModel_Factory(forwardingRepositoryProvider, serverRepositoryProvider, savedStateHandleProvider);
  }

  public static AddEditForwardingViewModel newInstance(ForwardingRepository forwardingRepository,
      ServerRepository serverRepository, SavedStateHandle savedStateHandle) {
    return new AddEditForwardingViewModel(forwardingRepository, serverRepository, savedStateHandle);
  }
}
