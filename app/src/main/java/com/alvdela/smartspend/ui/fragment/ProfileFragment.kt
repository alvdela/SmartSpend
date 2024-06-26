package com.alvdela.smartspend.ui.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.alvdela.smartspend.FamilyManager
import com.alvdela.smartspend.R
import com.alvdela.smartspend.filters.Validator
import com.alvdela.smartspend.util.Constants
import com.alvdela.smartspend.model.Member
import com.alvdela.smartspend.model.Parent
import com.alvdela.smartspend.ui.activity.CameraActivity
import com.alvdela.smartspend.ui.activity.LoginActivity
import com.alvdela.smartspend.ui.activity.ProfilesActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ProfileFragment : Fragment() {
    private var user: String? = null
    private lateinit var member: Member
    private lateinit var uid: String

    private lateinit var view: View

    private lateinit var tvUserName: TextView
    private lateinit var tvNombreFamilia: TextView

    private lateinit var dialog: Dialog

    private val MAX_USER_LENGHT = 10
    private val MAX_FAMILY_NAME = 30

    companion object {
        const val USER_BUNDLE = "user_bundle"
        var configProfileOpen = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getString(USER_BUNDLE)
        }
        member = FamilyManager.family!!.getMember(user!!)!!
        if (!FamilyManager.isMock) {
            uid = FirebaseAuth.getInstance().currentUser!!.uid
        }
        configProfileOpen = true
    }

    override fun onResume() {
        super.onResume()
        if (!FamilyManager.isMock )showProfilePicture()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.view = view

        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserName.text = user

        tvNombreFamilia = view.findViewById(R.id.tvNombreFamilia)
        tvNombreFamilia.text = FamilyManager.family!!.getName()

        if (FamilyManager.family!!.isParent(user!!)) {
            val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
            tvEmail.text = FamilyManager.family!!.getEmail()
        } else {
            val lyEmail = view.findViewById<LinearLayout>(R.id.lyEmailProfile)
            lyEmail.visibility = View.GONE
        }

        val tvPasswordProfile = view.findViewById<TextView>(R.id.tvPasswordProfile)
        if (member.getPassword() == ""){
            tvPasswordProfile.text = ""
        }else{
            tvPasswordProfile.text = "********"
        }

        initButtons()
        if (!FamilyManager.isMock) showProfilePicture()

        val toolbarProfile = view.findViewById<Toolbar>(R.id.toolbar_profile)
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(toolbarProfile)
        toolbarProfile.setNavigationOnClickListener {
            // Cerrar el Fragment
            configProfileOpen = false
            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    private fun initButtons() {
        val btEditUserName = view.findViewById<ImageView>(R.id.btEditUserName)
        btEditUserName.setOnClickListener {
            editUserName()
        }

        val btEditFamilyName = view.findViewById<ImageView>(R.id.btEditFamilyName)
        btEditFamilyName.setOnClickListener {
            editFamilyName()
        }

        val btEditEmail = view.findViewById<ImageView>(R.id.btEditEmail)
        if (FamilyManager.isMock){
            btEditEmail.visibility = View.GONE
        }else{
            btEditEmail.setOnClickListener {
                editEmail()
            }
        }

        val btEditPassword = view.findViewById<ImageView>(R.id.btEditPassword)
        btEditPassword.setOnClickListener {
            editPassword()
        }

        val changeProfilePicture = view.findViewById<FloatingActionButton>(R.id.changeProfilePicture)
        changeProfilePicture.setOnClickListener {
            changePicture()
        }

        val btDeleteMember = view.findViewById<TextView>(R.id.btDeleteMember)
        btDeleteMember.setOnClickListener {
            if (!FamilyManager.isMock) {
                deleteProfile()
            } else{
                Toast.makeText(
                    requireContext(),
                    "No se puede eliminar la familia de prueba",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (!FamilyManager.family!!.isParent(user!!)) {
            btEditFamilyName.visibility = View.GONE
            btEditEmail.visibility = View.GONE
            btDeleteMember.visibility = View.GONE
        }
    }

    private fun showProfilePicture() {
        val uuid = FirebaseAuth.getInstance().currentUser!!.uid
        val fileName = FamilyManager.family!!.getMember(user!!)!!.getId()

        val storageRef = FirebaseStorage.getInstance().reference.child("images/$uuid/$fileName")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localFile)
            .addOnSuccessListener {
                if (localFile.exists() && localFile.length() > 0) {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)

                    if (bitmap != null) {
                        val profileView = view.findViewById<ImageView>(R.id.profileView)
                        profileView.scaleType = ImageView.ScaleType.FIT_CENTER
                        profileView.setImageBitmap(bitmap)
                    }
                }
            }
            .addOnFailureListener{
            }
    }

    private fun changePicture() {
        if (!FamilyManager.isMock){
            val intent = Intent(requireContext(), CameraActivity::class.java).apply {
                putExtra("USER_NAME", user)
            }
            startActivity(intent)
        }else{
            Toast.makeText(requireContext(), "No se puede cambiar la foto en el modo invitado", Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteProfile() {
        showPopUp(R.layout.pop_up_delete_profile)

        val passwordInput = dialog.findViewById<EditText>(R.id.passwordInput)
        passwordInput.setText("")

        val tvNota = dialog.findViewById<TextView>(R.id.tvNota)

        val buttonCancelDeleteProfile = dialog.findViewById<Button>(R.id.buttonCancelDeleteProfile)
        buttonCancelDeleteProfile.setOnClickListener {
            dialog.dismiss()
        }

        var parents = 0
        var childs = 0
        for ((_, member) in FamilyManager.family!!.getMembers()) {
            if (member is Parent) {
                parents++
            } else {
                childs++
            }
        }

        val buttonConfirmDeleteProfile =
            dialog.findViewById<Button>(R.id.buttonConfirmDeleteProfile)

        buttonConfirmDeleteProfile.setOnClickListener {
            if (passwordInput.text.toString().isBlank()) {
                passwordInput.error = "Es necesaria la contraseña"
            } else {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(
                        FamilyManager.family!!.getEmail(),
                        passwordInput.text.toString()
                    )
                    .addOnSuccessListener {
                        if (parents == 1 && childs != 0) {
                            tvNota.text =
                                "Es necesario que siempre haya un perfil de adulto. Por favor, borre el resto de perfiles e intentelo de nuevo"
                            tvNota.setTextColor(Color.RED)
                        }
                        if (parents > 1) {
                            deleteMemberFromDatabase(user!!)
                        }
                        if (parents == 1 && childs == 0) {
                            deleteMemberFromDatabase(user!!)
                            deleteTasksFromDatabase(Constants.TASKS)
                            deleteTasksFromDatabase(Constants.HISTORIC)
                            deleteFamily()

                        }
                    }
                    .addOnFailureListener {
                        tvNota.text = resources.getString(R.string.password_wrong)
                        tvNota.setTextColor(Color.RED)
                    }
            }
        }
    }

    private fun editPassword() {
        showPopUp(R.layout.pop_up_change_password)

        val tvInfo1 = dialog.findViewById<TextView>(R.id.tvInfo1)
        val oldPasswordInput = dialog.findViewById<EditText>(R.id.oldPasswordInput)
        val newPasswordInput = dialog.findViewById<EditText>(R.id.newPasswordInput)
        val repeatPasswordInput = dialog.findViewById<EditText>(R.id.repeatPasswordInput)
        initShowButtons()

        if (member.getPassword() == "") {
            tvInfo1.visibility = View.GONE
            val oldPasswordContainer =
                dialog.findViewById<RelativeLayout>(R.id.oldPasswordContainer)
            oldPasswordContainer.visibility = View.GONE
        }

        val tvWrongPassword = dialog.findViewById<TextView>(R.id.tvWrongPassword)
        tvWrongPassword.visibility = View.GONE
        val tvWrongSize = dialog.findViewById<TextView>(R.id.tvWrongSize)
        tvWrongSize.visibility = View.GONE
        val tvWrongRepeat = dialog.findViewById<TextView>(R.id.tvWrongRepeat)
        tvWrongRepeat.visibility = View.GONE
        initShowButtons()

        val buttonCancelNewPassword = dialog.findViewById<Button>(R.id.buttonCancelNewPassword)
        buttonCancelNewPassword.setOnClickListener {
            dialog.dismiss()
        }

        val buttonConfirmNewPassword = dialog.findViewById<Button>(R.id.buttonConfirmNewPassword)
        buttonConfirmNewPassword.setOnClickListener {
            tvWrongPassword.visibility = View.GONE
            tvWrongSize.visibility = View.GONE
            tvWrongRepeat.visibility = View.GONE
            var allOk = true
            if (member.getPassword() != "") {
                if (!member.checkPassword(oldPasswordInput.text.toString())) {
                    allOk = false
                    tvWrongPassword.visibility = View.VISIBLE
                }
            }
            if (newPasswordInput.text.isBlank()) {
                allOk = false
                newPasswordInput.error = "Es necesario una nueva contraseña"
            }
            if (newPasswordInput.text.toString().length < 4) {
                allOk = false
                tvWrongSize.visibility = View.VISIBLE
            }
            if (newPasswordInput.text.toString() != repeatPasswordInput.text.toString()) {
                allOk = false
                tvWrongRepeat.visibility = View.VISIBLE
            }
            if (allOk) {
                member.setPassword(newPasswordInput.text.toString())
                if (!FamilyManager.isMock) {
                    updatePasswordInDatabase()
                }
                Toast.makeText(requireContext(), "Contraseña cambiada", Toast.LENGTH_SHORT).show()
                val tvPasswordProfile = view.findViewById<TextView>(R.id.tvPasswordProfile)
                tvPasswordProfile.text = "********"
                dialog.dismiss()
            }
        }
    }

    private fun initShowButtons() {

        val showOldPassword = dialog.findViewById<CheckBox>(R.id.showOldPassword)
        val oldPasswordInput = dialog.findViewById<EditText>(R.id.oldPasswordInput)
        showOldPassword.setOnCheckedChangeListener{ _, isChecked ->
            oldPasswordInput.transformationMethod = if (isChecked) null else PasswordTransformationMethod.getInstance()
        }

        val showNewPassword = dialog.findViewById<CheckBox>(R.id.showNewPassword)
        val newPasswordInput = dialog.findViewById<EditText>(R.id.newPasswordInput)
        showNewPassword.setOnCheckedChangeListener{ _, isChecked ->
            newPasswordInput.transformationMethod = if (isChecked) null else PasswordTransformationMethod.getInstance()
        }

        val showRepeatPassword = dialog.findViewById<CheckBox>(R.id.showRepeatPassword)
        val repeatPasswordInput = dialog.findViewById<EditText>(R.id.repeatPasswordInput)
        showRepeatPassword.setOnCheckedChangeListener{ _, isChecked ->
            repeatPasswordInput.transformationMethod = if (isChecked) null else PasswordTransformationMethod.getInstance()
        }

        oldPasswordInput.setText("")
        newPasswordInput.setText("")
        repeatPasswordInput.setText("")
    }

    private fun editEmail() {
        showPopUp(R.layout.pop_up_change_email)

        val inputNewEmail = dialog.findViewById<EditText>(R.id.inputNewEmail)
        inputNewEmail.setText("")

        val buttonCancelChangeEmail = dialog.findViewById<Button>(R.id.buttonCancelChangeEmail)
        buttonCancelChangeEmail.setOnClickListener {
            dialog.dismiss()
        }

        val buttonConfirmChangeEmail = dialog.findViewById<Button>(R.id.buttonConfirmChangeEmail)
        buttonConfirmChangeEmail.setOnClickListener {
            val newEmail = inputNewEmail.text.toString()
            if (newEmail.isBlank()) {
                inputNewEmail.error = "Es necesario un nuevo email"
            } else if (!Validator.validateEmail(newEmail)) {
                inputNewEmail.error = "Email no valido"
            } else if (!FamilyManager.isMock) {
                val user = FirebaseAuth.getInstance().currentUser
                user?.verifyBeforeUpdateEmail(newEmail)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Enviado correo de confirmación",
                                Toast.LENGTH_SHORT
                            ).show()
                            FamilyManager.reset()
                            startActivity(Intent(requireContext(), LoginActivity::class.java))
                            dialog.dismiss()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Error al actualizar el correo electrónico",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun editUserName() {
        showPopUp(R.layout.pop_up_change_name)
        val tvChangeName = dialog.findViewById<TextView>(R.id.tvChangeName)
        tvChangeName.text = resources.getString(R.string.change_user_name)

        val inputNewName = dialog.findViewById<EditText>(R.id.inputNewName)
        inputNewName.setText(user)

        val tvWrongName = dialog.findViewById<TextView>(R.id.tvWrongName)
        tvWrongName.visibility = View.INVISIBLE

        val buttonCancelNewName = dialog.findViewById<Button>(R.id.buttonCancelNewName)
        buttonCancelNewName.setOnClickListener {
            dialog.dismiss()
        }

        val buttonConfirmNewName = dialog.findViewById<Button>(R.id.buttonConfirmNewName)
        buttonConfirmNewName.setOnClickListener {
            val name = inputNewName.text.toString()
            if (name.isBlank()) {
                inputNewName.error = "Es necesario un nuevo nombre"
            } else if (FamilyManager.family!!.checkName(name)) {
                tvWrongName.visibility = View.VISIBLE
            } else if (name.length > MAX_USER_LENGHT) {
                inputNewName.error = "Nombre demasiado largo. Máximo $MAX_USER_LENGHT caracteres."
            } else {
                if (!FamilyManager.isMock) {
                    updateUserNameInDatabase(name)
                } else {
                    FamilyManager.family!!.deleteMember(member.getUser())
                    member.setUser(name)
                    FamilyManager.family!!.addMember(member)
                    tvUserName.text = name
                    user = name
                    Toast.makeText(
                        requireContext(),
                        "Nombre actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    backToProfiles()
                }
                dialog.dismiss()

            }
        }
    }

    private fun editFamilyName() {
        showPopUp(R.layout.pop_up_change_name)
        val tvChangeName = dialog.findViewById<TextView>(R.id.tvChangeName)
        tvChangeName.text = resources.getString(R.string.change_family_name)

        val inputNewName = dialog.findViewById<EditText>(R.id.inputNewName)
        inputNewName.setText("")

        val tvWrongName = dialog.findViewById<TextView>(R.id.tvWrongName)
        tvWrongName.visibility = View.INVISIBLE

        val buttonCancelNewName = dialog.findViewById<Button>(R.id.buttonCancelNewName)
        buttonCancelNewName.setOnClickListener {
            dialog.dismiss()
        }

        val buttonConfirmNewName = dialog.findViewById<Button>(R.id.buttonConfirmNewName)
        buttonConfirmNewName.setOnClickListener {
            val name = inputNewName.text.toString()
            if (name.isBlank()) {
                inputNewName.error = "Es necesario un nuevo nombre"
            } else if (name.length > MAX_FAMILY_NAME) {
                inputNewName.error = "Nombre demasiado largo. Máximo 30 caracteres."
            } else {
                if (!FamilyManager.isMock) {
                    updateFamilyNameInDatabase(name)
                    tvNombreFamilia.text = FamilyManager.family!!.getName()
                } else {
                    FamilyManager.family!!.setName(name)
                    tvNombreFamilia.text = FamilyManager.family!!.getName()
                }
                dialog.dismiss()
            }
        }
    }

    private fun showPopUp(layout: Int) {
        dialog = Dialog(requireContext())
        dialog.setContentView(layout)
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun backToProfiles() {
        val intent = Intent(requireContext(), ProfilesActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    private fun backToLogin() {
        FamilyManager.reset()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    /* Funciones de base de datos */

    private fun updateUserNameInDatabase(name: String) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(member.getId())
            .update(
                mapOf(
                    "user" to name,
                )
            )
            .addOnSuccessListener {
                FamilyManager.family!!.deleteMember(member.getUser())
                member.setUser(name)
                FamilyManager.family!!.addMember(member)
                tvUserName.text = name
                user = name
                Toast.makeText(
                    requireContext(),
                    "Nombre actualizado correctamente",
                    Toast.LENGTH_SHORT
                )
                    .show()
                backToProfiles()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Se ha producido un error", Toast.LENGTH_SHORT)
                    .show()
                println("Error al actualizar datos del usuario: $e")
            }
    }

    private fun updateFamilyNameInDatabase(name: String) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(Constants.FAMILY)
            .update(
                mapOf(
                    "familyName" to name
                )
            )
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Datos de la familia actualizados correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                FamilyManager.family!!.setName(name)
                tvNombreFamilia.text = FamilyManager.family!!.getName()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Error al actualizar datos de la familia",
                    Toast.LENGTH_SHORT
                ).show()
                println("Error al actualizar datos de la familia: $e")
            }
    }

    private fun updatePasswordInDatabase() {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(member.getId())
            .update(
                mapOf(
                    "password" to member.getPassword()
                )
            )
    }

    private fun deleteMemberFromDatabase(selectedMember: String) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(Constants.FAMILY)
            .collection(Constants.MEMBERS)
            .document(member.getId())
            .delete()
            .addOnSuccessListener {
                FamilyManager.family!!.deleteMember(selectedMember)
                deletePictures(member.getId())
                if (FamilyManager.family!!.getMembers().isNotEmpty()) {
                    backToProfiles()
                }
                println("Miembro eliminado correctamente")
            }
            .addOnFailureListener {
                println("Error al eliminar al miembro")
            }
    }

    private fun deleteTasksFromDatabase(typeOfTask: String) {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(Constants.FAMILY)
            .collection(typeOfTask)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document.exists()) {
                        document.reference.delete()
                    }
                }
            }
            .addOnFailureListener {
                println("Error al eliminar todas las tareas")
            }
    }

    private fun deleteFamily() {
        FirebaseFirestore.getInstance()
            .collection(uid)
            .document(Constants.FAMILY)
            .delete()
            .addOnSuccessListener {
                FirebaseAuth.getInstance().currentUser?.delete()
                backToLogin()
            }
    }

    private fun deletePictures(userId: String){
        val uuid = FirebaseAuth.getInstance().currentUser!!.uid

        val storageRef = FirebaseStorage.getInstance().reference.child("images/$uuid/$userId")
        storageRef.delete()
            .addOnSuccessListener {
                Log.d("FirebaseStorage", "Archivo eliminado exitosamente.")
            }
            .addOnFailureListener{exc ->
                Log.e("FirebaseStorage", "Error al eliminar el archivo", exc)
            }
    }
}