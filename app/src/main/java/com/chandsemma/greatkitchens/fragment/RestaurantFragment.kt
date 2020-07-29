package com.chandsemma.greatkitchens.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.chandsemma.greatkitchens.R
import com.chandsemma.greatkitchens.activity.CartActivity
import com.chandsemma.greatkitchens.adapter.AllRestaurantsAdapter
import com.chandsemma.greatkitchens.adapter.RestaurantMenuAdapter
import com.chandsemma.greatkitchens.database.OrderEntity
import com.chandsemma.greatkitchens.database.RestaurantDatabase
import com.chandsemma.greatkitchens.database.RestaurantEntity
import com.chandsemma.greatkitchens.model.FoodItem
import com.chandsemma.greatkitchens.util.ConnectionManager
import com.chandsemma.greatkitchens.util.DrawerLocker

class RestaurantFragment(val resEntity: RestaurantEntity) : Fragment() {
    private lateinit var recyclerMenu: RecyclerView
    private lateinit var restaurantMenuAdapter: RestaurantMenuAdapter
    private var menuList = arrayListOf<FoodItem>()
    private lateinit var rlLoading: RelativeLayout
    private var orderList = arrayListOf<FoodItem>()
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var filledHeart: ImageView
    private lateinit var emptyHeart: ImageView
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var goToCart: Button
        var resId: Int? = 0
        var resName: String? = ""
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restaurant, container, false)
        filledHeart=view.findViewById(R.id.filledHeart)
        emptyHeart=view.findViewById(R.id.emptyHeart)
        sharedPreferences = activity?.getSharedPreferences("FoodApp", Context.MODE_PRIVATE) as SharedPreferences
        rlLoading = view?.findViewById(R.id.rlLoading) as RelativeLayout
        rlLoading.visibility = View.VISIBLE
        resId = arguments?.getInt("id", 0)
        resName = arguments?.getString("name", "")
        (activity as DrawerLocker).setDrawerEnabled(false)
        setHasOptionsMenu(true)
        goToCart = view.findViewById(R.id.btnGoToCart) as Button
        goToCart.visibility = View.GONE
        goToCart.setOnClickListener {
            proceedToCart()
        }
        setUpRestaurantMenu(view)

        if (AllRestaurantsAdapter.DBAsyncTask(context as Context, resEntity, 1).execute().get()) {
            filledHeart.visibility=View.VISIBLE
            emptyHeart.visibility=View.GONE
        }
        else{
            filledHeart.visibility=View.GONE
            emptyHeart.visibility=View.VISIBLE
        }
        return view
    }
    private fun setUpRestaurantMenu(view: View) {
        recyclerMenu = view.findViewById(R.id.recyclerMenuItems)
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            val queue = Volley.newRequestQueue(activity as Context)
            val url="http://13.235.250.119/v2/restaurants/fetch_result/"
            val jsonObjectRequest = object :
                JsonObjectRequest(Request.Method.GET, url + resId, null, Response.Listener {
                    rlLoading.visibility = View.GONE
                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            val resArray = data.getJSONArray("data")
                            for (i in 0 until resArray.length()) {
                                val menuObject = resArray.getJSONObject(i)
                                val foodItem =
                                    FoodItem(
                                        menuObject.getString("id"),
                                        menuObject.getString("name"),
                                        menuObject.getString("cost_for_one").toInt()
                                    )
                                menuList.add(foodItem)
                                restaurantMenuAdapter =
                                    RestaurantMenuAdapter(
                                        activity as Context,
                                        menuList,
                                        object :
                                            RestaurantMenuAdapter.OnItemClickListener {
                                            override fun onAddItemClick(foodItem: FoodItem) {
                                                orderList.add(foodItem)
                                                if (orderList.size > 0) {
                                                    goToCart.visibility = View.VISIBLE
                                                    RestaurantMenuAdapter.isCartEmpty =
                                                        false
                                                }
                                            }

                                            override fun onRemoveItemClick(foodItem: FoodItem) {
                                                orderList.remove(foodItem)
                                                if (orderList.isEmpty()) {
                                                    goToCart.visibility = View.GONE
                                                    RestaurantMenuAdapter.isCartEmpty =
                                                        true
                                                }
                                            }
                                        })
                                val mLayoutManager = LinearLayoutManager(activity)
                                recyclerMenu.layoutManager = mLayoutManager
                                recyclerMenu.itemAnimator = DefaultItemAnimator()
                                recyclerMenu.adapter = restaurantMenuAdapter
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(activity as Context, "Some error occurred", Toast.LENGTH_SHORT).show()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(activity as Context, it.message, Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "1f0231dbd129ff"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {
            val dialog= AlertDialog.Builder(activity as Context)
            dialog.setTitle("No Internet")
            dialog.setMessage("Internet Connection can't be establish!")
            dialog.setPositiveButton("Open Settings"){ text, listener->
                val settingsIntent= Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            dialog.setNegativeButton("Exit"){ text, listener->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
    }

    private fun proceedToCart() {
        val gson = Gson()
        val foodItems = gson.toJson(orderList)
        val async = ItemsOfCart(
            activity as Context,
            resId.toString(),
            foodItems,
            1
        ).execute()
        val result = async.get()
        if (result) {
            val data = Bundle()
            data.putInt("resId", resId as Int)
            data.putString("resName",
                resName
            )
            val intent = Intent(activity, CartActivity::class.java)
            intent.putExtra("data", data)
            startActivity(intent)
        } else {
            Toast.makeText((activity as Context), "Some unexpected error", Toast.LENGTH_SHORT)
                .show()
        }
    }


    class ItemsOfCart(
        context: Context,
        val restaurantId: String,
        val foodItems: String,
        val mode: Int
    ) : AsyncTask<Void, Void, Boolean>() {
        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    db.orderDao().insertOrder(
                        OrderEntity(
                            restaurantId,
                            foodItems
                        )
                    )
                    db.close()
                    return true
                }

                2 -> {
                    db.orderDao().deleteOrder(
                        OrderEntity(
                            restaurantId,
                            foodItems
                        )
                    )
                    db.close()
                    return true
                }
            }
            return false
        }
    }

}
