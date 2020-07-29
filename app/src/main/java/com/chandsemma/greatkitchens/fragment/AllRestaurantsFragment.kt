package com.chandsemma.greatkitchens.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.chandsemma.greatkitchens.R
import com.chandsemma.greatkitchens.adapter.AllRestaurantsAdapter
import com.chandsemma.greatkitchens.model.Restaurant
import com.chandsemma.greatkitchens.util.ConnectionManager
import com.chandsemma.greatkitchens.util.DrawerLocker
import com.chandsemma.greatkitchens.util.Sorter
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class AllRestaurantsFragment : Fragment() {

    private lateinit var recyclerRestaurant: RecyclerView
    private lateinit var allRestaurantsAdapter: AllRestaurantsAdapter
    private var restaurantList = arrayListOf<Restaurant>()
    private lateinit var progressBar: ProgressBar
    private lateinit var rlLoading: RelativeLayout
    private var checkedItem: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        (activity as DrawerLocker).setDrawerEnabled(true)
        progressBar = view?.findViewById(R.id.progressBar) as ProgressBar
        rlLoading = view.findViewById(R.id.rlLoading) as RelativeLayout
        rlLoading.visibility = View.VISIBLE

        setUpRecycler(view)
        setHasOptionsMenu(true)
        return view
    }
    private fun setUpRecycler(view: View) {
        recyclerRestaurant = view.findViewById(R.id.recyclerRestaurants) as RecyclerView

        val queue = Volley.newRequestQueue(activity as Context)
        val url="http://13.235.250.119/v2/restaurants/fetch_result/"
        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            val jsonObjectRequest = object : JsonObjectRequest( Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    rlLoading.visibility = View.GONE
                    try {
                        val jsonResponse = response.getJSONObject("data")
                        val success = jsonResponse.getBoolean("success")
                        if (success) {
                            val dataArray = jsonResponse.getJSONArray("data")
                            for (i in 0 until dataArray.length()) {
                                val resObject = dataArray.getJSONObject(i)
                                val restaurant = Restaurant(
                                    resObject.getString("id").toInt(),
                                    resObject.getString("name"),
                                    resObject.getString("rating"),
                                    resObject.getString("cost_for_one").toInt(),
                                    resObject.getString("image_url")
                                )
                                restaurantList.add(restaurant)
                                if (activity != null) {
                                    allRestaurantsAdapter =
                                        AllRestaurantsAdapter(
                                            restaurantList,
                                            activity as Context
                                        )
                                    val mLayoutManager = LinearLayoutManager(activity)
                                    recyclerRestaurant.layoutManager = mLayoutManager
                                    recyclerRestaurant.itemAnimator = DefaultItemAnimator()
                                    recyclerRestaurant.adapter = allRestaurantsAdapter
                                    recyclerRestaurant.setHasFixedSize(true)
                                }
                            }
                        } else {
                            Toast.makeText(activity as Context, "Some error occurred", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(activity as Context, "Some error occurred", Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { error: VolleyError? ->
                    Toast.makeText(activity as Context, error?.message, Toast.LENGTH_SHORT).show()
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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        activity?.menuInflater?.inflate(R.menu.dashboard_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_sort -> showDialog(context as Context)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDialog(context: Context) {

        val builder: AlertDialog.Builder? = AlertDialog.Builder(context)
        builder?.setTitle("Sort By?")
        builder?.setSingleChoiceItems(R.array.filters, checkedItem) { _, isChecked ->
            checkedItem = isChecked
        }
        builder?.setPositiveButton("Ok") { _, _ ->

            when (checkedItem) {
                0 -> {
                    Collections.sort(restaurantList, Sorter.costComparator)
                }
                1 -> {
                    Collections.sort(restaurantList, Sorter.costComparator)
                    restaurantList.reverse()
                }
                2 -> {
                    Collections.sort(restaurantList, Sorter.ratingComparator)
                    restaurantList.reverse()
                }
            }
            allRestaurantsAdapter.notifyDataSetChanged()
        }
        builder?.setNegativeButton("Cancel") { _, _ ->

        }
        builder?.create()
        builder?.show()
    }

}
