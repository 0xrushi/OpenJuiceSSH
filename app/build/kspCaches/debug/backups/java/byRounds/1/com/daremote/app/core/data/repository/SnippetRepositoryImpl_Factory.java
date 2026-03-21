package com.daremote.app.core.data.repository;

import com.daremote.app.core.database.dao.SnippetDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SnippetRepositoryImpl_Factory implements Factory<SnippetRepositoryImpl> {
  private final Provider<SnippetDao> daoProvider;

  public SnippetRepositoryImpl_Factory(Provider<SnippetDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public SnippetRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static SnippetRepositoryImpl_Factory create(Provider<SnippetDao> daoProvider) {
    return new SnippetRepositoryImpl_Factory(daoProvider);
  }

  public static SnippetRepositoryImpl newInstance(SnippetDao dao) {
    return new SnippetRepositoryImpl(dao);
  }
}
