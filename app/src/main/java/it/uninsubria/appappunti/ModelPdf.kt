package it.uninsubria.appappunti



data class ModelPdf(
    var uid: String = "",
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var categoryId: String = "",
    var url: String = "",
    var timestamp: Long = 0,
    var viewCount: Long = 0,
    var dowloadsCount: Long = 0,
    var isFavorite: Boolean = false
)
