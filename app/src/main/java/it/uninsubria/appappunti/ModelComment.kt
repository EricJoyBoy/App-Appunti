package it.uninsubria.appappunti



data class ModelComment(
    var id: String = "",
    var bookId: String = "",
    var timestamp: String = "",
    var comment: String = "",
    var uid: String = ""
)