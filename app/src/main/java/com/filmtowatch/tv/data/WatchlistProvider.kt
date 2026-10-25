package com.filmtowatch.tv.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import kotlinx.serialization.json.Json
import java.io.File

/**
 * The watchlist, read-only, for Free My TV's "My List" tab: content://com.filmtowatch.tv.watchlist/films, newest
 * first. Only Free My TV gets an answer; it opens a movie with filmtowatch://film/<imdb id> (MainActivity).
 */
class WatchlistProvider : ContentProvider() {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    override fun onCreate() = true

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        if (callingPackage != FREE_MY_TV) return null
        val file = File(context?.filesDir ?: return null, "store_v2.json")
        val films = runCatching { json.decodeFromString(StoreData.serializer(), file.readText()).watchlist }.getOrDefault(emptyList())
        val cursor = MatrixCursor(COLUMNS)
        for (film in films) {
            cursor.addRow(arrayOf(film.id, film.title, film.year, film.genres.firstOrNull(), film.runtimeLabel,
                Images.backgroundLarge(film.background), Images.poster(film.poster)))
        }
        return cursor
    }

    override fun getType(uri: Uri) = "vnd.android.cursor.dir/vnd.com.filmtowatch.tv.film"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0

    private companion object {
        const val FREE_MY_TV = "io.freemytv"
        val COLUMNS = arrayOf("id", "title", "year", "genre", "runtime", "background", "poster")
    }
}
