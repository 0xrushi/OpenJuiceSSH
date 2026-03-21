package com.daremote.app.feature.alerts;

import com.daremote.app.core.domain.repository.AlertRepository;
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
public final class AlertConfigViewModel_Factory implements Factory<AlertConfigViewModel> {
  private final Provider<AlertRepository> alertRepositoryProvider;

  private final Provider<ServerRepository> serverRepositoryProvider;

  public AlertConfigViewModel_Factory(Provider<AlertRepository> alertRepositoryProvider,
      Provider<ServerRepository> serverRepositoryProvider) {
    this.alertRepositoryProvider = alertRepositoryProvider;
    this.serverRepositoryProvider = serverRepositoryProvider;
  }

  @Override
  public AlertConfigViewModel get() {
    return newInstance(alertRepositoryProvider.get(), serverRepositoryProvider.get());
  }

  public static AlertConfigViewModel_Factory create(
      Provider<AlertRepository> alertRepositoryProvider,
      Provider<ServerRepository> serverRepositoryProvider) {
    return new AlertConfigViewModel_Factory(alertRepositoryProvider, serverRepositoryProvider);
  }

  public static AlertConfigViewModel newInstance(AlertRepository alertRepository,
      ServerRepository serverRepository) {
    return new AlertConfigViewModel(alertRepository, serverRepository);
  }
}
