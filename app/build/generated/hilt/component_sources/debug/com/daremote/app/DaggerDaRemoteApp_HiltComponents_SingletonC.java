package com.daremote.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import com.daremote.app.core.data.repository.AlertRepositoryImpl;
import com.daremote.app.core.data.repository.ForwardingRepositoryImpl;
import com.daremote.app.core.data.repository.ServerRepositoryImpl;
import com.daremote.app.core.data.repository.SnippetRepositoryImpl;
import com.daremote.app.core.data.repository.SshConnectionRepositoryImpl;
import com.daremote.app.core.data.ssh.SftpClient;
import com.daremote.app.core.data.ssh.SshCommandExecutor;
import com.daremote.app.core.data.ssh.SshSessionManager;
import com.daremote.app.core.database.AppDatabase;
import com.daremote.app.core.database.dao.AlertRuleDao;
import com.daremote.app.core.database.dao.ForwardingRuleDao;
import com.daremote.app.core.database.dao.ServerDao;
import com.daremote.app.core.database.dao.ServerGroupDao;
import com.daremote.app.core.database.dao.SnippetDao;
import com.daremote.app.core.di.AppModule_ProvideAlertRuleDaoFactory;
import com.daremote.app.core.di.AppModule_ProvideDatabaseFactory;
import com.daremote.app.core.di.AppModule_ProvideForwardingRuleDaoFactory;
import com.daremote.app.core.di.AppModule_ProvideServerDaoFactory;
import com.daremote.app.core.di.AppModule_ProvideServerGroupDaoFactory;
import com.daremote.app.core.di.AppModule_ProvideSnippetDaoFactory;
import com.daremote.app.core.di.SshModule_ProvideSshSessionManagerFactory;
import com.daremote.app.core.security.CredentialManager;
import com.daremote.app.core.security.SshKeyManager;
import com.daremote.app.core.service.SshConnectionService;
import com.daremote.app.core.service.SshConnectionService_MembersInjector;
import com.daremote.app.core.service.TunnelManager;
import com.daremote.app.feature.alerts.AlertConfigViewModel;
import com.daremote.app.feature.alerts.AlertConfigViewModel_HiltModules;
import com.daremote.app.feature.alerts.AlertConfigViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.alerts.AlertConfigViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.connections.AddEditServerViewModel;
import com.daremote.app.feature.connections.AddEditServerViewModel_HiltModules;
import com.daremote.app.feature.connections.AddEditServerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.connections.AddEditServerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.connections.ConnectionListViewModel;
import com.daremote.app.feature.connections.ConnectionListViewModel_HiltModules;
import com.daremote.app.feature.connections.ConnectionListViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.connections.ConnectionListViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.docker.ContainerDetailViewModel;
import com.daremote.app.feature.docker.ContainerDetailViewModel_HiltModules;
import com.daremote.app.feature.docker.ContainerDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.docker.ContainerDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.docker.DockerListViewModel;
import com.daremote.app.feature.docker.DockerListViewModel_HiltModules;
import com.daremote.app.feature.docker.DockerListViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.docker.DockerListViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.filemanager.FileManagerViewModel;
import com.daremote.app.feature.filemanager.FileManagerViewModel_HiltModules;
import com.daremote.app.feature.filemanager.FileManagerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.filemanager.FileManagerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.forwarding.AddEditForwardingViewModel;
import com.daremote.app.feature.forwarding.AddEditForwardingViewModel_HiltModules;
import com.daremote.app.feature.forwarding.AddEditForwardingViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.forwarding.AddEditForwardingViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.forwarding.ForwardingListViewModel;
import com.daremote.app.feature.forwarding.ForwardingListViewModel_HiltModules;
import com.daremote.app.feature.forwarding.ForwardingListViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.forwarding.ForwardingListViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.monitoring.DashboardViewModel;
import com.daremote.app.feature.monitoring.DashboardViewModel_HiltModules;
import com.daremote.app.feature.monitoring.DashboardViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.monitoring.DashboardViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.monitoring.ProcessListViewModel;
import com.daremote.app.feature.monitoring.ProcessListViewModel_HiltModules;
import com.daremote.app.feature.monitoring.ProcessListViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.monitoring.ProcessListViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.security.SshKeyManagerViewModel;
import com.daremote.app.feature.security.SshKeyManagerViewModel_HiltModules;
import com.daremote.app.feature.security.SshKeyManagerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.security.SshKeyManagerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.settings.SettingsViewModel;
import com.daremote.app.feature.settings.SettingsViewModel_HiltModules;
import com.daremote.app.feature.settings.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.settings.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.snippets.AddEditSnippetViewModel;
import com.daremote.app.feature.snippets.AddEditSnippetViewModel_HiltModules;
import com.daremote.app.feature.snippets.AddEditSnippetViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.snippets.AddEditSnippetViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.snippets.SnippetListViewModel;
import com.daremote.app.feature.snippets.SnippetListViewModel_HiltModules;
import com.daremote.app.feature.snippets.SnippetListViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.snippets.SnippetListViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.daremote.app.feature.terminal.TerminalViewModel;
import com.daremote.app.feature.terminal.TerminalViewModel_HiltModules;
import com.daremote.app.feature.terminal.TerminalViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.daremote.app.feature.terminal.TerminalViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerDaRemoteApp_HiltComponents_SingletonC {
  private DaggerDaRemoteApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public DaRemoteApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements DaRemoteApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public DaRemoteApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements DaRemoteApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public DaRemoteApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements DaRemoteApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public DaRemoteApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements DaRemoteApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public DaRemoteApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements DaRemoteApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public DaRemoteApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements DaRemoteApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public DaRemoteApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements DaRemoteApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public DaRemoteApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends DaRemoteApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends DaRemoteApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends DaRemoteApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends DaRemoteApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(15).put(AddEditForwardingViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AddEditForwardingViewModel_HiltModules.KeyModule.provide()).put(AddEditServerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AddEditServerViewModel_HiltModules.KeyModule.provide()).put(AddEditSnippetViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AddEditSnippetViewModel_HiltModules.KeyModule.provide()).put(AlertConfigViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AlertConfigViewModel_HiltModules.KeyModule.provide()).put(ConnectionListViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ConnectionListViewModel_HiltModules.KeyModule.provide()).put(ContainerDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ContainerDetailViewModel_HiltModules.KeyModule.provide()).put(DashboardViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DashboardViewModel_HiltModules.KeyModule.provide()).put(DockerListViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DockerListViewModel_HiltModules.KeyModule.provide()).put(FileManagerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, FileManagerViewModel_HiltModules.KeyModule.provide()).put(ForwardingListViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ForwardingListViewModel_HiltModules.KeyModule.provide()).put(ProcessListViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ProcessListViewModel_HiltModules.KeyModule.provide()).put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide()).put(SnippetListViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SnippetListViewModel_HiltModules.KeyModule.provide()).put(SshKeyManagerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SshKeyManagerViewModel_HiltModules.KeyModule.provide()).put(TerminalViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TerminalViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends DaRemoteApp_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AddEditForwardingViewModel> addEditForwardingViewModelProvider;

    private Provider<AddEditServerViewModel> addEditServerViewModelProvider;

    private Provider<AddEditSnippetViewModel> addEditSnippetViewModelProvider;

    private Provider<AlertConfigViewModel> alertConfigViewModelProvider;

    private Provider<ConnectionListViewModel> connectionListViewModelProvider;

    private Provider<ContainerDetailViewModel> containerDetailViewModelProvider;

    private Provider<DashboardViewModel> dashboardViewModelProvider;

    private Provider<DockerListViewModel> dockerListViewModelProvider;

    private Provider<FileManagerViewModel> fileManagerViewModelProvider;

    private Provider<ForwardingListViewModel> forwardingListViewModelProvider;

    private Provider<ProcessListViewModel> processListViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<SnippetListViewModel> snippetListViewModelProvider;

    private Provider<SshKeyManagerViewModel> sshKeyManagerViewModelProvider;

    private Provider<TerminalViewModel> terminalViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.addEditForwardingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.addEditServerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.addEditSnippetViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.alertConfigViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.connectionListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.containerDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.dashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.dockerListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.fileManagerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.forwardingListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.processListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.snippetListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.sshKeyManagerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.terminalViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(15).put(AddEditForwardingViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) addEditForwardingViewModelProvider)).put(AddEditServerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) addEditServerViewModelProvider)).put(AddEditSnippetViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) addEditSnippetViewModelProvider)).put(AlertConfigViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) alertConfigViewModelProvider)).put(ConnectionListViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) connectionListViewModelProvider)).put(ContainerDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) containerDetailViewModelProvider)).put(DashboardViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) dashboardViewModelProvider)).put(DockerListViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) dockerListViewModelProvider)).put(FileManagerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) fileManagerViewModelProvider)).put(ForwardingListViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) forwardingListViewModelProvider)).put(ProcessListViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) processListViewModelProvider)).put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) settingsViewModelProvider)).put(SnippetListViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) snippetListViewModelProvider)).put(SshKeyManagerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) sshKeyManagerViewModelProvider)).put(TerminalViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) terminalViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.daremote.app.feature.forwarding.AddEditForwardingViewModel 
          return (T) new AddEditForwardingViewModel(singletonCImpl.forwardingRepositoryImplProvider.get(), singletonCImpl.serverRepositoryImplProvider.get(), viewModelCImpl.savedStateHandle);

          case 1: // com.daremote.app.feature.connections.AddEditServerViewModel 
          return (T) new AddEditServerViewModel(singletonCImpl.serverRepositoryImplProvider.get(), viewModelCImpl.savedStateHandle);

          case 2: // com.daremote.app.feature.snippets.AddEditSnippetViewModel 
          return (T) new AddEditSnippetViewModel(singletonCImpl.snippetRepositoryImplProvider.get(), viewModelCImpl.savedStateHandle);

          case 3: // com.daremote.app.feature.alerts.AlertConfigViewModel 
          return (T) new AlertConfigViewModel(singletonCImpl.alertRepositoryImplProvider.get(), singletonCImpl.serverRepositoryImplProvider.get());

          case 4: // com.daremote.app.feature.connections.ConnectionListViewModel 
          return (T) new ConnectionListViewModel(singletonCImpl.serverRepositoryImplProvider.get(), singletonCImpl.sshConnectionRepositoryImplProvider.get());

          case 5: // com.daremote.app.feature.docker.ContainerDetailViewModel 
          return (T) new ContainerDetailViewModel(singletonCImpl.sshCommandExecutorProvider.get(), viewModelCImpl.savedStateHandle);

          case 6: // com.daremote.app.feature.monitoring.DashboardViewModel 
          return (T) new DashboardViewModel(singletonCImpl.sshCommandExecutorProvider.get(), singletonCImpl.serverRepositoryImplProvider.get(), viewModelCImpl.savedStateHandle);

          case 7: // com.daremote.app.feature.docker.DockerListViewModel 
          return (T) new DockerListViewModel(singletonCImpl.sshCommandExecutorProvider.get(), singletonCImpl.serverRepositoryImplProvider.get(), viewModelCImpl.savedStateHandle);

          case 8: // com.daremote.app.feature.filemanager.FileManagerViewModel 
          return (T) new FileManagerViewModel(singletonCImpl.sftpClientProvider.get(), singletonCImpl.serverRepositoryImplProvider.get(), viewModelCImpl.savedStateHandle);

          case 9: // com.daremote.app.feature.forwarding.ForwardingListViewModel 
          return (T) new ForwardingListViewModel(singletonCImpl.forwardingRepositoryImplProvider.get(), singletonCImpl.tunnelManagerProvider.get());

          case 10: // com.daremote.app.feature.monitoring.ProcessListViewModel 
          return (T) new ProcessListViewModel(singletonCImpl.sshCommandExecutorProvider.get(), viewModelCImpl.savedStateHandle);

          case 11: // com.daremote.app.feature.settings.SettingsViewModel 
          return (T) new SettingsViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 12: // com.daremote.app.feature.snippets.SnippetListViewModel 
          return (T) new SnippetListViewModel(singletonCImpl.snippetRepositoryImplProvider.get(), singletonCImpl.serverRepositoryImplProvider.get(), singletonCImpl.sshCommandExecutorProvider.get());

          case 13: // com.daremote.app.feature.security.SshKeyManagerViewModel 
          return (T) new SshKeyManagerViewModel(singletonCImpl.sshKeyManagerProvider.get());

          case 14: // com.daremote.app.feature.terminal.TerminalViewModel 
          return (T) new TerminalViewModel(singletonCImpl.provideSshSessionManagerProvider.get(), singletonCImpl.serverRepositoryImplProvider.get(), viewModelCImpl.savedStateHandle);

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends DaRemoteApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends DaRemoteApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectSshConnectionService(SshConnectionService sshConnectionService) {
      injectSshConnectionService2(sshConnectionService);
    }

    private SshConnectionService injectSshConnectionService2(SshConnectionService instance) {
      SshConnectionService_MembersInjector.injectSessionManager(instance, singletonCImpl.provideSshSessionManagerProvider.get());
      SshConnectionService_MembersInjector.injectTunnelManager(instance, singletonCImpl.tunnelManagerProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends DaRemoteApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AppDatabase> provideDatabaseProvider;

    private Provider<ForwardingRepositoryImpl> forwardingRepositoryImplProvider;

    private Provider<CredentialManager> credentialManagerProvider;

    private Provider<ServerRepositoryImpl> serverRepositoryImplProvider;

    private Provider<SnippetRepositoryImpl> snippetRepositoryImplProvider;

    private Provider<AlertRepositoryImpl> alertRepositoryImplProvider;

    private Provider<SshKeyManager> sshKeyManagerProvider;

    private Provider<SshSessionManager> provideSshSessionManagerProvider;

    private Provider<SshConnectionRepositoryImpl> sshConnectionRepositoryImplProvider;

    private Provider<SshCommandExecutor> sshCommandExecutorProvider;

    private Provider<SftpClient> sftpClientProvider;

    private Provider<TunnelManager> tunnelManagerProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(Collections.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>emptyMap());
    }

    private ForwardingRuleDao forwardingRuleDao() {
      return AppModule_ProvideForwardingRuleDaoFactory.provideForwardingRuleDao(provideDatabaseProvider.get());
    }

    private ServerDao serverDao() {
      return AppModule_ProvideServerDaoFactory.provideServerDao(provideDatabaseProvider.get());
    }

    private ServerGroupDao serverGroupDao() {
      return AppModule_ProvideServerGroupDaoFactory.provideServerGroupDao(provideDatabaseProvider.get());
    }

    private SnippetDao snippetDao() {
      return AppModule_ProvideSnippetDaoFactory.provideSnippetDao(provideDatabaseProvider.get());
    }

    private AlertRuleDao alertRuleDao() {
      return AppModule_ProvideAlertRuleDaoFactory.provideAlertRuleDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 1));
      this.forwardingRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ForwardingRepositoryImpl>(singletonCImpl, 0));
      this.credentialManagerProvider = DoubleCheck.provider(new SwitchingProvider<CredentialManager>(singletonCImpl, 3));
      this.serverRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ServerRepositoryImpl>(singletonCImpl, 2));
      this.snippetRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<SnippetRepositoryImpl>(singletonCImpl, 4));
      this.alertRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<AlertRepositoryImpl>(singletonCImpl, 5));
      this.sshKeyManagerProvider = DoubleCheck.provider(new SwitchingProvider<SshKeyManager>(singletonCImpl, 8));
      this.provideSshSessionManagerProvider = DoubleCheck.provider(new SwitchingProvider<SshSessionManager>(singletonCImpl, 7));
      this.sshConnectionRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<SshConnectionRepositoryImpl>(singletonCImpl, 6));
      this.sshCommandExecutorProvider = DoubleCheck.provider(new SwitchingProvider<SshCommandExecutor>(singletonCImpl, 9));
      this.sftpClientProvider = DoubleCheck.provider(new SwitchingProvider<SftpClient>(singletonCImpl, 10));
      this.tunnelManagerProvider = DoubleCheck.provider(new SwitchingProvider<TunnelManager>(singletonCImpl, 11));
    }

    @Override
    public void injectDaRemoteApp(DaRemoteApp daRemoteApp) {
      injectDaRemoteApp2(daRemoteApp);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private DaRemoteApp injectDaRemoteApp2(DaRemoteApp instance) {
      DaRemoteApp_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.daremote.app.core.data.repository.ForwardingRepositoryImpl 
          return (T) new ForwardingRepositoryImpl(singletonCImpl.forwardingRuleDao());

          case 1: // com.daremote.app.core.database.AppDatabase 
          return (T) AppModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.daremote.app.core.data.repository.ServerRepositoryImpl 
          return (T) new ServerRepositoryImpl(singletonCImpl.serverDao(), singletonCImpl.serverGroupDao(), singletonCImpl.credentialManagerProvider.get());

          case 3: // com.daremote.app.core.security.CredentialManager 
          return (T) new CredentialManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.daremote.app.core.data.repository.SnippetRepositoryImpl 
          return (T) new SnippetRepositoryImpl(singletonCImpl.snippetDao());

          case 5: // com.daremote.app.core.data.repository.AlertRepositoryImpl 
          return (T) new AlertRepositoryImpl(singletonCImpl.alertRuleDao());

          case 6: // com.daremote.app.core.data.repository.SshConnectionRepositoryImpl 
          return (T) new SshConnectionRepositoryImpl(singletonCImpl.provideSshSessionManagerProvider.get());

          case 7: // com.daremote.app.core.data.ssh.SshSessionManager 
          return (T) SshModule_ProvideSshSessionManagerFactory.provideSshSessionManager(singletonCImpl.credentialManagerProvider.get(), singletonCImpl.sshKeyManagerProvider.get());

          case 8: // com.daremote.app.core.security.SshKeyManager 
          return (T) new SshKeyManager(singletonCImpl.credentialManagerProvider.get());

          case 9: // com.daremote.app.core.data.ssh.SshCommandExecutor 
          return (T) new SshCommandExecutor(singletonCImpl.provideSshSessionManagerProvider.get());

          case 10: // com.daremote.app.core.data.ssh.SftpClient 
          return (T) new SftpClient(singletonCImpl.provideSshSessionManagerProvider.get());

          case 11: // com.daremote.app.core.service.TunnelManager 
          return (T) new TunnelManager(singletonCImpl.provideSshSessionManagerProvider.get(), singletonCImpl.forwardingRepositoryImplProvider.get(), singletonCImpl.serverRepositoryImplProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
