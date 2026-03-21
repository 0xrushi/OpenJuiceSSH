package com.daremote.app.feature.forwarding;

import com.daremote.app.core.domain.repository.ForwardingRepository;
import com.daremote.app.core.service.TunnelManager;
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
public final class ForwardingListViewModel_Factory implements Factory<ForwardingListViewModel> {
  private final Provider<ForwardingRepository> forwardingRepositoryProvider;

  private final Provider<TunnelManager> tunnelManagerProvider;

  public ForwardingListViewModel_Factory(
      Provider<ForwardingRepository> forwardingRepositoryProvider,
      Provider<TunnelManager> tunnelManagerProvider) {
    this.forwardingRepositoryProvider = forwardingRepositoryProvider;
    this.tunnelManagerProvider = tunnelManagerProvider;
  }

  @Override
  public ForwardingListViewModel get() {
    return newInstance(forwardingRepositoryProvider.get(), tunnelManagerProvider.get());
  }

  public static ForwardingListViewModel_Factory create(
      Provider<ForwardingRepository> forwardingRepositoryProvider,
      Provider<TunnelManager> tunnelManagerProvider) {
    return new ForwardingListViewModel_Factory(forwardingRepositoryProvider, tunnelManagerProvider);
  }

  public static ForwardingListViewModel newInstance(ForwardingRepository forwardingRepository,
      TunnelManager tunnelManager) {
    return new ForwardingListViewModel(forwardingRepository, tunnelManager);
  }
}
