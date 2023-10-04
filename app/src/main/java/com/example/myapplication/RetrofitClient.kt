package com.example.myapplication
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// retrofit set up so there's no need to redo it if it's needed in multiple files

object RetrofitClient {
        private const val BASE_URL = "https://api.giphy.com/v1/"
        private const val API_KEY = "29hKHyyk3IP0Ey7wddGM8gIbyB7E4UhC"

        private val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val giphyService: GiphyService by lazy {
                retrofit.create(GiphyService::class.java)
        }

        fun getApiKey(): String {
                return API_KEY
        }
}
