import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import com.example.myapplication.GiphyResponse
import com.example.myapplication.GiphyService
import retrofit2.Response

class GiphyViewModel : ViewModel() {
    private val giphyService = RetrofitClient.giphyService
    private val apiKey = RetrofitClient.getApiKey()

    private val _gifUrls = MutableLiveData<List<String>>()
    val gifUrls: LiveData<List<String>> get() = _gifUrls

    var offset = 0
    private val limit = 12

    var isLoading = false
    var isLastPage = false

    fun searchGifs(query: String) {
        if (isLoading || isLastPage) {
            return
        }
        isLoading = true

        viewModelScope.launch {
            try {
                val response: Response<GiphyResponse> = withContext(Dispatchers.IO) {
                    giphyService.searchGifs(apiKey, query, offset, limit).execute()
                }

                if (response.isSuccessful) {
                    val giphyResponse = response.body()
                    val gifs = giphyResponse?.data ?: emptyList()

                    Log.d("GifAdapter", "Fetched ${gifs.size} GIFs")


                    // Clear the old list if it's the first page
                    if (offset == 0) {
                        _gifUrls.value = emptyList()
                    }

                    val currentGifUrls = _gifUrls.value ?: emptyList()
                    val newGifUrls = currentGifUrls.toMutableList()
                    for (gif in gifs) {
                        newGifUrls.add(gif.images.fixed_height.url)
                    }

                    _gifUrls.value = newGifUrls
                    isLoading = false

                    if (gifs.isEmpty()) {
                        isLastPage = true
                    }

                    offset += limit
                } else {
                    Log.e("ERROR", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ERROR", "$e")
            }
        }
    }
}
