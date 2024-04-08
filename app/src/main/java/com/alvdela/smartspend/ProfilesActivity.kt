package com.alvdela.smartspend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import com.alvdela.smartspend.domain.Child
import com.alvdela.smartspend.domain.Family
import com.alvdela.smartspend.domain.Parent

class ProfilesActivity : AppCompatActivity() {

    private lateinit var profilesButtons: MutableList<Button>
    private lateinit var familyName: TextView
    private lateinit var popUpProfile: ConstraintLayout

    private lateinit var family: Family

    private var popUpShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profiles)
        getFamily()
        initObjects()
        hideButtons()
        showFamilyData()
    }

    private fun getFamily() {
        if (ContextFamily.mockFamily != null) {
            family = ContextFamily.mockFamily!!
        }else{
            //TODO consulta a firebase
        }
    }

    private fun showFamilyData() {
        var i = 0
        for ((clave, valor) in family.getMembers()) {
            profilesButtons[i].visibility = View.VISIBLE
            profilesButtons[i].text = clave
            profilesButtons[i].tag = clave
            i++
        }
    }

    private fun hideButtons() {
        for (button in profilesButtons) {
            button.visibility = View.INVISIBLE
        }
    }

    private fun initObjects() {
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
        popUpProfile = findViewById(R.id.popUpProfile)
        val closeButton = findViewById<ImageView>(R.id.closePopUpProfiles)
        closeButton.setOnClickListener {
            popUpProfile.visibility = View.GONE
            popUpShown = false
        }

        familyName = findViewById(R.id.familyName)
        familyName.text = getString(R.string.familia_display, family.getName())
    }

    fun triggerGoMain(view: View) {
        goMain(view.tag.toString())
    }

    private fun goMain(user: String) {
        val member = family.getMember(user)
        var acceder = false
        popUpProfile.visibility = View.VISIBLE
        popUpShown = true
        val tvProfileName = findViewById<TextView>(R.id.tvProfile)
        tvProfileName.text = user
        val passwordInput = findViewById<EditText>(R.id.passwordProfile)
        val unlockImage = findViewById<ImageView>(R.id.ivUnlock)
        if (member != null) {
            if (member.checkPassword("")){
                acceder = true
                passwordInput.visibility = View.GONE
                unlockImage.visibility = View.VISIBLE
            }else{
                passwordInput.visibility = View.VISIBLE
                unlockImage.visibility = View.GONE
                passwordInput.setText("")
            }
        }else{
            Toast.makeText(this,"Error: Usuario no existente", Toast.LENGTH_SHORT).show()
        }
        val accessButton = findViewById<Button>(R.id.accessButton)
        accessButton.setOnClickListener {
            if (acceder){
                if (member is Parent){
                    goParentMain(user)
                }else if(member is Child){
                    goChildMain(user)
                }
            }else if (member!!.checkPassword(passwordInput.text.toString())){
                if (member is Parent){
                    goParentMain(user)
                }else if(member is Child){
                    goChildMain(user)
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
        if (popUpShown) {
            popUpProfile.visibility = View.GONE
            popUpShown = false
        } else {
            //TODO mostrar mensaje si quiere salir
            super.onBackPressed()
        }
    }
}