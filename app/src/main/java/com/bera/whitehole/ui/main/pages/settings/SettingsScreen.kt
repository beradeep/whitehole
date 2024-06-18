package com.bera.whitehole.ui.main.pages.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bera.whitehole.R
import com.bera.whitehole.data.localdb.Preferences
import com.bera.whitehole.data.localdb.backup.BackupHelper
import com.bera.whitehole.workers.WorkModule
import com.posthog.PostHog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val createPhotosBackupFile =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BackupHelper.JSON_MIME)) {
            scope.launch(Dispatchers.IO) {
                BackupHelper.exportPhotos(it ?: return@launch, context)
            }
        }
    val importPhotosBackupFile =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            scope.launch(Dispatchers.IO) {
                BackupHelper.importPhotos(it ?: return@launch, context)
            }
        }
    var isAutoPhotoBackupEnabled: Boolean by remember {
        mutableStateOf(
            Preferences.getBoolean(Preferences.isPeriodicPhotoBackupEnabeld, false)
        )
    }
    LaunchedEffect(key1 = isAutoPhotoBackupEnabled, Dispatchers.IO) {
        Preferences.edit { putBoolean(Preferences.isPeriodicPhotoBackupEnabeld, isAutoPhotoBackupEnabled) }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        item {
            Text(text = "Backup", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            SettingsSwitchCard(
                text = "Auto periodic backup",
                icon = painterResource(id = R.drawable.cloud_arrow_up_solid),
                isChecked = isAutoPhotoBackupEnabled
            ) {
                isAutoPhotoBackupEnabled = !isAutoPhotoBackupEnabled
                if (isAutoPhotoBackupEnabled) {
                    WorkModule.backupPeriodic()
                    PostHog.capture(event = "enabled-periodic-backup")
                }
                else {
                    WorkModule.cancelPeriodicBackupWorker()
                    PostHog.capture(event = "disabled-periodic-backup")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            SettingsCard(
                settingHeaderText = "Restore all from cloud",
                painterResourceID = R.drawable.cloud_arrow_down_solid
            ) {
                PostHog.capture(event = "export-db")
                WorkModule.restoreAll()
            }
            Spacer(modifier = Modifier.height(6.dp))
            SettingsCard(
                settingHeaderText = "Export backup database",
                painterResourceID = R.drawable.file_export_solid
            ) {
                createPhotosBackupFile.launch("whitehole_photos_backup.json")
            }
            Spacer(modifier = Modifier.height(6.dp))
            SettingsCard(
                settingHeaderText = "Import backup database",
                painterResourceID = R.drawable.file_import_solid
            ) {
                PostHog.capture("import-db")
                importPhotosBackupFile.launch(arrayOf(BackupHelper.JSON_MIME))
            }
        }
    }
}