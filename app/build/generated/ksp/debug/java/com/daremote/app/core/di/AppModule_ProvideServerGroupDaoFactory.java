package com.daremote.app.core.di;

import com.daremote.app.core.database.AppDatabase;
import com.daremote.app.core.database.dao.ServerGroupDao;
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
public final class AppModule_ProvideServerGroupDaoFactory implements Factory<ServerGroupDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideServerGroupDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ServerGroupDao get() {
    return provideServerGroupDao(dbProvider.get());
  }

  public static AppModule_ProvideServerGroupDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideServerGroupDaoFactory(dbProvider);
  }

  public static ServerGroupDao provideServerGroupDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideServerGroupDao(db));
  }
}
