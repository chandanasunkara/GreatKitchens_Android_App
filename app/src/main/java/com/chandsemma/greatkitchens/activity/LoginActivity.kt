package com.chandsemma.greatkitchens.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.chandsemma.greatkitchens.R
import com.chandsemma.greatkitchens.util.ConnectionManager
import com.chandsemma.greatkitchens.util.SessionManager
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    lateinit var etMobileNumber: EditText
    lateinit var etPwd: EditText
    lateinit var btnLogin: Button
    lateinit var txtForgotPwd: TextView
    lateinit var txtRegisterYourself: TextView
    lateinit var sessionManager: SessionManager
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPwd = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtForgotPwd = findViewById(R.id.txtForgotPassword)
        txtRegisterYourself = findViewById(R.id.txtRegisterYourself)
        sessionManager = SessionManager(this)
        sharedPreferences = getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)
        txtForgotPwd.setOnClickListener {
            val intent=Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        txtRegisterYourself.setOnClickListener {
            val intent=Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        btnLogin.setOnClickListener {
            if(etMobileNumber.text.toString().length==10 && etPwd.text.toString().length>=4)  {
                if (ConnectionManager().isNetworkAvailable(this@LoginActivity)) {
                    val queue = Volley.newRequestQueue(this@LoginActivity)
                    val url="http://13.235.250.119/v2/login/fetch_result"
                    val jsonParams = JSONObject()
                    jsonParams.put("mobile_number", etMobileNumber.text.toString())
                    jsonParams.put("password", etPwd.text.toString())
                    val jsonObjectRequest = object : JsonObjectRequest( Request.Method.POST, url, jsonParams,
                        Response.Listener {
                            try {
                                val jsonResponse = it.getJSONObject("data")
                                val success = jsonResponse.getBoolean("success")
                                if (success) {
                                    val response = jsonResponse.getJSONObject("data")
                                    sharedPreferences.edit() .putString("user_id", response.getString("user_id")).apply()
                                    sharedPreferences.edit() .putString("user_name", response.getString("name")).apply()
                                    sharedPreferences.edit() .putString( "user_mobile_number", response.getString("mobile_number") ) .apply()
                                    sharedPreferences.edit() .putString("user_address", response.getString("address")) .apply()
                                    sharedPreferences.edit() .putString("user_email", response.getString("email")).apply()
                                    sessionManager.setLogin(true)
                                    val intent=Intent( this@LoginActivity, MainActivity::class.java )
                                    startActivity(intent)
                                    finish()
                                } else {
                                    btnLogin.visibility = View.VISIBLE
                                    txtForgotPwd.visibility = View.VISIBLE
                                    btnLogin.visibility = View.VISIBLE
                                    val errorMessage = jsonResponse.getString("errorMessage")
                                    Toast.makeText( this@LoginActivity, errorMessage, Toast.LENGTH_SHORT ).show()
                                }
                            } catch (e: JSONException) {
                                btnLogin.visibility = View.VISIBLE
                                txtForgotPwd.visibility = View.VISIBLE
                                txtRegisterYourself.visibility = View.VISIBLE
                                e.printStackTrace()
                            }
                        },
                        Response.ErrorListener {
                            Toast.makeText( this@LoginActivity, "Some error occurred" , Toast.LENGTH_SHORT ).show()
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
                    val dialog= AlertDialog.Builder(this@LoginActivity)
                    dialog.setTitle("No Internet")
                    dialog.setMessage("Internet Connection can't be establish!")
                    dialog.setPositiveButton("Open Settings"){ text, listener->
                        val settingsIntent= Intent(Settings.ACTION_SETTINGS)
                        startActivity(settingsIntent)
                    }
                    dialog.setNegativeButton("Exit"){ text, listener->
                        ActivityCompat.finishAffinity(this@LoginActivity)
                    }
                    dialog.create()
                    dialog.show()
                }
            } else {
                Toast.makeText(this@LoginActivity, "Invalid Phone or Password", Toast.LENGTH_SHORT) .show()
            }
        }

    }
}
