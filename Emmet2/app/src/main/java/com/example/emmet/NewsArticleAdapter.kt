package com.example.emmet

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.textclassifier.TextClassifier.TYPE_EMAIL
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso


class NewsArticleAdapter(var items : ArrayList<NewsData>,val articleType:String, val context:Context,val fragment: Fragment ,var firebaseDatabaseReference: DatabaseReference,var firebaseAuth: FirebaseAuth):androidx.recyclerview.widget.RecyclerView.Adapter<NewsArticleAdapter.ViewHolder>() {

    var displayVideo = false

    var myListener: MyItemClickListener? = null


    fun setMyItemClickListener (listener: MyItemClickListener){
        this.myListener = listener
    }

    interface MyItemClickListener {
        fun onArticleCardPressed(String:String)
    }


    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        var articleCard = itemView
        var titleText = itemView.findViewById(R.id.maintitle) as TextView
        var description = itemView.findViewById(R.id.descriptiontxt) as TextView

        var newsArticlePoster = itemView.findViewById(R.id.newsimage) as ImageView

        var authorName = itemView.findViewById(R.id.author) as TextView


        init {
            articleCard.setOnLongClickListener (View.OnLongClickListener {
                if(articleType == "trending") {
                    firebaseDatabaseReference.child("users").child(firebaseAuth.currentUser?.uid.toString()).child("searchString")
                        .setValue(titleText.text)
                    displayVideo = true
                }
                false
            })

            articleCard.setOnClickListener {
                if (myListener != null && displayVideo == false) {
                    myListener?.onArticleCardPressed(items[adapterPosition].url!!)
                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var rowView : View
        if(articleType == "trending") {
           rowView  = LayoutInflater.from(parent.context)
                .inflate(R.layout.trending_news_card_view, parent, false)
        }else {
            rowView = LayoutInflater.from(parent.context)
                .inflate(R.layout.sports_view, parent, false)

        }
        return ViewHolder(rowView)
    }

    fun UpdateList( list:ArrayList<NewsData>) {
        items = list;
        notifyDataSetChanged()

    }



    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var newsArticleCard = items[position]
        holder.titleText.text = newsArticleCard.title
        holder.description.text = newsArticleCard.description
        val url = newsArticleCard.imageToUrl!!
        Picasso.get().load(url).into(holder.newsArticlePoster)
        var authorTxt = "Author"
        holder.authorName.text = authorTxt.plus(" : ").plus(newsArticleCard.author)
    }

    fun handleErrors(String: String) {
        Toast.makeText(context,String, Toast.LENGTH_LONG).show()
    }





}


