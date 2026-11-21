package com.shuaiqiu.fuckets100.feature.read

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shuaiqiu.fuckets100.ActivationMode
import com.shuaiqiu.fuckets100.CloudHomeworkState
import com.shuaiqiu.fuckets100.ETS100AnswerReader
import com.shuaiqiu.fuckets100.ETS100ApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * 读取页 ViewModel（仅云端模式）。
 *
 * 本地三种读取模式（Shizuku / Root / 漏洞直读）与 FE 账户系统已移除，
 * 本 ViewModel 只负责云端作业列表加载、下载与解析，全部委托给 [ReadCloudController]。
 */
@HiltViewModel
class ReadViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReadUiState())
    val uiState: StateFlow<ReadUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ReadEffect>(Channel.BUFFERED)
    val effects: Flow<ReadEffect> = _effects.receiveAsFlow()

    private val cloudController: ReadCloudController by lazy {
        ReadCloudController(
            context = context,
            uiStateFlow = _uiState,
            effectsFlow = _effects,
            scope = viewModelScope,
        )
    }

    init {
        restoreCloudSnapshotIfNeeded()
    }

    fun requestReload() {
        _uiState.update { it.copy(reloadTrigger = it.reloadTrigger + 1, errorMessage = null) }
    }

    // ==================== 云端模式（委托给 ReadCloudController） ====================

    fun selectCloudCategory(category: String) = cloudController.selectCloudCategory(category)

    fun selectCloudHomeworkStatus(status: String) = cloudController.selectCloudHomeworkStatus(status)

    fun restoreCloudSnapshotIfNeeded() = cloudController.restoreCloudSnapshotIfNeeded()

    fun updateCloudDownloadProgress(
        cacheKey: String,
        progress: CloudHomeworkState.DownloadProgress?,
    ) = cloudController.updateCloudDownloadProgress(cacheKey, progress)

    suspend fun loadCloudHomeworkList(
        status: String = _uiState.value.selectedCloudHomeworkStatus,
        accountOnline: Boolean,
    ) = cloudController.loadCloudHomeworkList(status, accountOnline)

    fun requestHomeworkDownload(
        homeworkInfo: ETS100ApiClient.HomeworkInfo,
        status: String,
        accountOnline: Boolean,
    ) = cloudController.requestHomeworkDownload(homeworkInfo, status, accountOnline)

    suspend fun downloadAndParseHomework(
        homeworkInfo: ETS100ApiClient.HomeworkInfo,
        status: String = _uiState.value.selectedCloudHomeworkStatus,
        accountOnline: Boolean,
    ) = cloudController.downloadAndParseHomework(homeworkInfo, status, accountOnline)

    fun clearCloudHomeworkCache() = cloudController.clearCloudHomeworkCache()
}
