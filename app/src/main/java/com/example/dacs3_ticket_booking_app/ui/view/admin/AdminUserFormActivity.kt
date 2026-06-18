package com.example.dacs3_ticket_booking_app.ui.view.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.dacs3_ticket_booking_app.data.model.User
import com.example.dacs3_ticket_booking_app.databinding.ActivityAdminUserFormBinding
import com.example.dacs3_ticket_booking_app.ui.viewmodel.UserViewModel
import com.example.dacs3_ticket_booking_app.utils.CloudinaryHelper

class AdminUserFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminUserFormBinding
    private lateinit var userViewModel: UserViewModel
    private var currentUser: User? = null
    private var avatarUrl: String = ""
    private var selectedImageUri: Uri? = null
    private var isUploading: Boolean = false  // ✅ Flag để kiểm tra upload

    // Image picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            selectedImageUri = uri
            Glide.with(this).load(uri).into(binding.ivUserAvatar)
            uploadAvatarToCloudinary(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        currentUser = intent.getSerializableExtra("user") as? User

        if (currentUser != null) {
            populateForm(currentUser!!)
            binding.titleBar.text = "Chỉnh sửa User"
            binding.etEmail.isEnabled = false
            binding.layoutPassword.visibility = View.GONE
        } else {
            binding.titleBar.text = "Thêm User mới"
            binding.etEmail.isEnabled = true
            binding.layoutPassword.visibility = View.GONE  // Luôn ẩn password
        }

        setupListeners()
        observeViewModel()
    }

    private fun populateForm(user: User) {
        binding.etFullName.setText(user.fullName)
        binding.etEmail.setText(user.email)
        avatarUrl = user.avatar

        if (user.avatar.isNotEmpty()) {
            Glide.with(this)
                .load(user.avatar)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivUserAvatar)
        }

        if (user.role == "admin") {
            binding.rbAdmin.isChecked = true
        } else {
            binding.rbUser.isChecked = true
        }
    }

    private fun setupListeners() {
        binding.backBtn.setOnClickListener { finish() }

        // Avatar pick - chỉ click vào ImageView để chọn ảnh
        binding.ivUserAvatar.setOnClickListener { openImagePicker() }

        binding.btnSave.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val role = if (binding.rbAdmin.isChecked) "admin" else "user"

            if (fullName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Kiểm tra đang upload ảnh không
            if (isUploading) {
                Toast.makeText(this, "Vui lòng chờ tải ảnh hoàn tất", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUser != null) {
                // Update existing user
                val updatedUser = currentUser!!.copy(
                    fullName = fullName,
                    role = role,
                    avatar = avatarUrl,
                    updatedAt = System.currentTimeMillis()
                )
                userViewModel.updateUser(updatedUser)
            } else {
                // Create new user via ViewModel
                if (email.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                // Tự sinh mật khẩu: [chữ cái cuối cùng của họ][họ đầu tiên][movie@]
                val password = generatePassword(fullName)
                
                val newUser = User(
                    id = "",
                    email = email,
                    fullName = fullName,
                    role = role,
                    isActivate = true,
                    avatar = avatarUrl,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                userViewModel.createUser(newUser, password)
            }
        }

        // Xóa listener delete vì không cần nút này
     }

     private fun openImagePicker() {
         val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
         imagePickerLauncher.launch(intent)
     }
     
     // Tự sinh mật khẩu từ tên: Ví dụ "Đỗ Lam" -> "lamdmovie@"
     private fun generatePassword(fullName: String): String {
         val parts = fullName.trim().split(" ").filter { it.isNotEmpty() }
         if (parts.isEmpty()) return "movie@123"
         
         val lastName = parts.last()
         val firstPart = parts.dropLast(1).joinToString("")
         val firstInitial = firstPart.firstOrNull()?.lowercaseChar() ?: ""
         val lastNameLower = lastName.lowercase()
         
         return "${lastNameLower}${firstInitial}movie@"
     }

    private fun uploadAvatarToCloudinary(uri: Uri) {
        isUploading = true  // ✅ Đánh dấu đang upload
        binding.btnSave.isEnabled = false  // ✅ Disable nút Save
        binding.progressUploadAvatar.visibility = View.VISIBLE
        binding.tvAvatarStatus.text = "Đang tải ảnh lên..."

        CloudinaryHelper.uploadImage(uri, this) { url ->
            runOnUiThread {
                binding.progressUploadAvatar.visibility = View.GONE
                isUploading = false  // ✅ Upload xong
                binding.btnSave.isEnabled = true  // ✅ Enable nút Save
                
                if (url != null) {
                    avatarUrl = url
                    binding.tvAvatarStatus.text = "✓ Tải ảnh thành công"
                    binding.tvAvatarStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    Toast.makeText(this, "Ảnh đã tải lên thành công!", Toast.LENGTH_SHORT).show()
                } else {
                    binding.tvAvatarStatus.text = "✗ Tải ảnh thất bại"
                    binding.tvAvatarStatus.setTextColor(android.graphics.Color.parseColor("#FF5252"))
                    Toast.makeText(this, "Lỗi tải ảnh lên Cloudinary", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun observeViewModel() {
        userViewModel.successMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            finish()
        }

        userViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
