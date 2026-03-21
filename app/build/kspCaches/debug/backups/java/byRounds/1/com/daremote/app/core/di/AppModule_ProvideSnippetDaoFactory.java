package com.daremote.app.core.di;

import com.daremote.app.core.database.AppDatabase;
import com.daremote.app.core.database.dao.SnippetDao;
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
public final class AppModule_ProvideSnippetDaoFactory implements Factory<SnippetDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideSnippetDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SnippetDao get() {
    return provideSnippetDao(dbProvider.get());
  }

  public static AppModule_ProvideSnippetDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideSnippetDaoFactory(dbProvider);
  }

  public static SnippetDao provideSnippetDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSnippetDao(db));
  }
}
