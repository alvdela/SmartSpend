package com.alvdela.smartspend.ui.activity

import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alvdela.smartspend.ContextFamily
import com.alvdela.smartspend.R
import com.alvdela.smartspend.model.Child
import com.alvdela.smartspend.model.Family
import com.alvdela.smartspend.model.Member
import com.alvdela.smartspend.model.Parent
import com.alvdela.smartspend.ui.widget.TaskChildWidget
import com.alvdela.smartspend.ui.widget.TaskParentWidget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class ProfilesActivity : AppCompatActivity() {

    private lateinit var profilesButtons: MutableList<Button>

    private lateinit var familyName: TextView
    private lateinit var passwordInput: EditText

    private lateinit var family: Family
    private lateinit var email: String

    private lateinit var dialog: Dialog

    companion object {
        private lateinit var widgetParent: TaskParentWidget
        private lateinit var widgetChild: TaskChildWidget
        private lateinit var mAppWidgetManager: AppWidgetManager

        private fun initWidgets(context: Context) {
            widgetParent = TaskParentWidget()
            widgetChild = TaskChildWidget()
            mAppWidgetManager = AppWidgetManager.getInstance(context)
            updateWidgets(context)
        }

        fun updateWidgets(context: Context) {
            if (!ContextFamily.isMock) {
                var intent = Intent(context, TaskParentWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    val ids = mAppWidgetManager.getAppWidgetIds(
                        ComponentName(
                            context,
                            TaskParentWidget::class.java
                        )
                    )
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
                Handler().postDelayed({
                    intent = Intent(context, TaskChildWidget::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        val ids = mAppWidgetManager.getAppWidgetIds(
                            ComponentName(
                                context,
                                TaskChildWidget::class.java
                            )
                        )
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(intent)
                }, 5000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profiles)
        getFamily()
        initObjects()
        family.checkChildrenPayments()
    }

    override fun onStart() {
        super.onStart()
        getFamily()
        initObjects()
        family.checkChildrenPayments()
    }

    private fun getFamily() {
        family = ContextFamily.family!!
        email = family.getEmail()
    }

    private fun initObjects() {
        setViews()
        hideButtons()
        showFamilyData()
        if (!ContextFamily.isMock) initWidgets(this)
    }

    private fun setViews() {
        profilesButtons = mutableListOf()
        val button0: Button = findViewById(R.id.button0)
        profilesButtons.add(button0)
        val button1: Button = findViewById(R.id.button1)
        profilesButtons.add(button1)
        val button2: Button = findViewById(R.id.button2)
        profilesButtons.add(button2)
        val button3: Button = findViewById(R.id.button3)
        profilesButtons.add(button3)
        val button4: Button = findViewById(R.id.button4)
        profilesButtons.add(button4)
        val button5: Button = findViewById(R.id.button5)
        profilesButtons.add(button5)
        val button6: Button = findViewById(R.id.button6)
        profilesButtons.add(button6)
        val button7: Button = findViewById(R.id.button7)
        profilesButtons.add(button7)
        val button8: Button = findViewById(R.id.button8)
        profilesButtons.add(button8)
        val button9: Button = findViewById(R.id.button9)
        profilesButtons.add(button9)
        val button10: Button = findViewById(R.id.button10)
        profilesButtons.add(button10)
        val button11: Button = findViewById(R.id.button11)
        profilesButtons.add(button11)
        //Aqui finaliza la creación de los botones de inicio de seción
        dialog = Dialog(this)

        familyName = findViewById(R.id.familyName)
        familyName.text = getString(R.string.familia_display, family.getName())
    }

    private fun hideButtons() {
        for (button in profilesButtons) {
            button.visibility = View.INVISIBLE
        }
    }

    private fun showFamilyData() {

        var i = 0
        for ((clave, member) in family.getMembers()) {
            profilesButtons[i].visibility = View.VISIBLE
            profilesButtons[i].text = clave
            profilesButtons[i].tag = clave
            if (!ContextFamily.isMock) {
                showProfilePicture(profilesButtons[i], member)
            }
            i++
        }
    }

    private fun showProfilePicture(button: Button, member: Member) {
        val uuid = FirebaseAuth.getInstance().currentUser!!.uid
        val fileName = member.getId()

        val storageRef = FirebaseStorage.getInstance().reference.child("images/$uuid/$fileName")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localFile)
            .addOnSuccessListener {
                if (localFile.exists() && localFile.length() > 0) {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 260, 260, true)
                    val circularImage = getCroppedBitmap(scaledBitmap)
                    val drawable: Drawable = BitmapDrawable(resources, circularImage)
                    button.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
                }
            }
            .addOnFailureListener {
                //Toast.makeText(this, "Fallo al obtener imagen de perfil", Toast.LENGTH_LONG).show()
            }
    }

    private fun getCroppedBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(
            bitmap.getWidth(),
            bitmap.getHeight(), Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.getWidth(), bitmap.getHeight())
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.setColor(color)
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(
            (bitmap.getWidth() / 2).toFloat(), (bitmap.getHeight() / 2).toFloat(),
            (
                    bitmap.getWidth() / 2).toFloat(), paint
        )
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output
    }

    fun triggerGoMain(view: View) {
        goMain(view.tag.toString())
    }

    private fun goMain(user: String) {
        val member = family.getMember(user)
        var acceder = false
        showPopUp(R.layout.pop_up_profile)
        initShowButtons()

        val tvProfileName = dialog.findViewById<TextView>(R.id.tvProfile)
        val unlockImage = dialog.findViewById<ImageView>(R.id.ivUnlock)
        val passwordContainer = dialog.findViewById<RelativeLayout>(R.id.passwordContainer)
        val accessButton = dialog.findViewById<Button>(R.id.accessButton)
        val passwordWrong = dialog.findViewById<TextView>(R.id.tvWrongPassword)
        passwordWrong.visibility = View.GONE
        passwordInput = dialog.findViewById(R.id.passwordProfile)
        val forgetButton = dialog.findViewById<TextView>(R.id.forgetButton)

        tvProfileName.text = user

        if (member != null) {
            if (member.checkPassword("")) {
                acceder = true
                passwordContainer.visibility = View.GONE
                forgetButton.visibility = View.GONE
                unlockImage.visibility = View.VISIBLE
            } else {
                forgetButton.setOnClickListener {
                    dialog.dismiss()
                    forgetPassword(member)
                }
                passwordContainer.visibility = View.VISIBLE
                unlockImage.visibility = View.GONE
                passwordInput.setText("")

            }
        } else {
            Toast.makeText(this, "Error: Usuario no existente", Toast.LENGTH_SHORT).show()
        }
        accessButton.setOnClickListener {
            if (acceder) {
                if (member is Parent) {
                    dialog.dismiss()
                    goParentMain(user)
                } else if (member is Child) {
                    dialog.dismiss()
                    goChildMain(user)
                }
            } else if (member!!.checkPassword(passwordInput.text.toString())) {
                if (member is Parent) {
                    dialog.dismiss()
                    goParentMain(user)
                } else if (member is Child) {
                    dialog.dismiss()
                    goChildMain(user)
                }
            } else {
                passwordWrong.visibility = View.VISIBLE
            }
        }

    }

    private fun forgetPassword(member: Member?) {
        showPopUp(R.layout.pop_up_forgot_password)
        val passwordProfile = dialog.findViewById<EditText>(R.id.passwordForget)
        val tvWrongPassword = dialog.findViewById<TextView>(R.id.tvWrongPassword)
        tvWrongPassword.visibility = View.GONE
        val passwordButton = dialog.findViewById<CheckBox>(R.id.show_password)
        passwordButton.setOnCheckedChangeListener { _, isChecked ->
            passwordProfile.transformationMethod =
                if (isChecked) null else PasswordTransformationMethod.getInstance()
        }

        if (ContextFamily.isMock) {
            val tvInfo = dialog.findViewById<TextView>(R.id.tvInfo)
            tvInfo.text = resources.getString(R.string.forget_in_mock)
            val passwordContainer = dialog.findViewById<RelativeLayout>(R.id.passwordContainer)
            passwordContainer.visibility = View.GONE
        }

        val accessForgetButton = dialog.findViewById<Button>(R.id.accessForgetButton)
        accessForgetButton.setOnClickListener {
            if (ContextFamily.isMock) {
                member!!.setPassword("")
                if (member is Parent) {
                    dialog.dismiss()
                    goParentMain(member.getUser())
                } else if (member is Child) {
                    dialog.dismiss()
                    goChildMain(member.getUser())
                }
            } else {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email, passwordProfile.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            member!!.setPassword("")
                            if (member is Parent) {
                                dialog.dismiss()
                                goParentMain(member.getUser())
                            } else if (member is Child) {
                                dialog.dismiss()
                                goChildMain(member.getUser())
                            }
                        } else {
                            tvWrongPassword.visibility = View.VISIBLE
                        }
                    }
            }
        }

    }

    private fun goParentMain(user: String) {
        val intent = Intent(this, MainParentsActivity::class.java).apply {
            putExtra("USER_NAME", user)
        }
        startActivity(intent)
    }

    private fun goChildMain(user: String) {
        val intent = Intent(this, MainChildrenActivity::class.java).apply {
            putExtra("USER_NAME", user)
        }
        startActivity(intent)
    }

    @Deprecated("Deprecated")
    override fun onBackPressed() {
        showPopUp(R.layout.pop_up_log_out)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButtonLogOut)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButtonLogOut)
        confirmButton.setOnClickListener {
            ContextFamily.reset()
            startActivity(Intent(this, LoginActivity::class.java))
            super.onBackPressed()
        }
    }

    private fun initShowButtons() {
        val passwordButton = dialog.findViewById<CheckBox>(R.id.show_password)
        passwordButton.setOnCheckedChangeListener { _, isChecked ->
            passwordInput.transformationMethod =
                if (isChecked) null else PasswordTransformationMethod.getInstance()
        }
    }

    private fun showPopUp(layout: Int) {
        dialog = Dialog(this)
        dialog.setContentView(layout)
        dialog.setCancelable(true)
        dialog.show()
    }

}