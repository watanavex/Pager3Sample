package tech.watanave.pager3sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.paging.liveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ListViewModel : ViewModel() {

    private var _pagingSource: ListPagingSource? = null
    private val _pager = Pager(PagingConfig(30, initialLoadSize = 30)) {
        ListPagingSource().also {
            _pagingSource = it
        }
    }

    val pager = _pager.liveData.cachedIn(viewModelScope)

    fun invalidate() {
        _pagingSource?.invalidate()
    }
}
class ListPagingSource: PagingSource<Int, String>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
        return withContext(Dispatchers.IO) {
            // ページは0開始
            val page = params.key ?: 0
            val start = page * params.loadSize
            val end = start + params.loadSize

            delay(1500)

            val prevKey = page - 1
            val nextKey = page + 1
            return@withContext LoadResult.Page(
                data = (start until end)
                    .map { it.toString() }
                    .toList(),
                prevKey = if (prevKey < 0) { null } else { prevKey },
                nextKey = nextKey
            )
        }
    }

    override fun getRefreshKey(state: PagingState<Int, String>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val pages = state.closestPageToPosition(anchorPosition) ?: return null

        pages.nextKey?.let { return it - 1 }

        return null
    }
}