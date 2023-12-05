package net.ilsoft.earthquake.provider

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import net.ilsoft.earthquake.room.EarthquakeDatabaseAccessor

class EarthquakeSearchProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        EarthquakeDatabaseAccessor.getInstance(context!!.applicationContext)
        return true
    }

    override fun query(
        p0: Uri,
        p1: Array<out String>?,
        p2: String?,
        p3: Array<out String>?,
        p4: String?
    ): Cursor? {
        if (uriMatcher.match(p0) == SEARCH_SUGGESTIONS) {
            val searchQuery = "%${p0.lastPathSegment}%"
            val earthquakeDAO =
                EarthquakeDatabaseAccessor.getInstance(context!!.applicationContext).earthquakeDAO()
            val c = earthquakeDAO.generateSearchSuggestions(searchQuery)
            //검색 제안의 커서를 반환한다.
            return c
        }
        return null
    }

    override fun getType(p0: Uri): String? {
        when (uriMatcher.match(p0)) {
            SEARCH_SUGGESTIONS -> return SearchManager.SUGGEST_MIME_TYPE
            else -> throw java.lang.IllegalArgumentException("Unsupported URI: $p0")
        }
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        TODO("Not yet implemented")
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    companion object {
        val SEARCH_SUGGESTIONS = 1

        // UriMatcher 객체를 할당한다. 검색 요청을 파악한다.
        val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(
                "net.ilsoft.earthquake.provider.earthquake",
                SearchManager.SUGGEST_URI_PATH_QUERY,
                SEARCH_SUGGESTIONS
            )
            addURI(
                "net.ilsoft.earthquake.provider.earthquake",
                "${SearchManager.SUGGEST_URI_PATH_QUERY}/*",
                SEARCH_SUGGESTIONS
            )
            addURI(
                "net.ilsoft.earthquake.provider.earthquake",
                SearchManager.SUGGEST_URI_PATH_SHORTCUT,
                SEARCH_SUGGESTIONS
            )
            addURI(
                "net.ilsoft.earthquake.provider.earthquake",
                SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*",
                SEARCH_SUGGESTIONS
            )
        }

    }
}