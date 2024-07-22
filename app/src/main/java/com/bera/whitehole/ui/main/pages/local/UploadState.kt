package com.bera.whitehole.ui.main.pages.local

enum class UploadState {
    ENQUEUED,
    BLOCKED,
    CHECKING,
    UPLOADING,
    UPLOADED,
    NOT_UPLOADED,
    FAILED,
}