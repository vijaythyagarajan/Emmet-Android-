package com.example.emmet

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_articles.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    lateinit var toolbar: ActionBar
    lateinit var itemType: String
    lateinit var newsList :ArrayList<NewsData>
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var bottomNav = findViewById(R.id.navigation) as BottomNavigationView

        bottomNav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        itemType = "trending"
        val trending = articles.newInstance(itemType)
        openFragment(trending)

       // httpCall("https://newsapi.org/v2/top-headlines?country=us&apiKey=23845188ab0744d38437bb00f408954f")


    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_settings -> {
                val songsFragment = Settings()
                itemType = "settings"
                openFragment(songsFragment)
            }
            R.id.navigation_sports -> {

                itemType = "sports"
                val sports = articles.newInstance(itemType)
                openFragment(sports)

            }
            R.id.navigation_trending -> {
                itemType = "trending"
                val trending = articles.newInstance(itemType)
                openFragment(trending)
            }
        }
        false
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        //transaction.addToBackStack(null)
        transaction.commit()
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun httpCall(Url: String) {

        if (isOnline(this)) {

            val queue = Volley.newRequestQueue(this)
            val url = Url

// Request a string response from the provided URL.
            val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    val jsonObj = JSONObject(response)
                    val contacts = jsonObj.getJSONArray("articles")


                    var myList: ArrayList<NewsData> = arrayListOf()
                    for (i in 0 until contacts.length()) {
                    val item = contacts.getJSONObject(i)


                        //val test = Gson().fromJson(response,NewsData::class.java)


                            var newsArticle = NewsData(
                                item?.getString("author"),
                                item?.getString("title"),
                                item?.getString("description"),
                                item?.getString("url"),
                                item?.getString("urlToImage"),
                                item?.getString("publishedAt"),
                                item?.getString("content")
                            )

                        myList.add(newsArticle)
                        }

                    // Your code here


                    print(contacts)


                },
                Response.ErrorListener { print("That didn't work!") })

// Add the request to the RequestQueue.
            queue.add(stringRequest)
        }
        else {
            openFragment(nointernetfragment())
        }
    }

}
