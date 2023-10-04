package com.example.myapplication

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GiphyService {
    @GET("gifs/search")
    fun searchGifs(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Call<GiphyResponse>
}