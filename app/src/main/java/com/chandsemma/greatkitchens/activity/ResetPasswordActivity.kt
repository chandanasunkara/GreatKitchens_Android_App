package com.chandsemma.greatkitchens.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.chandsemma.greatkitchens.R
import com.chandsemma.greatkitchens.util.ConnectionManager
import org.json.JSONObject

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var etOTP: EditText
    private lateinit var etNewPwd: EditText
    private lateinit var etConfirmNewPwd: EditText
    private lateinit var btnSubmit: Button
    private lateinit var rlOTP: RelativeLayout
    private lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var mobileNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        etOTP = findViewById(R.id.etOTP)
        etNewPwd = findViewById(R.id.etNewPassword)
        etConfirmNewPwd = findViewById(R.id.etConfirmNewPassword)
        btnSubmit = findViewById(R.id.btnSubmitOTP)
        rlOTP = findViewById(R.id.rlOTP)
        progressLayout = findViewById(R.id.progressLayout)
        progressBar = findViewById(R.id.progressBar)

        rlOTP.visibility = View.VISIBLE
        progressLayout.visibility=View.GONE
        progressBar.visibility = View.GONE

        if (intent != null) {
            mobileNumber = intent.getStringExtra("user_mobile") as String
        }
        btnSubmit.setOnClickListener {
            rlOTP.visibility = View.GONE
            progressLayout.visibility=View.VISIBLE
            progressBar.visibility = View.VISIBLE
            if (ConnectionManager().isNetworkAvailable(this@ResetPasswordActivity)) {
                if (etOTP.text.toString().length==4) {
                    if (etNewPwd.text.toString().length>=4) {
                        if ( etNewPwd.text.toString() == etConfirmNewPwd.text.toString() ) {
                            resetPassword( mobileNumber, etOTP.text.toString(), etNewPwd.text.toString() )
                        } else {
                            rlOTP.visibility = View.VISIBLE
                            progressLayout.visibility=View.GONE
                            progressBar.visibility = View.GONE
                            Toast.makeText( this@ResetPasswordActivity, "Passwords do not match", Toast.LENGTH_SHORT ) .show()
                        }
                    } else {
                        rlOTP.visibility = View.VISIBLE
                        progressLayout.visibility=View.GONE
                        progressBar.visibility = View.GONE
                        Toast.makeText( this@ResetPasswordActivity, "Invalid Password", Toast.LENGTH_SHORT ).show()
                    }
                } else {
                    rlOTP.visibility = View.VISIBLE
                    progressLayout.visibility=View.GONE
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ResetPasswordActivity, "Incorrect OTP", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                progressLayout.visibility = View.GONE
                progressBar.visibility = View.GONE
                val dialog= AlertDialog.Builder(this@ResetPasswordActivity)
                dialog.setTitle("No Internet")
                dialog.setMessage("Internet connection not found")
                dialog.setPositiveButton("Open Settings"){ text, listener->
                    val settingsIntent= Intent(Settings.ACTION_SETTINGS)
                    startActivity(settingsIntent)
                }
                dialog.setNegativeButton("Exit"){ text, listener->
                    ActivityCompat.finishAffinity(this@ResetPasswordActivity)
                }
                dialog.create()
                dialog.show()
            }
        }
    }
    private fun resetPassword(mobileNumber: String, otp: String, password: String) {
        val queue = Volley.newRequestQueue(this)
        val url="http://13.235.250.119/v2/reset_password/fetch_result"
        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", mobileNumber)
        jsonParams.put("password", password)
        jsonParams.put("otp", otp)
        val jsonObjectRequest = object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        progressLayout.visibility = View.INVISIBLE
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(this@ResetPasswordActivity, " Password changed successfully",Toast.LENGTH_SHORT).show()
                        val inte=Intent( this@ResetPasswordActivity, LoginActivity::class.java )
                        startActivity(inte )
                        ActivityCompat.finishAffinity(this@ResetPasswordActivity)
                    } else {
                        rlOTP.visibility = View.VISIBLE
                        progressLayout.visibility=View.GONE
                        progressBar.visibility = View.GONE
                        val error = data.getString("errorMessage")
                        Toast.makeText( this@ResetPasswordActivity, error, Toast.LENGTH_SHORT ).show()
                    }
                } catch (e: Exception) {
                    rlOTP.visibility = View.VISIBLE
                    progressLayout.visibility=View.GONE
                    progressBar.visibility = View.GONE
                    Toast.makeText( this@ResetPasswordActivity, "Some error occurred!!", Toast.LENGTH_SHORT ).show()
                }
            }, Response.ErrorListener {
                rlOTP.visibility = View.VISIBLE
                progressLayout.visibility=View.GONE
                progressBar.visibility = View.GONE
                Toast.makeText(this@ResetPasswordActivity, it.message, Toast.LENGTH_SHORT).show()
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
