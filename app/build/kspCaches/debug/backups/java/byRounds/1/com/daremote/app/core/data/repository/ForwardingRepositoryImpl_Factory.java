package com.daremote.app.core.data.repository;

import com.daremote.app.core.database.dao.ForwardingRuleDao;
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
public final class ForwardingRepositoryImpl_Factory implements Factory<ForwardingRepositoryImpl> {
  private final Provider<ForwardingRuleDao> daoProvider;

  public ForwardingRepositoryImpl_Factory(Provider<ForwardingRuleDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public ForwardingRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static ForwardingRepositoryImpl_Factory create(Provider<ForwardingRuleDao> daoProvider) {
    return new ForwardingRepositoryImpl_Factory(daoProvider);
  }

  public static ForwardingRepositoryImpl newInstance(ForwardingRuleDao dao) {
    return new ForwardingRepositoryImpl(dao);
  }
}
