package com.daremote.app;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class DaRemoteApp_MembersInjector implements MembersInjector<DaRemoteApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public DaRemoteApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<DaRemoteApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new DaRemoteApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(DaRemoteApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.daremote.app.DaRemoteApp.workerFactory")
  public static void injectWorkerFactory(DaRemoteApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
