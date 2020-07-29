package com.chandsemma.greatkitchens.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.chandsemma.greatkitchens.R
import com.chandsemma.greatkitchens.util.ConnectionManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity() {
    lateinit var etForgotMobile: EditText
    lateinit var etForgotEmail: EditText
    lateinit var btnForgotNext: Button
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var rlContentMain: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        etForgotMobile = findViewById(R.id.etForgotMobile)
        etForgotEmail = findViewById(R.id.etForgotEmail)
        btnForgotNext = findViewById(R.id.btnForgotNext)
        rlContentMain = findViewById(R.id.rlContentMain)
        progressLayout = findViewById(R.id.progressLayout)
        progressBar = findViewById(R.id.progressBar)
        rlContentMain.visibility = View.VISIBLE
        progressLayout.visibility=View.GONE
        progressBar.visibility = View.GONE
        btnForgotNext.setOnClickListener {
            val forgotMobileNumber = etForgotMobile.text.toString()
            if ( !(etForgotMobile.text.isEmpty()) && (forgotMobileNumber.length==10) ) {
                etForgotMobile.error = null
                if (isValidEmail(
                        etForgotEmail.text.toString()
                    )
                ) {
                    if (ConnectionManager().isNetworkAvailable(this@ForgotPasswordActivity)) {
                        rlContentMain.visibility = View.GONE
                        progressLayout.visibility=View.VISIBLE
                        progressBar.visibility = View.VISIBLE
                        sendOTP(etForgotMobile.text.toString(), etForgotEmail.text.toString())
                    } else {
                        rlContentMain.visibility = View.VISIBLE
                        progressLayout.visibility=View.GONE
                        progressBar.visibility = View.GONE
                        val dialog= AlertDialog.Builder(this@ForgotPasswordActivity)
                        dialog.setTitle("No Internet")
                        dialog.setMessage("Internet Connection can't be establish!")
                        dialog.setPositiveButton("Open Settings"){ text, listener->
                            val settingsIntent= Intent(Settings.ACTION_SETTINGS)
                            startActivity(settingsIntent)
                        }
                        dialog.setNegativeButton("Exit"){ text, listener->
                            ActivityCompat.finishAffinity(this@ForgotPasswordActivity)
                        }
                        dialog.create()
                        dialog.show()
                    }
                } else {
                    rlContentMain.visibility = View.VISIBLE
                    progressLayout.visibility=View.GONE
                    progressBar.visibility = View.GONE
                    etForgotEmail.error = "Invalid Email"
                    Toast.makeText( this@ForgotPasswordActivity, "Please enter a valid mail id", Toast.LENGTH_SHORT ).show()
                }
            } else {
                rlContentMain.visibility = View.VISIBLE
                progressLayout.visibility=View.GONE
                progressBar.visibility = View.GONE
                etForgotMobile.error = "Invalid Mobile Number"
                Toast.makeText( this@ForgotPasswordActivity, "Please enter a valid phone number", Toast.LENGTH_SHORT ).show()
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

    private fun sendOTP(mobileNumber: String, email: String) {
        val queue = Volley.newRequestQueue(this)
        val url ="http://13.235.250.119/v2/forgot_password/fetch_result"
        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNumber)
        jsonParams.put("email", email)
        val jsonObjectRequest = object : JsonObjectRequest(Method.POST, url, jsonParams,
            Response.Listener {
                try {
                    val jsonResponse = it.getJSONObject("data")
                    val success = jsonResponse.getBoolean("success")
                    if (success) {
                        val firstTry = jsonResponse.getBoolean("first_try")
                        if (firstTry) {
                            Toast.makeText( this@ForgotPasswordActivity, "An OTP is sent to your registered mail. Please check", Toast.LENGTH_SHORT ).show()
                            val intent = Intent( this@ForgotPasswordActivity, ResetPasswordActivity::class.java )
                            intent.putExtra("user_mobile", mobileNumber)
                            startActivity(intent)
                        } else {
                            Toast.makeText( this@ForgotPasswordActivity, "OTP is already sent to your registered mail. Please check", Toast.LENGTH_SHORT ).show()
                            val intent = Intent( this@ForgotPasswordActivity, ResetPasswordActivity::class.java )
                            intent.putExtra("user_mobile", mobileNumber)
                            startActivity(intent)
                        }
                    } else {
                        rlContentMain.visibility = View.VISIBLE
                        progressLayout.visibility=View.GONE
                        progressBar.visibility = View.GONE
                        Toast.makeText( this@ForgotPasswordActivity, "User not registered!", Toast.LENGTH_SHORT ).show()
                    }
                } catch (e: Exception) {
                    rlContentMain.visibility = View.VISIBLE
                    progressLayout.visibility=View.GONE
                    progressBar.visibility = View.GONE
                    Toast.makeText( this@ForgotPasswordActivity, "Response error!!", Toast.LENGTH_SHORT ).show()
                }
            }, Response.ErrorListener {
                rlContentMain.visibility = View.VISIBLE
                progressLayout.visibility=View.GONE
                progressBar.visibility = View.GONE
                Toast.makeText(this@ForgotPasswordActivity, "Some error occurred"/* it.message */, Toast.LENGTH_SHORT).show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "1f0231dbd129ff"
                    return headers
                }
            }
        queue.add(jsonObjectRequest)
    }
}
