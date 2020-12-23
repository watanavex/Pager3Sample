package tech.watanave.pager3sample

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.watanave.pager3sample.databinding.ListFragmentBinding
import tech.watanave.pager3sample.databinding.ListItemBinding

class ListFragment : Fragment(R.layout.list_fragment) {

    private val viewModel: ListViewModel by viewModels()
    private val adapter: Adapter by lazy { Adapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = ListFragmentBinding.bind(view)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@ListFragment.adapter
        }
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { state ->
                binding.progressBar.visibility =
                    if (state.refresh is LoadState.Loading) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
            }
        }

        binding.fab.setOnClickListener {
            adapter.refresh()
        }

        viewModel.pager.observe(viewLifecycleOwner) { pageingData ->
            lifecycleScope.launch {
                adapter.submitData(pageingData)
            }
        }
    }
}

class Adapter: PagingDataAdapter<String, Adapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: ListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(id: String) {
            binding.textView.text = id
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position) ?: "null")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

}
