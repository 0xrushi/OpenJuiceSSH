package com.daremote.app.feature.filemanager;

import androidx.lifecycle.SavedStateHandle;
import com.daremote.app.core.data.ssh.SftpClient;
import com.daremote.app.core.domain.repository.ServerRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class FileManagerViewModel_Factory implements Factory<FileManagerViewModel> {
  private final Provider<SftpClient> sftpClientProvider;

  private final Provider<ServerRepository> serverRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public FileManagerViewModel_Factory(Provider<SftpClient> sftpClientProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.sftpClientProvider = sftpClientProvider;
    this.serverRepositoryProvider = serverRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public FileManagerViewModel get() {
    return newInstance(sftpClientProvider.get(), serverRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static FileManagerViewModel_Factory create(Provider<SftpClient> sftpClientProvider,
      Provider<ServerRepository> serverRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new FileManagerViewModel_Factory(sftpClientProvider, serverRepositoryProvider, savedStateHandleProvider);
  }

  public static FileManagerViewModel newInstance(SftpClient sftpClient,
      ServerRepository serverRepository, SavedStateHandle savedStateHandle) {
    return new FileManagerViewModel(sftpClient, serverRepository, savedStateHandle);
  }
}
