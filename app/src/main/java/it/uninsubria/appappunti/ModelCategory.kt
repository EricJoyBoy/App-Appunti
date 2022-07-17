package it.uninsubria.appappunti

class ModelCategory { // classe che rappresenta una categoria

    var id:String = "" // id della categoria
    var category:String = "" // nome della categoria
    var timestamp: Long = 0 // timestamp della categoria
    var uid:String = "" // id dell'utente che ha creato la categoria


// Costruttore vuoto richiesto da Firebase
    constructor()
// Costruttre con i parametri
    constructor(
        id: String,
        category: String,
        timestamp: Long,
        uid: String
    ) {
        this.id = id // id della categoria
        this.category = category // nome della categoria
        this.timestamp = timestamp // timestamp della categoria
        this.uid = uid // id dell'utente che ha creato la categoria
    }


}