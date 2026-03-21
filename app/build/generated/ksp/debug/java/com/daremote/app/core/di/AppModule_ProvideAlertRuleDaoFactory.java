package com.daremote.app.core.di;

import com.daremote.app.core.database.AppDatabase;
import com.daremote.app.core.database.dao.AlertRuleDao;
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
public final class AppModule_ProvideAlertRuleDaoFactory implements Factory<AlertRuleDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideAlertRuleDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AlertRuleDao get() {
    return provideAlertRuleDao(dbProvider.get());
  }

  public static AppModule_ProvideAlertRuleDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideAlertRuleDaoFactory(dbProvider);
  }

  public static AlertRuleDao provideAlertRuleDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAlertRuleDao(db));
  }
}
