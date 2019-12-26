package com.example.emmet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.PopupMenu
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_emmet_sign_in.*
import kotlinx.android.synthetic.main.fragment_main_top_view.view.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import java.io.IOException


class Settings : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var settingsV:View ?= null
    private  lateinit var storageInstance: FirebaseStorage
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private val PICK_IMAGE_REQUEST = 71

    var actionMode: ActionMode? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference
        auth = FirebaseAuth.getInstance()


        database.child("users").child(auth.currentUser?.uid.toString()).child("userName").addValueEventListener(loginChecker)
        database.child("users").child(auth.currentUser?.uid.toString()).child("country").addValueEventListener(loginChecker)


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
                    Glide.with(context).load(downloadUri.toString()).into(settingsV?.myImage)
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

    val loginChecker = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot) {

            if(dataSnapshot.key == "userName")  {
                settingsV?.username?.text = dataSnapshot.value.toString()
            }
            if(dataSnapshot.key == "country") {
                if(dataSnapshot.value == "us") {
                    settingsV?.countryFlag?.setImageResource(R.drawable.india)
                }
                else {
                    settingsV?.countryFlag?.setImageResource(R.drawable.united_states)
                }
            }
            if(dataSnapshot.key == "loggedIn") {
                if(dataSnapshot.value == "true") {
                    settingsV?.checkBox?.isChecked = true
                }
                else {
                    settingsV?.checkBox?.isChecked = false
                }
            }


        }

        override fun onCancelled(databaseError: DatabaseError) {
            println("loadPost:onCancelled ${databaseError.toException()}")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var settingsView = inflater.inflate(R.layout.fragment_settings, container, false)
        settingsV = settingsView

        settingsView.reportBug.setOnClickListener {
            sendEmail("tester@emmet.com","Emmet Issue","")
        }

        settingsView.country.setOnClickListener {
            actionMode = this.activity?.startActionMode(ActionModeCallback())

            val popupMenu: PopupMenu = PopupMenu(context,settingsView.country)
            popupMenu.menuInflater.inflate(R.menu.select_country,popupMenu.menu)

            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->

                when(item.itemId) {
                    R.id.usa -> {
                        countryFlag.setImageResource(R.drawable.united_states)
                        database.child("users").child(auth.currentUser?.uid.toString()).child("country").setValue("us")

                    }

                    R.id.india -> {
                        //Toast.makeText(context,"Pasted",Toast.LENGTH_SHORT)
                        countryFlag.setImageResource(R.drawable.india)
                        database.child("users").child(auth.currentUser?.uid.toString()).child("country").setValue("in")
                    }
                }
                true
            })

            popupMenu.show()

        }

        settingsView.updateInfo.setOnClickListener {
//            database.child("users").child("phoneNumber").setValue(phonenumber.text.toString())
//            database.child("users").child("userName").setValue(userName.text.toString())
            if(settingsView.checkBox.isChecked) {
                database.child("users").child(auth.currentUser?.uid.toString()).child("loggedIn").setValue("true")
            }
            else {
                database.child("users").child(auth.currentUser?.uid.toString()).child("loggedIn").setValue("false")
            }
            var user = auth?.currentUser
            if(user != null) {
                var passcodes = settingsView.passcode.text.toString()
                if(passcodes.length > 0) {
                    user!!.updatePassword(passcodes).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            println("Update Success")
                            Toast.makeText(context, "Password Reset Successful", Toast.LENGTH_LONG)
                        } else {
                            println("Error")
                            Toast.makeText(
                                context,
                                "Unable to reset Password Contact Administrator",
                                Toast.LENGTH_LONG
                            )
                        }
                    }
                }
            }

            if(settingsView.userName.text.length > 0) {
                database.child("users").child(auth.currentUser?.uid.toString()).child("userName")
                    .setValue(settingsView.userName.text.toString())
            }
        }

        settingsView.SignOut.setOnClickListener {
            auth.signOut()
            getActivity()?.onBackPressed()
        }

        settingsView.myImage.setOnClickListener {
            launchGallery()
        }


        return settingsView
    }




    inner class ActionModeCallback : ActionMode.Callback {
        var shouldResetRecyclerView = true
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.getItemId()) {
                R.id.usa-> {
                }
                R.id.india ->{

                }

            }
            return false
        }
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }
        override fun onDestroyActionMode(mode: ActionMode?) {

        }
    }


    private fun sendEmail(recipient: String, subject: String, message: String) {
        /*ACTION_SEND action to launch an email client installed on your Android device.*/
        val mIntent = Intent(Intent.ACTION_SEND)
        /*To send an email you need to specify mailto: as URI using setData() method
        and data type will be to text/plain using setType() method*/
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"
        // put recipient email in intent
        /* recipient is put as array because you may wanna send email to multiple emails
           so enter comma(,) separated emails, it will be stored in array*/
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        //put the Subject in the intent
        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        //put the message in the intent
        mIntent.putExtra(Intent.EXTRA_TEXT, message)


        try {
            //start email intent
            startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
        }
        catch (e: Exception){
            //if any thing goes wrong for example no email client application or any exception
            //get and show exception message
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }

    }

}
