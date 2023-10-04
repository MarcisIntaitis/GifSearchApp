package com.example.myapplication

data class GiphyResponse(
    val data: List<GifItem>
)

data class GifItem(
    val images: Images
)

data class Images(
    val fixed_height: ImageDetail
)

data class ImageDetail(
    val url: String
)
