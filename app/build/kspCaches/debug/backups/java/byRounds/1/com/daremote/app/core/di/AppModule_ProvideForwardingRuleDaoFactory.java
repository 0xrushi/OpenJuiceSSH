package com.daremote.app.core.di;

import com.daremote.app.core.database.AppDatabase;
import com.daremote.app.core.database.dao.ForwardingRuleDao;
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
public final class AppModule_ProvideForwardingRuleDaoFactory implements Factory<ForwardingRuleDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideForwardingRuleDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ForwardingRuleDao get() {
    return provideForwardingRuleDao(dbProvider.get());
  }

  public static AppModule_ProvideForwardingRuleDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideForwardingRuleDaoFactory(dbProvider);
  }

  public static ForwardingRuleDao provideForwardingRuleDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideForwardingRuleDao(db));
  }
}
