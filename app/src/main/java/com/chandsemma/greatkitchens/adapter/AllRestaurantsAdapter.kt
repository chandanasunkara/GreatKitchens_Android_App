package com.chandsemma.greatkitchens.adapter

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.chandsemma.greatkitchens.R
import com.chandsemma.greatkitchens.database.RestaurantDatabase
import com.chandsemma.greatkitchens.database.RestaurantEntity
import com.chandsemma.greatkitchens.fragment.RestaurantFragment
import com.chandsemma.greatkitchens.model.Restaurant
import com.squareup.picasso.Picasso

class AllRestaurantsAdapter(private var restaurants: ArrayList<Restaurant>, val context: Context) :
    RecyclerView.Adapter<AllRestaurantsAdapter.AllRestaurantsViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AllRestaurantsViewHolder {
        val itemView = LayoutInflater.from(p0.context)
            .inflate(R.layout.restaurants_single_row, p0, false)
        return AllRestaurantsViewHolder(
            itemView
        )
    }
    override fun getItemCount(): Int {
        return restaurants.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(p0: AllRestaurantsViewHolder, p1: Int) {
        val resObject = restaurants.get(p1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            p0.resThumbnail.clipToOutline = true
        }
        p0.restaurantName.text = resObject.restaurantName
        p0.rating.text = resObject.restaurantRating
        val costForOne = "${resObject.costForOne.toString()}/person"
        p0.cost.text = costForOne
        Picasso.get().load(resObject.resImageUrl).error(R.drawable.res_image).into(p0.resThumbnail)


        val listOfFavourites = GetAllFavAsyncTask(
            context
        ).execute().get()

        if (listOfFavourites.isNotEmpty() && listOfFavourites.contains(resObject.restaurantId.toString())) {
            p0.favImage.setImageResource(R.drawable.ic_action_fav_checked)
        } else {
            p0.favImage.setImageResource(R.drawable.ic_action_fav)
        }
        p0.favImage.setOnClickListener {
            val restaurantEntity =
                RestaurantEntity(
                    resObject.restaurantId,
                    resObject.restaurantName,
                    resObject.restaurantRating,
                    resObject.costForOne.toString(),
                    resObject.resImageUrl
                )
            if (!DBAsyncTask(
                    context,
                    restaurantEntity,
                    1
                ).execute().get()) {
                val async =
                    DBAsyncTask(
                        context,
                        restaurantEntity,
                        2
                    ).execute()
                val result = async.get()
                if (result) {
                    p0.favImage.setImageResource(R.drawable.ic_action_fav_checked)
                }
            } else {
                val async = DBAsyncTask(
                    context,
                    restaurantEntity,
                    3
                ).execute()
                val result = async.get()

                if (result) {
                    p0.favImage.setImageResource(R.drawable.ic_action_fav)
                }
            }
        }
        p0.cardRestaurant.setOnClickListener {
            val resEntity =
                RestaurantEntity(
                    resObject.restaurantId,
                    resObject.restaurantName,
                    resObject.restaurantRating,
                    resObject.costForOne.toString(),
                    resObject.resImageUrl
                )
            val fragment =
                RestaurantFragment(
                    resEntity
                )
            val args = Bundle()
            args.putInt("id", resObject.restaurantId)
            args.putString("name", resObject.restaurantName)
            fragment.arguments = args
            val transaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame, fragment)
            transaction.commit()
          //  transaction.add(RestaurantFragment(), p0.favImage.id.toString())
            (context as AppCompatActivity).supportActionBar?.title = p0.restaurantName.text.toString()
        }
    }
    class AllRestaurantsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val resThumbnail = view.findViewById(R.id.imgRestaurantThumbnail) as ImageView
        val restaurantName = view.findViewById(R.id.txtRestaurantName) as TextView
        val rating = view.findViewById(R.id.txtRestaurantRating) as TextView
        val cost = view.findViewById(R.id.txtCostForOne) as TextView
        val cardRestaurant = view.findViewById(R.id.cardRestaurant) as CardView
        val favImage = view.findViewById(R.id.imgIsFav) as ImageView
    }
    class DBAsyncTask(context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    val res: RestaurantEntity? = db.restaurantDao().getRestaurantById(restaurantEntity.id.toString())
                    db.close()
                    return res != null
                }
                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }
    class GetAllFavAsyncTask( context: Context ) : AsyncTask<Void, Void, List<String>>() {
        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): List<String> {
            val list = db.restaurantDao().getAllRestaurants()
            val listOfIds = arrayListOf<String>()
            for (i in list) {
                listOfIds.add(i.id.toString())
            }
            return listOfIds
        }
    }
}