package com.example.emmet

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_articles.*
import org.json.JSONObject



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

 class articles : Fragment(),NewsArticleAdapter.MyItemClickListener {


     var myListener: ArticleListener? = null


     fun setMyArticleListener (listener: ArticleListener){
         this.myListener = listener
     }

     interface ArticleListener {
         fun handleErrors(String:String)
     }

    private var param1: String? = null
    lateinit var  linearLayoutManager : RecyclerView.LayoutManager
    lateinit var newsList :ArrayList<NewsData>
    lateinit var myAdapter: NewsArticleAdapter
    private var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
     private lateinit var auth: FirebaseAuth
    private var countryCode :String? = null


     override fun onArticleCardPressed(string: String) {
         val i = Intent(context, articleweb_view::class.java)
         i.putExtra("url",string)
         startActivity(i)
     }

    val youtubesearchstringListner = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            print(dataSnapshot.child("users").value)
                            //perform youtube search

            val ans = dataSnapshot.key

            val map = dataSnapshot.getValue()
            if (map is Map<*, *>) {
                val message = map["resultString"].toString()
            }

            if(ans == "resultString") {
                var youtubesearch = "https://www.googleapis.com/youtube/v3/search/?key=AIzaSyDuIX9pH9j5mvVZfMzb2uE03pVzbtu7aSM&part=snippet&q=".plus(map)
                youtubeSearch(youtubesearch)
            }
            else if(ans == "country") {
                countryCode = map.toString()

                if(param1 == "trending") {
                    if(countryCode == null) {
                        httpCall("https://newsapi.org/v2/top-headlines?country=us&apiKey=3d2ebaa0c86748cda95a59a4639af6d3")
                    }
                    else {
                        var url = "https://newsapi.org/v2/top-headlines?country=".plus(countryCode).plus("&apiKey=3d2ebaa0c86748cda95a59a4639af6d3")
                        httpCall(url)
                    }
                }
                else {
                    httpCall("https://newsapi.org/v2/top-headlines?country=us&category=sports&apiKey=3d2ebaa0c86748cda95a59a4639af6d3")
                }

            }


        }

        override fun onCancelled(databaseError: DatabaseError) {
            println("loadPost:onCancelled ${databaseError.toException()}")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
        auth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("users").child(auth.currentUser?.uid.toString()).child("resultString").addValueEventListener(youtubesearchstringListner)
        mDatabase.child("users").child(auth.currentUser?.uid.toString()).child("country").addValueEventListener(youtubesearchstringListner)

    }

     override fun onStart() {
         super.onStart()
        // mDatabase.child("users").child(auth.currentUser?.uid.toString()).child("resultString").removeEventListener(youtubesearchstringListner)


     }

    fun start() {
    }


     override fun onStop() {
         super.onStop()
         mDatabase.child("users").child(auth?.currentUser?.uid.toString()).child("country").removeEventListener(youtubesearchstringListner)
        // mDatabase.child("users").child(auth.currentUser?.uid.toString()).child("resultString").removeEventListener(youtubesearchstringListner)


     }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        newsList = arrayListOf()
        // Inflate the layout for this fragment
        var  mView:View? = null
        if(isOnline(context!!)) {
             mView = inflater.inflate(R.layout.fragment_articles, container, false)

            val itemRecyclerView = mView.findViewById(R.id.recyclerView) as RecyclerView
            itemRecyclerView.layoutManager = LinearLayoutManager(mView.context)


            linearLayoutManager = itemRecyclerView.layoutManager as LinearLayoutManager
            myAdapter = NewsArticleAdapter(newsList, param1!!, mView.context,this, mDatabase,auth)
            itemRecyclerView.adapter = myAdapter

            myAdapter.setMyItemClickListener(this)


        }
        else {
           mView = inflater.inflate(R.layout.fragment_nointernetfragment,container,false)
        }



        return mView
    }




    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    fun youtubeSearch(Url: String) {

        if ((this.context!= null) && isOnline(this.context!!) && myAdapter.displayVideo) {
            val queue = Volley.newRequestQueue(context)
            val url = Url

            val stringRequest = StringRequest(
                Request.Method.GET, url,
                Response.Listener<String> { response ->
                    val jsonObj = JSONObject(response)
                    val contacts = jsonObj.getJSONArray("items")
                    var map = contacts.toString()
                    var movieList:List<test> = Gson().fromJson(map, Array<test>::class.java).toList()
                    if(movieList.size > 0) {
                        var onePart = movieList[0]
                        var x = onePart.id
                        print(x.videoId)
                        if(x.videoId != null) {
                            val i = Intent(context, youtubeActivity::class.java)
                            i.putExtra("videoId", x.videoId)
                            myAdapter.displayVideo = false
                            mDatabase.child("users").child(auth.currentUser?.uid.toString()).child("resultString").setValue("")
                          //  mDatabase.child("users").child(auth.currentUser?.uid.toString()).child("resultString").removeEventListener(youtubesearchstringListner)
                            startActivity(i)

                        }
                    }
                    else {
                    }
                },
                Response.ErrorListener { print("That didn't work!") })
            queue.add(stringRequest)
        }
    }



    fun httpCall(Url: String)  {
        var myList: ArrayList<NewsData> = arrayListOf()
        if (isOnline(context!!)) {

            val queue = Volley.newRequestQueue(context)
            val url = Url
// Request a string response from the provided URL.
            val stringRequest = StringRequest(
                Request.Method.GET, url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    val jsonObj = JSONObject(response)
                    val contacts = jsonObj.getJSONArray("articles")

                    for (i in 0 until contacts.length()) {
                        val item = contacts.getJSONObject(i)

                        if( item?.getString("author") != "null"  && item?.getString("urlToImage") != "null"&& item?.getString("title") != "null" ) {

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

                    }
                    newsList = myList
                    myAdapter.UpdateList(newsList)
                },
                Response.ErrorListener { print("That didn't work!") })

            queue.add(stringRequest)
        }
    }






    companion object {
        /**
         * Use this factory method to create a new instance of* this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment articles.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            articles().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }

    data class test(
        val etag: String,
        val id: com.example.emmet.Id,
        val kind: String,
        val snippet: com.example.emmet.Snippet
    )

    data class Id(
        val kind: String,
        val videoId: String
    )

    data class Snippet(
        val channelId: String,
        val channelTitle: String,
        val description: String,
        val liveBroadcastContent: String,
        val publishedAt: String,
        val thumbnails: com.example.emmet.Thumbnails,
        val title: String
    )

    data class Thumbnails(
        val default: com.example.emmet.Default,
        val high: com.example.emmet.High,
        val medium: com.example.emmet.Medium
    )

    data class Default(
        val height: Int,
        val url: String,
        val width: Int
    )

    data class High(
        val height: Int,
        val url: String,
        val width: Int
    )

    data class Medium(
        val height: Int,
        val url: String,
        val width: Int
    )

}

