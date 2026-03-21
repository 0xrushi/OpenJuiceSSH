package com.daremote.app.core.di;

import com.daremote.app.core.database.AppDatabase;
import com.daremote.app.core.database.dao.ServerDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideServerDaoFactory implements Factory<ServerDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideServerDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ServerDao get() {
    return provideServerDao(dbProvider.get());
  }

  public static AppModule_ProvideServerDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideServerDaoFactory(dbProvider);
  }

  public static ServerDao provideServerDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideServerDao(db));
  }
}
