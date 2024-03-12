package com.bera.telegram_drive

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bera.telegram_drive.Constants.BOT_TOKEN
import com.bera.telegram_drive.ui.theme.TelegramdriveTheme
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.document
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.network.fold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bot = bot {
            token = BOT_TOKEN
            dispatch {
                text {
                    bot.sendMessage(ChatId.fromChannelUsername("owndrivechannel"), "hi")
                    val sentByBot = message.from?.username == "owndrive"
                    if (sentByBot) {
                        Log.d("log", "Message from bot: ${message.text}")
                    }
                }
                document {
                    val sentByBot = message.from?.username == "owndrive"
                    if (sentByBot) {
                        val documentFile = message.document
                        Log.d("log", "Bot sent a document: ${documentFile?.fileName}")
                    }
                }
            }
        }
        bot.startPolling()

        setContent {
            TelegramdriveTheme {
                val scope = rememberCoroutineScope()
                val permissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        if (!isGranted) {
                            Toast.makeText(
                                applicationContext,
                                "Permissions denied!",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                    }
                )
                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_DENIED
                    ) {
                        permissionsLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        permissionsLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val launcher =
                        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                            uri?.let {
                                val parcelFileDescriptor =
                                    contentResolver.openFileDescriptor(uri, "r", null)
                                val inputStream =
                                    FileInputStream(parcelFileDescriptor?.fileDescriptor)
                                val fileName = uri.lastPathSegment!!
                                val file = File.createTempFile(fileName, ".pdf")
                                val outputStream = FileOutputStream(file)
                                inputStream.copyTo(outputStream)
                                scope.launch(Dispatchers.IO) {
                                    bot.sendDocument(
                                        chatId = ChatId.fromChannelUsername("owndrivechannel"),
                                        document = TelegramFile.ByFile(file),
                                        disableContentTypeDetection = false,
                                        mimeType = "application/pdf"
                                    ).fold(
                                        {
                                            Log.d("SendDocument", "Success: ${it.toString()}")
                                            val fileId = it?.result?.document?.fileId
                                            if (fileId != null) {
                                                // Save the file ID in a local file or a database
                                                Log.d("SendDocument", "Saved file ID: $fileId")
                                                Log.d("SendDocument", "Saved file: $file")
                                                bot.downloadFileBytes(fileId).also { byteArray ->
                                                    val fileOutputStream = FileOutputStream(
                                                        File(
                                                            getExternalFilesDir(null),
                                                            "$fileName.pdf"
                                                        )
                                                    )
                                                    fileOutputStream.write(byteArray)
                                                    fileOutputStream.close()
                                                    openPDF(fileName)
                                                }
                                            }
                                        },
                                        {
                                            Log.e("SendDocument", "Error: $it")
                                        }
                                    )
                                }
                                parcelFileDescriptor?.close()
                            }
                        }
                    Column {
                        Button(onClick = { launcher.launch(arrayOf("application/pdf")) }) {
                            Text(text = "Send")
                        }
                    }
                }
            }
        }
    }

    private fun openPDF(fileName: String) {

        // Get the File location and file name.
        val file = File(getExternalFilesDir(null), "$fileName.pdf")
        Log.d("pdfFIle", "" + file)

        // Get the URI Path of file.
        val uriPdfPath =
            FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
        Log.d("pdfPath", "" + uriPdfPath)

        // Start Intent to View PDF from the Installed Applications.
        val pdfOpenIntent = Intent(Intent.ACTION_VIEW)
        pdfOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        pdfOpenIntent.clipData = ClipData.newRawUri("", uriPdfPath)
        pdfOpenIntent.setDataAndType(uriPdfPath, "application/pdf")
        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            startActivity(pdfOpenIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(this, "There is no app to load corresponding PDF", Toast.LENGTH_LONG)
                .show()
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TelegramdriveTheme {
        Greeting("Android")
    }
}