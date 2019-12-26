package com.example.emmet
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.net.URL

class NewsData (
    val author: String?,
    val title:String?,
    val description: String?,
    val url:String?,
    val imageToUrl:String?,
    val publishedAt:String?,
    val content:String?
):Serializable {
    constructor(): this("","","","","","","")

}