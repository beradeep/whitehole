package com.bera.whitehole.data.localphotosource

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bera.whitehole.data.models.PhotoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocalPhotoSource : PagingSource<Int, PhotoModel.LocalPhotoModel>() {

    private lateinit var contentResolver: ContentResolver
    private val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PhotoModel.LocalPhotoModel> {

        return withContext(Dispatchers.IO) {
            val page = params.key ?: 1
            val pageSize = params.loadSize
            val limit = pageSize
            val offset = (page - 1) * pageSize
            val pagePhotoList = mutableListOf<PhotoModel.LocalPhotoModel>()
            val query = Query.PhotoQuery().copy(
                projection = arrayOf(
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.MIME_TYPE,
                    MediaStore.MediaColumns.DATE_MODIFIED,
                ),
                bundle = Query.PhotoQuery().bundle?.apply {
                    putInt(
                        ContentResolver.QUERY_ARG_OFFSET,
                        offset
                    )
                    putInt(
                        ContentResolver.QUERY_ARG_LIMIT,
                        limit
                    )
                }
            )
            return@withContext try {
                val cursor = contentResolver.query(
                    imageCollection,
                    query.projection,
                    query.bundle,
                    null
                )
                cursor?.use { _ ->
                    while (cursor.moveToNext()) {
                        try {
                            pagePhotoList.add(cursor.getPhotoFromCursor())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    pagePhotoList
                }
                val nextKey = if (pagePhotoList.size < pageSize) null else page + 1
                LoadResult.Page(pagePhotoList, null, nextKey)
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PhotoModel.LocalPhotoModel>): Int? {
        return state.anchorPosition
    }

    fun create(applicationContext: Context) {
        contentResolver = applicationContext.contentResolver
    }
}