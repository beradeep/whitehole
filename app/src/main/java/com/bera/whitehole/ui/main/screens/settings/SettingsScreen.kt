package com.bera.whitehole.ui.main.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.MoveToInbox
import androidx.compose.material.icons.rounded.Outbox
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.NetworkType
import com.bera.whitehole.R
import com.bera.whitehole.data.localdb.DbHolder
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.data.localdb.backup.BackupHelper
import com.bera.whitehole.ui.components.IconTextCard
import com.bera.whitehole.ui.components.IconTextSwitchCard
import com.bera.whitehole.ui.components.ListCategoryHeader
import com.bera.whitehole.ui.components.ListDialogCard
import com.bera.whitehole.utils.toastFromMainThread
import com.bera.whitehole.workers.WorkModule
import com.posthog.PostHog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val exportBackupFileLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(BackupHelper.JSON_MIME)
        ) {
            scope.launch(Dispatchers.IO) {
                BackupHelper.exportDatabase(it ?: return@launch, context)
            }
        }
    val autoExportBackupFileLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(BackupHelper.JSON_MIME)
        ) {
            it?.let { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                Preferences.edit {
                    putString(
                        Preferences.autoExportDatabseLocation,
                        uri.toString()
                    )
                }
                WorkModule.PeriodicDbExport.enqueue()
                scope.launch {
                    context.toastFromMainThread(
                        context.getString(R.string.periodic_database_export_enabled)
                    )
                }
            }
        }
    val importPhotosBackupFile =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            scope.launch(Dispatchers.IO) {
                BackupHelper.importDatabase(it ?: return@launch, context)
            }
        }

    var isAutoPhotoBackupEnabled: Boolean by remember {
        mutableStateOf(
            Preferences.getBoolean(Preferences.isAutoBackupEnabledKey, false)
        )
    }
    var isAutoExportDatabaseEnabled: Boolean by remember {
        mutableStateOf(
            Preferences.getBoolean(Preferences.isAutoExportDatabaseEnabledKey, false)
        )
    }
    val photosNotOnDeviceCount by
        DbHolder.database.remotePhotoDao().getNotOnDeviceCountFlow().collectAsStateWithLifecycle(
            initialValue = 0
        )

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        ListCategoryHeader(title = "Backup & Restore")
        Spacer(modifier = Modifier.height(6.dp))

        IconTextSwitchCard(
            text = stringResource(R.string.auto_periodic_backup),
            icon = Icons.Rounded.CloudSync,
            isChecked = isAutoPhotoBackupEnabled
        ) {
            isAutoPhotoBackupEnabled = !isAutoPhotoBackupEnabled
            Preferences.edit {
                putBoolean(
                    Preferences.isAutoBackupEnabledKey,
                    isAutoPhotoBackupEnabled
                )
            }
            if (isAutoPhotoBackupEnabled) {
                WorkModule.PeriodicBackup.enqueue()
                PostHog.capture(event = context.getString(R.string.enabled_periodic_backup))
                scope.launch {
                    context.toastFromMainThread(context.getString(R.string.periodic_backup_enabled))
                }
            } else {
                WorkModule.PeriodicBackup.cancel()
                PostHog.capture(event = context.getString(R.string.disabled_periodic_backup))
                scope.launch {
                    context.toastFromMainThread(
                        context.getString(R.string.periodic_backup_cancelled)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        val intervals = remember {
            listOf(
                "Daily" to 1,
                "Weekly" to 7,
                "Biweekly" to 14,
                "Monthly" to 30
            )
        }

        val networkTypes = remember {
            listOf(
                "All networks" to NetworkType.CONNECTED,
                "Unmetered" to NetworkType.UNMETERED,
                "Metered" to NetworkType.METERED,
                "Not Roaming" to NetworkType.NOT_ROAMING
            )
        }

        ListDialogCard(
            prefKey = Preferences.autoBackupIntervalKey,
            title = stringResource(R.string.backup_interval),
            icon = Icons.Rounded.AccessTime,
            entries = intervals.map { it.first },
            values = intervals.map { it.second.toString() },
            enabled = isAutoPhotoBackupEnabled,
            defaultValue = Preferences.defaultAutoBackupInterval.toString()
        ) {
            WorkModule.PeriodicBackup.enqueue(forceUpdate = true)
        }
        Spacer(modifier = Modifier.size(6.dp))

        ListDialogCard(
            prefKey = Preferences.autoBackupNetworkTypeKey,
            title = stringResource(R.string.backup_network_type),
            entries = networkTypes.map { it.first },
            values = networkTypes.map { it.second.name },
            enabled = isAutoPhotoBackupEnabled,
            icon = Icons.Rounded.SignalCellularAlt,
            defaultValue = NetworkType.CONNECTED.name
        ) {
            WorkModule.PeriodicBackup.enqueue(forceUpdate = true)
        }
        Spacer(modifier = Modifier.size(6.dp))

        IconTextCard(
            settingHeaderText = stringResource(R.string.restore_all_from_cloud),
            imageVector = Icons.Outlined.CloudDownload,
            settingSummaryText = stringResource(
                R.string.photos_not_found_on_this_device,
                photosNotOnDeviceCount
            )
        ) {
            PostHog.capture(context.getString(R.string.restore_all))
            WorkModule.RestoreMissingFromDevice.enqueue()
            scope.launch {
                context.toastFromMainThread(
                    context.getString(R.string.restoring_task_enqueued_in_the_background)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        Spacer(modifier = Modifier.size(6.dp))
        ListCategoryHeader(title = stringResource(R.string.import_export))
        Spacer(modifier = Modifier.size(6.dp))

        IconTextCard(
            settingHeaderText = stringResource(R.string.import_database),
            imageVector = Icons.Rounded.MoveToInbox
        ) {
            PostHog.capture(context.getString(R.string.import_db))
            importPhotosBackupFile.launch(arrayOf(BackupHelper.JSON_MIME))
        }
        Spacer(modifier = Modifier.height(6.dp))

        IconTextCard(
            settingHeaderText = stringResource(R.string.export_database),
            imageVector = Icons.Rounded.Outbox
        ) {
            PostHog.capture(event = context.getString(R.string.export_db))
            exportBackupFileLauncher.launch(
                context.getString(R.string.whitehole_photos_backup_json)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        IconTextSwitchCard(
            text = stringResource(R.string.auto_export_database),
            icon = Icons.Rounded.AutoMode,
            isChecked = isAutoExportDatabaseEnabled
        ) {
            isAutoExportDatabaseEnabled = !isAutoExportDatabaseEnabled
            Preferences.edit {
                putBoolean(
                    Preferences.isAutoExportDatabaseEnabledKey,
                    isAutoExportDatabaseEnabled
                )
            }
            if (isAutoExportDatabaseEnabled) {
                autoExportBackupFileLauncher.launch(
                    context.getString(R.string.whitehole_auto_photos_backup_json)
                )
                PostHog.capture(event = context.getString(R.string.enabled_periodic_db_export))
            } else {
                WorkModule.PeriodicDbExport.cancel()
                PostHog.capture(event = context.getString(R.string.disabled_periodic_db_export))
                scope.launch {
                    context.toastFromMainThread(
                        context.getString(R.string.periodic_database_export_cancelled)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        ListDialogCard(
            prefKey = Preferences.autoBackupIntervalKey,
            title = stringResource(R.string.auto_export_interval),
            icon = Icons.Rounded.AccessTime,
            entries = intervals.drop(1).map { it.first },
            values = intervals.drop(1).map { it.second.toString() },
            enabled = isAutoPhotoBackupEnabled,
            defaultValue = Preferences.defaultAutoExportDatabaseIntervalKey.toString()
        ) {
            WorkModule.PeriodicDbExport.enqueue(forceUpdate = true)
        }
        Spacer(modifier = Modifier.size(6.dp))
    }
}