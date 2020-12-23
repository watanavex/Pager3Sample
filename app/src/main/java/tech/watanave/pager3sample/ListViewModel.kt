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

            val start: Int
            val end: Int
            val prevKey: Int
            val nextKey: Int

            // スクロール位置上部以外でFABをタップされたか
            if (params is LoadParams.Refresh && page > 0) {
                // ページ 3つ分を読む
                start = (page * params.loadSize) - params.loadSize
                end = start + (params.loadSize * 3)
                prevKey = page - 2
                nextKey = page + 2
            } else {
                start = page * params.loadSize
                end = start + params.loadSize
                prevKey = page - 1
                nextKey = page + 1
            }

            delay(1500)

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

        val offset: Int
        // スクロール位置上部以外でFABをタップされたページか
        if (pages.data.count() != state.config.pageSize) {
            offset = 2
        } else {
            offset = 1
        }
        pages.prevKey?.let { return it + offset }
        pages.nextKey?.let { return it - offset }

        return null
    }
}