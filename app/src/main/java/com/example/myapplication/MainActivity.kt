package com.example.myapplication

import GiphyViewModel
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class MainActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private lateinit var hiddenResultView: RecyclerView
    private lateinit var gifAdapter: GifAdapter
    private lateinit var viewModel: GiphyViewModel
    private lateinit var query: String

    private val handler = Handler(Looper.getMainLooper())
    private val delayMillis = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.gif_search_field)
        hiddenResultView = findViewById(R.id.recyclerView)

        gifAdapter = GifAdapter()

        // Define the number of columns you want in the grid
        val spanCount = 2

        // Use GridLayoutManager instead of StaggeredGridLayoutManager
        val layoutManager = GridLayoutManager(this, spanCount)

        hiddenResultView.layoutManager = layoutManager
        hiddenResultView.adapter = gifAdapter

        viewModel = ViewModelProvider(this)[GiphyViewModel::class.java]

        // Observe the gifUrls LiveData from the ViewModel
        viewModel.gifUrls.observe(this, Observer { gifUrls ->
            // Clear the existing data in the adapter
            gifAdapter.updateData(gifUrls)
        })

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                handler.removeCallbacksAndMessages(null)

                query = s?.toString() ?: ""
                if (query.isNotEmpty()) {
                    handler.postDelayed({
                        // Trigger a new search
                        viewModel.searchGifs(query)
                        viewModel.offset = 0
                    }, delayMillis.toLong())
                    hiddenResultView.visibility = View.VISIBLE
                } else {
                    // Clear the existing data in the adapter and hide the RecyclerView
                    clearGridLayout()
                    hiddenResultView.visibility = View.GONE
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        hiddenResultView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager // Change this line

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!viewModel.isLoading && !viewModel.isLastPage) {

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        // Load more gifs here
                        viewModel.searchGifs(query)
                    }
                }
            }
        })

    }

    private fun clearGridLayout() {
        gifAdapter.updateData(emptyList())
        hiddenResultView.visibility = View.GONE
    }
}
