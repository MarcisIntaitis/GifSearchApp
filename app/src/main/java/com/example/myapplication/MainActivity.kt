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

/*
* updated code tested on Pixel 7 Pro API 30 and Samsung Galaxy s22+ (physical device), works fine on both
* but scrolling is worse on the emulator, so I assume that it might be an issue with the emulator itself
*/
class MainActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private lateinit var hiddenResultView: RecyclerView
    private lateinit var gifAdapter: GifAdapter
    private lateinit var viewModel: GiphyViewModel
    private lateinit var query: String
    private val delayMillis = 400

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.gif_search_field)
        hiddenResultView = findViewById(R.id.recyclerView)


        /* removing the differing heights on gifs resolved issues with the
         * grid loading items on one side only and them "jumping" around but as a results it looks worse */
        gifAdapter = GifAdapter()
        val spanCount = 2
        val layoutManager = GridLayoutManager(this, spanCount)

        hiddenResultView.layoutManager = layoutManager
        hiddenResultView.adapter = gifAdapter

        viewModel = ViewModelProvider(this)[GiphyViewModel::class.java]

        // observe the gifUrls LiveData from the ViewModel
        viewModel.gifUrls.observe(this, Observer { gifUrls ->
            // clear the existing data in the adapter
            gifAdapter.updateData(gifUrls)
        })

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                handler.removeCallbacksAndMessages(null)

                query = s?.toString() ?: ""
                if (query.isNotEmpty()) {
                    // setting delay in the GiphyViewModel caused issues with gif loading
                    handler.postDelayed({
                        // triggers a new search, not having offset set to 0 still shows the old query results
                        viewModel.searchGifs(query)
                        viewModel.offset = 0
                        hiddenResultView.visibility = View.VISIBLE
                    }, delayMillis.toLong())
                } else {
                    clearGridLayout()
                    hiddenResultView.visibility = View.GONE
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        hiddenResultView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!viewModel.isLoading && !viewModel.isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
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
