package com.chandsemma.greatkitchens.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.chandsemma.greatkitchens.R
import com.chandsemma.greatkitchens.adapter.OrderHistoryAdapter
import com.chandsemma.greatkitchens.model.OrderDetails
import com.chandsemma.greatkitchens.util.ConnectionManager
import com.chandsemma.greatkitchens.util.DrawerLocker

class OrderHistoryFragment : Fragment() {
    private lateinit var recyclerOrderHistory: RecyclerView
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    private var orderHistoryList = ArrayList<OrderDetails>()
    private lateinit var llHasOrders: LinearLayout
    private lateinit var rlNoOrders: RelativeLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var rlLoading: RelativeLayout
    private var userId = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)
        (activity as DrawerLocker).setDrawerEnabled(true)
        llHasOrders = view.findViewById(R.id.llHasOrders)
        rlNoOrders = view.findViewById(R.id.rlNoOrders)
        recyclerOrderHistory = view.findViewById(R.id.recyclerOrderHistory)
        rlLoading = view?.findViewById(R.id.rlLoading) as RelativeLayout
        rlLoading.visibility = View.VISIBLE
        sharedPreferences =
            (activity as Context).getSharedPreferences("FoodApp", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null) as String
        sendServerRequest(userId)
        return view
    }
    private fun sendServerRequest(userId: String) {
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/orders/fetch_result/"
            val jsonObjectRequest = object :
                JsonObjectRequest(Method.GET, url + userId, null, Response.Listener {
                    rlLoading.visibility = View.GONE
                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            val resArray = data.getJSONArray("data")
                            if (resArray.length() == 0) {
                                llHasOrders.visibility = View.GONE
                                rlNoOrders.visibility = View.VISIBLE
                            } else {
                                for (i in 0 until resArray.length()) {
                                    val orderObject = resArray.getJSONObject(i)
                                    val foodItems = orderObject.getJSONArray("food_items")
                                    val orderDetails = OrderDetails(
                                        orderObject.getInt("order_id"),
                                        orderObject.getString("restaurant_name"),
                                        orderObject.getString("order_placed_at"),
                                        foodItems
                                    )
                                    orderHistoryList.add(orderDetails)
                                    if (orderHistoryList.isEmpty()) {
                                        llHasOrders.visibility = View.GONE
                                        rlNoOrders.visibility = View.VISIBLE
                                    } else {
                                        llHasOrders.visibility = View.VISIBLE
                                        rlNoOrders.visibility = View.GONE
                                        if (activity != null) {
                                            orderHistoryAdapter =
                                                OrderHistoryAdapter(
                                                    activity as Context,
                                                    orderHistoryList
                                                )
                                            val mLayoutManager =
                                                LinearLayoutManager(activity as Context)
                                            recyclerOrderHistory.layoutManager = mLayoutManager
                                            recyclerOrderHistory.itemAnimator = DefaultItemAnimator()
                                            recyclerOrderHistory.adapter = orderHistoryAdapter
                                        } else {
                                            queue.cancelAll(this::class.java.simpleName)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(activity as Context, "Some error occurred.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }, Response.ErrorListener {
                    Toast.makeText(activity as Context, it.message, Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = getString(R.string.my_token)
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
}