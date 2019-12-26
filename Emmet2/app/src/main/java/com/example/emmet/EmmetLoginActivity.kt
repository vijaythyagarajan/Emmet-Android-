package com.example.emmet

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_emmet_login.*
import kotlinx.android.synthetic.main.forgot_passcode.*
import kotlinx.android.synthetic.main.network_result_view.view.*
import java.util.concurrent.TimeUnit


class EmmetLoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private lateinit var database: DatabaseReference

    var isUp = false
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    val phoneNumber = "+13152787146"
    val smsCode = "123456"
    private var smsCodeView:TextView? = null
    private  var currentUser : FirebaseUser?= null


    val loginChecker = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot) {

            print("check")
            val user = FirebaseAuth.getInstance().currentUser
            if(user != null && dataSnapshot.getValue() != null) {
                val map = dataSnapshot.getValue()

                if(map == "true") {
                    val sharedPref = this@EmmetLoginActivity?.getPreferences(Context.MODE_PRIVATE) ?: return
                    with (sharedPref.edit()) {
                        putString(getString(R.string.important),user.toString())
                        commit()
                    }
                    val i = Intent(this@EmmetLoginActivity,MainActivity::class.java)
                    startActivity(i)
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            println("loadPost:onCancelled ${databaseError.toException()}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emmet_login)
        val sharedPref = this@EmmetLoginActivity.getSharedPreferences(
            getString(R.string.important), Context.MODE_PRIVATE)

        mAuth = FirebaseAuth.getInstance()
        val firebaseAuthSettings = mAuth?.firebaseAuthSettings
        firebaseAuthSettings?.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, smsCode)
        database = FirebaseDatabase.getInstance().reference

        val user = FirebaseAuth.getInstance().currentUser

        database.child("users").child(mAuth?.currentUser?.uid.toString()).child("loggedIn").addValueEventListener(loginChecker)


        var forgotPasscode = findViewById(R.id.forgotPasscodeButton) as Button

        forgotPasscode.setOnClickListener {
            var ntk = isOnline(this)

            if(!ntk) {
                val networkView = layoutInflater.inflate(R.layout.network_result_view,null)
                networkView.ExpressionTextView.text = "Whoops!"
                networkView.errorTextView.text = "No Internet Connection."
                var builder = AlertDialog.Builder(this).setView(networkView)
                val mAlert = builder.show()
                mAlert.window.setGravity(Gravity.TOP)
            }

            if(ntk) {

                startPhoneNumberVerification(phoneNumber)

//                val dialogView = layoutInflater.inflate(R.layout.forgot_passcode, null)
//                var builder = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).setPositiveButton("Reset",{dialog,whichButton ->
//                    reset()
//                }).
//                    setNegativeButton("Back", { dialog, whichButton ->
//                    dialog.dismiss()
//                })
//                val mAlert = builder.show()
//                mAlert.window.setGravity(Gravity.BOTTOM)
//                mAlert.window.setBackgroundDrawableResource(R.drawable.corner_radius_view)
//                mAlert.window.attributes.windowAnimations = R.style.PauseDialogAnimation
//                smsCodeView = mAlert.smsCodeTxt



//                mAlert.checkSmsCodeButton.setOnClickListener {
//                    if(storedVerificationId.isNullOrBlank() && smsCodeTxt.text.toString().isNullOrBlank()) {
////                        verifyPhoneNumberWithCode(
////                            storedVerificationId,
////                            mAlert.smsCodeTxt.text.toString()
////                        )
////                    }
////                }



            }

        }

        val signInButton = findViewById(R.id.signinbutton) as Button

        signInButton.setOnClickListener {

            sigIn(emailtxtfield.text.toString(), passcodetxtfield.text.toString())
        }

        val signUpButton = findViewById(R.id.signupbutton) as Button

        signUpButton.setOnClickListener {
            database.child("users").child(mAuth?.currentUser?.uid.toString()).child("loggedIn").removeEventListener(loginChecker)
            val i = Intent(this, EmmetSignInActivity::class.java)
            startActivity(i)

        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                verificationInProgress = false
                print(credential)
                Toast.makeText(this@EmmetLoginActivity,"Please Update your password in Settings page.",Toast.LENGTH_LONG).show()
                val handler = Handler()
                handler.postDelayed(Runnable {
                    val i = Intent(this@EmmetLoginActivity,MainActivity::class.java)
                    startActivity(i)
                }, 2000)

            }

            override fun onVerificationFailed(e: FirebaseException) {

                verificationInProgress = false
                Toast.makeText(this@EmmetLoginActivity,"Unable to verify .Try again later",Toast.LENGTH_LONG).show()

                if (e is FirebaseAuthInvalidCredentialsException) {

                } else if (e is FirebaseTooManyRequestsException) {

                }

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                val dialogView = layoutInflater.inflate(R.layout.forgot_passcode, null)
                var builder = AlertDialog.Builder(this@EmmetLoginActivity).setView(dialogView).setCancelable(false).setPositiveButton("Reset",{dialog,whichButton ->
                    if(storedVerificationId.isNullOrBlank() && smsCodeTxt.text.toString().isNullOrBlank()) {
                        verifyPhoneNumberWithCode(
                            storedVerificationId,
                            smsCodeView?.text.toString()
                       )
                    }
                }).
                    setNegativeButton("Back", { dialog, whichButton ->
                        dialog.dismiss()
                    })
                val mAlert = builder.show()
                mAlert.window.setGravity(Gravity.BOTTOM)
                mAlert.window.setBackgroundDrawableResource(R.drawable.corner_radius_view)
                mAlert.window.attributes.windowAnimations = R.style.PauseDialogAnimation
                smsCodeView = mAlert.smsCodeTxt

                storedVerificationId = verificationId
                resendToken = token
            }
        }

    }

    override fun onStop() {
        super.onStop()
        database.child("users").child(mAuth?.currentUser?.uid.toString()).child("loggedIn").removeEventListener(loginChecker)

    }

    fun reset() {
        var user = mAuth?.currentUser
        val txtNewPass =  smsCodeView
        if(user == null ) {
            val sharedPref = this?.getPreferences(Context.MODE_PRIVATE) ?: return
            val highScore = sharedPref.getString(R.string.important.toString(),user)
        }
        if(user != null) {
            user!!.updatePassword(txtNewPass?.text.toString()).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Update Success")
                    Toast.makeText(this, "Password Reset Successful", Toast.LENGTH_LONG).show()
                } else {
                    println("Error")
                    Toast.makeText(
                        this,
                        "Unable to reset Password Contact Administrator",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }


    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks) // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        verificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = task.result?.user
                    currentUser = user
                    val i = Intent(this, MainActivity::class.java)
                    startActivity(i)

                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val networkView = layoutInflater.inflate(R.layout.network_result_view,null)
                        networkView.ExpressionTextView.text = "Sorry!"
                        networkView.errorTextView.text = "In valid code.Enter an appropriate SMS code."
                        networkView.erroricon.setImageResource(R.drawable.failedsignin)
                        var builder = AlertDialog.Builder(this).setView(networkView)
                        val mAlert = builder.show()
                        mAlert.window.setGravity(Gravity.TOP)
                    }

                }
            }
    }

    fun sigIn(userEmailAddress:String, userEnteredPassword:String) {

        if(isOnline(this)) {
            print("online")

            if(userEmailAddress.isNotEmpty() && userEnteredPassword.isNotEmpty()) {
                mAuth?.signInWithEmailAndPassword(userEmailAddress.toString(),userEnteredPassword.toString())?.addOnCompleteListener {
                    if(it.isSuccessful) {
                        // successful login

//                        val circularProgessView = layoutInflater.inflate(R.layout.circular_loading_view,null)
//                        circularProgessView.progressbar.isIndeterminate = true
//                        var builder = AlertDialog.Builder(this).setView(circularProgessView)
//                        val mAlert = builder.show()
//                        mAlert.window.setGravity(Gravity.CENTER)
                        currentUser = mAuth?.currentUser
                        val i = Intent(this, MainActivity::class.java)
                        startActivity(i)

                    }
                    else {
                        // failure
                        val networkView = layoutInflater.inflate(R.layout.network_result_view,null)
                        networkView.ExpressionTextView.text = "Sorry!"
                        networkView.errorTextView.text = "Unable to Sign In.Check your email address and passCode and try again"
                        networkView.erroricon.setImageResource(R.drawable.failedsignin)
                        var builder = AlertDialog.Builder(this).setView(networkView)
                        val mAlert = builder.show()
                        mAlert.window.setGravity(Gravity.TOP)
                    }
                }
            }

        }
        else {

            val networkView = layoutInflater.inflate(R.layout.network_result_view,null)
            networkView.ExpressionTextView.text = "Whoops!"
            networkView.errorTextView.text = "No Internet Connection."
            var builder = AlertDialog.Builder(this).setView(networkView)
            val mAlert = builder.show()
            mAlert.window.setGravity(Gravity.TOP)

        }


    }

    fun getUserDetails() {
        var currentUser = mAuth?.currentUser
        currentUser = currentUser
        if(currentUser != null) {
            val userName = currentUser.displayName
            val userEmailAddress = currentUser.email
        }

    }

    fun slideUp(view: View) {
        view.setVisibility(View.VISIBLE)
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            view.height.toFloat(),  // fromYDelta
            0F
        ) // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    // slide the view from its current position to below itself
    fun slideDown(view: View) {
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            0F,  // fromYDelta
            view.height.toFloat()
        ) // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // [END phone_auth_callbacks]
}

