package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.myapplication.RetrofitClient.giphyService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
tested on Pixel 7 Pro API 30, Pixel 3a API 34 emulators and Samsung Galaxy s22 Plus (physical device), had the issues i've mentioned further on
the large amount of comments is so I can follow along myself since i'm not experienced with kotlin
*/

class MainActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private lateinit var hiddenResultView: RecyclerView
    private val gifUrls: MutableList<String> = mutableListOf()
    private lateinit var gifAdapter: GifAdapter

    private var apiKey = RetrofitClient.getApiKey()
    private var offset = 0
    private val limit = 12 // amount of items initially loaded

    private var isLoading = false
    private var isLastPage = false

    private val handler = Handler(Looper.getMainLooper())
    private val delayMillis = 500 // delay in milliseconds so there aren't that many requests sent otherwise writing query "cat" it'd show gifs for "c" and "ca"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.gif_search_field)
        hiddenResultView = findViewById(R.id.recyclerView)

        /*  responsible for the layout of gifs, StaggeredGridLayoutManager adds the uneven effect that's seen in Giphy.com
            has a weird issue when sorting the gifs in the grid, sometimes one side doesn't load properly, causes issues with scrolling back
            in combination with the pagination, genuinely no clue how to solve this */

        gifAdapter = GifAdapter(gifUrls)
        val spanCount = 2
        val layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
        hiddenResultView.layoutManager = layoutManager
        hiddenResultView.adapter = gifAdapter

        // monitors editText for any changes with a delay
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // removes previous callbacks to avoid unnecessary API calls
                handler.removeCallbacksAndMessages(null)

                val query = s?.toString() ?: ""
                if (query.isNotEmpty()) {
                    // adds the previously declared delay
                    handler.postDelayed({
                        offset = 0
                        loadMoreGifs(query)
                    }, delayMillis.toLong())
                    hiddenResultView.visibility = View.VISIBLE
                } else {
                    clearGridLayout()
                    hiddenResultView.visibility = View.GONE
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        // implementing pagination using RecyclerView scroll listener, might be the cause of the issue when scrolling back up
        hiddenResultView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!isLoading && !isLastPage) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPositions = layoutManager.findFirstVisibleItemPositions(null)

                    val firstVisibleItemPosition = firstVisibleItemPositions[0]

                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        // load more data when reaching the end of the list
                        offset += limit
                        loadMoreGifs(editText.text.toString())
                    }
                }
            }
        })
    }

    // handles the loading of more gifs
    private fun loadMoreGifs(query: String) {
        isLoading = true

        giphyService.searchGifs(apiKey, query, offset, limit)
            .enqueue(object : Callback<GiphyResponse> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call<GiphyResponse>, response: Response<GiphyResponse>) {
                    if (response.isSuccessful) {
                        val giphyResponse = response.body()
                        val gifs = giphyResponse?.data ?: emptyList()

                        val itemCountBeforeLoad = gifAdapter.itemCount

                        for (gif in gifs) {
                            gifUrls.add(gif.images.fixed_height.url)
                        }
                        gifAdapter.notifyItemRangeInserted(itemCountBeforeLoad, gifs.size)

                        isLoading = false
                        if (gifs.isEmpty()) {
                            isLastPage = true
                        }
                    } else {
                        Log.e("ERROR", "Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<GiphyResponse>, t: Throwable) {
                    isLoading = false
                    Log.e("ERROR", "Failed to fetch data: ${t.message}", t)
                }
            })
    }


    private fun clearGridLayout() {
        gifUrls.clear()
        gifAdapter.notifyDataSetChanged()
        hiddenResultView.visibility = View.GONE
    }
}
