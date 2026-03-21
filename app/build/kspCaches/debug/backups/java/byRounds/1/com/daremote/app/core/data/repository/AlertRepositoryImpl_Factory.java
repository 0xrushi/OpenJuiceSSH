package com.daremote.app.core.data.repository;

import com.daremote.app.core.database.dao.AlertRuleDao;
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
public final class AlertRepositoryImpl_Factory implements Factory<AlertRepositoryImpl> {
  private final Provider<AlertRuleDao> daoProvider;

  public AlertRepositoryImpl_Factory(Provider<AlertRuleDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public AlertRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static AlertRepositoryImpl_Factory create(Provider<AlertRuleDao> daoProvider) {
    return new AlertRepositoryImpl_Factory(daoProvider);
  }

  public static AlertRepositoryImpl newInstance(AlertRuleDao dao) {
    return new AlertRepositoryImpl(dao);
  }
}
