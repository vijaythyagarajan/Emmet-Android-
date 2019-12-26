package com.example.emmet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_emmet_sign_in.*
import kotlinx.android.synthetic.main.network_result_view.view.*
import java.io.IOException

class EmmetSignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private  lateinit var storageInstance: FirebaseStorage
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private lateinit var database: DatabaseReference

    private val PICK_IMAGE_REQUEST = 71

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emmet_sign_in)
        overridePendingTransition(R.anim.slide_left, R.anim.slide_right)

        auth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        database = FirebaseDatabase.getInstance().reference

        createAccount.setOnClickListener {
            CreateAccount()
        }

        myImage.setOnClickListener {
            launchGallery()
        }

    }

    fun CreateAccount() {
        var val1 = username.text
        var val2 = passcode.text
        var val3 = phonenumber.text


        auth.createUserWithEmailAndPassword(val1.toString(), val2.toString())?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = User(val1.toString(),val2.toString())
                database.child("users").child(auth.currentUser?.uid.toString())
                database.child("users").child(auth.currentUser?.uid.toString()).child("phoneNumber").setValue(val3.toString())
                database.child("users").child(auth.currentUser?.uid.toString()).child("userName").setValue(email.text.toString())

                database.child("users").child(auth.currentUser?.uid.toString()).child("emailAddress").setValue(val1.toString())
                database.child("users").child(auth.currentUser?.uid.toString()).child("searchString").setValue("")
                database.child("users").child(auth.currentUser?.uid.toString()).child("searchString").setValue("")
                database.child("users").child(auth.currentUser?.uid.toString()).child("country").setValue("us")
                if(checkBox.isChecked)
                {
                    database.child("users").child(auth.currentUser?.uid.toString()).child("loggedIn").setValue("true")
                }
                else {
                    database.child("users").child(auth.currentUser?.uid.toString()).child("loggedIn").setValue("false")
                }

                // Sign in success, update UI with the signed-in user's information


                val i = Intent(this, MainActivity::class.java)
                startActivity(i)

            } else {
                val networkView = layoutInflater.inflate(R.layout.network_result_view,null)
                networkView.ExpressionTextView.text = "Sorry!"
                networkView.errorTextView.text = "Unable to Create Account.Check the entered information and try again"
                networkView.erroricon.setImageResource(R.drawable.failedsignin)
                var builder = AlertDialog.Builder(this).setView(networkView)
                val mAlert = builder.show()
                mAlert.window.setGravity(Gravity.TOP)
            }

        }
    }


    private fun launchGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePath = data.data
            uploadImage()
            try {
                // val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                // uploadImage.setImageBitmap(bitmap)
            } catch (e: IOException) {
                //  e.printStackTrace()
            }
        }
    }

    private fun uploadImage(){
        if(filePath != null){
            val ref = storageReference?.child("uploads/myPhoto")
            val uploadTask = ref?.putFile(filePath!!)

            val urlTask = uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    Glide.with(this).load(downloadUri.toString()).into(myImage)
                    // addUploadRecordToDb(downloadUri.toString())
                } else {
                    // Handle failures
                }
            }?.addOnFailureListener{

            }
        }else{
            // Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }
}


data class User(
    var phonenumber: String? = "",
    var email: String? = ""

)
