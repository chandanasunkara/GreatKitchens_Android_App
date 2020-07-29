package com.chandsemma.greatkitchens.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.chandsemma.greatkitchens.R
import com.chandsemma.greatkitchens.util.ConnectionManager
import com.chandsemma.greatkitchens.util.SessionManager
import org.json.JSONObject
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var btnRegister: Button
    lateinit var etName: EditText
    lateinit var etPhoneNumber: EditText
    lateinit var etPwd: EditText
    lateinit var etEmail: EditText
    lateinit var etAddress: EditText
    lateinit var etConfirmPwd: EditText
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var rlRegister: RelativeLayout
    lateinit var sharedPreferences: SharedPreferences
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Register Yourself"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sessionManager = SessionManager(this@RegisterActivity)
        sharedPreferences = this@RegisterActivity.getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)
        rlRegister = findViewById(R.id.rlRegister)
        etName = findViewById(R.id.etName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etEmail = findViewById(R.id.etEmail)
        etPwd = findViewById(R.id.etPassword)
        etConfirmPwd = findViewById(R.id.etConfirmPassword)
        etAddress = findViewById(R.id.etAddress)
        btnRegister = findViewById(R.id.btnRegister)
        progressLayout=findViewById(R.id.progLayout)
        progressBar = findViewById(R.id.progressBar)

        rlRegister.visibility = View.VISIBLE
        progressLayout.visibility=View.INVISIBLE
        progressBar.visibility = View.INVISIBLE

        btnRegister.setOnClickListener {
            rlRegister.visibility = View.INVISIBLE
            progressLayout.visibility=View.VISIBLE
            progressBar.visibility = View.VISIBLE
            if ( ! etName.text.isEmpty()) {
                etName.error = null
                if ( (!etEmail.text.isEmpty()) && isValidEmail(
                        etEmail.text.toString()
                    )
                ) {
                    etEmail.error = null
                    if ( ( ! etPhoneNumber.text.isEmpty() )  &&  (etPhoneNumber.text.toString().length==10) ) {
                        etPhoneNumber.error = null
                        if ( !etPwd.text.isEmpty() && (etPwd.text.toString().length>=4) ) {
                            etPwd.error = null
                            if (etPwd.text.toString() == etConfirmPwd.text.toString() ) {
                                etPwd.error = null
                                etConfirmPwd.error = null
                                if (ConnectionManager().isNetworkAvailable(this@RegisterActivity)) {
                                    sendRegisterRequest(
                                        etName.text.toString(),
                                        etPhoneNumber.text.toString(),
                                        etAddress.text.toString(),
                                        etPwd.text.toString(),
                                        etEmail.text.toString()
                                    )
                                } else {
                                    rlRegister.visibility = View.VISIBLE
                                    progressLayout.visibility=View.INVISIBLE
                                    progressBar.visibility = View.INVISIBLE
                                    val dialog= AlertDialog.Builder(this@RegisterActivity)
                                    dialog.setTitle("No Internet")
                                    dialog.setMessage("Internet Connection can't be establish!")
                                    dialog.setPositiveButton("Open Settings"){ text, listener->
                                        val settingsIntent= Intent(Settings.ACTION_SETTINGS)
                                        startActivity(settingsIntent)
                                    }
                                    dialog.setNegativeButton("Exit"){ text, listener->
                                        ActivityCompat.finishAffinity(this@RegisterActivity)
                                    }
                                    dialog.create()
                                    dialog.show()
                                }
                            } else {
                                rlRegister.visibility = View.VISIBLE
                                progressLayout.visibility=View.INVISIBLE
                                progressBar.visibility = View.INVISIBLE
                                etPwd.error = "Passwords don't match"
                                etConfirmPwd.error = "Passwords don't match"
                                Toast.makeText(this@RegisterActivity, "Passwords don't match", Toast.LENGTH_SHORT) .show()
                            }
                        } else {
                            rlRegister.visibility = View.VISIBLE
                            progressLayout.visibility=View.INVISIBLE
                            progressBar.visibility = View.INVISIBLE
                            etPwd.error = "Invalid Password"
                            Toast.makeText( this@RegisterActivity, "Password should be more than or equal 4 digits", Toast.LENGTH_SHORT ).show()
                        }
                    } else {
                        rlRegister.visibility = View.VISIBLE
                        progressLayout.visibility=View.INVISIBLE
                        progressBar.visibility = View.INVISIBLE
                        etPhoneNumber.error = "Invalid mobile number"
                        Toast.makeText(this@RegisterActivity, "Enter a valid mobile number of 10 digits", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    rlRegister.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                    etEmail.error = "Invalid Email"
                    Toast.makeText(this@RegisterActivity, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                }
            } else {
                rlRegister.visibility = View.VISIBLE
                progressLayout.visibility=View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
                etName.error = "Name is missing"
                Toast.makeText(this@RegisterActivity, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        @JvmStatic
        val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})";
        fun isValidEmail(email: String): Boolean {
            return EMAIL_REGEX.toRegex().matches(email);
        }
    }

    private fun sendRegisterRequest(name: String, phone: String, address: String, password: String, email: String) {
        val queue = Volley.newRequestQueue(this)
        val url="http://13.235.250.119/v2/register/fetch_result"
        val jsonParams = JSONObject()
        jsonParams.put("name", name)
        jsonParams.put("mobile_number", phone)
        jsonParams.put("password", password)
        jsonParams.put("address", address)
        jsonParams.put("email", email)
        val jsonObjectRequest = object : JsonObjectRequest( Request.Method.POST, url, jsonParams,
            Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        val response = data.getJSONObject("data")
                        sharedPreferences.edit() .putString("user_id", response.getString("user_id")).apply()
                        sharedPreferences.edit() .putString("user_name", response.getString("name")).apply()
                        sharedPreferences.edit() .putString( "user_mobile_number", response.getString("mobile_number") ) .apply()
                        sharedPreferences.edit() .putString("user_address", response.getString("address")) .apply()
                        sharedPreferences.edit() .putString("user_email", response.getString("email")).apply()
                        sessionManager.setLogin(true)
                        Toast.makeText(this@RegisterActivity,"Registered successfully", Toast.LENGTH_LONG).show()
                        val intent=Intent( this@RegisterActivity, MainActivity::class.java )
                        startActivity( intent)

                    } else {
                        rlRegister.visibility = View.VISIBLE
                        progressLayout.visibility = View.INVISIBLE
                        progressBar.visibility = View.INVISIBLE
                        val errorMessage = data.getString("errorMessage")
                        Toast.makeText( this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT ).show()
                    }
                } catch (e: Exception){
                    rlRegister.visibility = View.VISIBLE
                    progressLayout.visibility = View.INVISIBLE
                    progressBar.visibility = View.INVISIBLE
                    Toast.makeText( this@RegisterActivity, "Some error occurred.", Toast.LENGTH_SHORT ).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this@RegisterActivity, it.message, Toast.LENGTH_SHORT).show()
                rlRegister.visibility = View.VISIBLE
                progressLayout.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
                Toast.makeText( this@RegisterActivity, "Some error occurred.", Toast.LENGTH_SHORT ).show()
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"
                headers["token"] = "1f0231dbd129ff"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    override fun onSupportNavigateUp(): Boolean {
        Volley.newRequestQueue(this).cancelAll(this::class.java.simpleName)
        onBackPressed()
        return true
    }
}
