package com.example.dacs3_ticket_booking_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3_ticket_booking_app.data.model.User
import com.example.dacs3_ticket_booking_app.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _userDetail = MutableLiveData<User?>()
    val userDetail: LiveData<User?> = _userDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    // ✅ Lấy tất cả users
    fun getAllUsers() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.getAllUsers()
            result.onSuccess { list ->
                _users.value = list
                _isLoading.value = false
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi tải danh sách user: ${exception.message}"
                _isLoading.value = false
            }
        }
    }
    fun createUser(user:User, password: String){
        _isLoading.value=true
        viewModelScope.launch {
            val result=userRepository.createUserAuth(user.email, password)
            result.onSuccess { uid->
                val newUser= user.copy(id=uid)
                val createResult=userRepository.createUser(newUser)
                createResult.onSuccess {
                    _successMessage.value="Tạo user thành công"
                    _isLoading.value=false
                    getAllUsers()
                }
                createResult.onFailure { exception->
                    _errorMessage.value="Lỗi tạo user: ${exception.message}"
                    _isLoading.value=false
                }
            }
        }
    }
    // ✅ Xóa user
    fun deleteUser(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.deleteUser(userId)
            result.onSuccess {
                _successMessage.value = "Xóa user thành công"
                _isLoading.value = false
                getAllUsers()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi xóa user: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    // ✅ Cập nhật role user
    fun updateUserRole(userId: String, newRole: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.updateUserRole(userId, newRole)
            result.onSuccess {
                _successMessage.value = "Cập nhật role thành công"
                _isLoading.value = false
                getAllUsers()
            }
            result.onFailure { exception ->
                _errorMessage.value = "Lỗi cập nhật: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

     // ✅ Lấy users theo role
     fun getUsersByRole(role: String) {
         _isLoading.value = true
         viewModelScope.launch {
             val result = userRepository.getUsersByRole(role)
             result.onSuccess { list ->
                 _users.value = list
                 _isLoading.value = false
             }
             result.onFailure { exception ->
                 _errorMessage.value = "Lỗi lấy users theo role: ${exception.message}"
                 _isLoading.value = false
             }
         }
     }

     // ✅ Cập nhật thông tin user
     fun updateUser(user: User) {
         _isLoading.value = true
         viewModelScope.launch {
             val result = userRepository.updateUser(user)
             result.onSuccess {
                 _successMessage.value = "Update user success"
                 _isLoading.value = false
                 getAllUsers()
             }
             result.onFailure { exception ->
                 _errorMessage.value = "Error: ${exception.message}"
                 _isLoading.value = false
             }
         }
     }

     fun clearErrorMessage() {
         _errorMessage.value = null
     }

     fun clearSuccessMessage() {
         _successMessage.value = null
     }

     fun getUserById(userId: String) {
         _isLoading.value = true
         viewModelScope.launch {
             val result = userRepository.getUserById(userId)
             result.onSuccess { user ->
                 _userDetail.value = user
                 _isLoading.value = false
             }
             result.onFailure { exception ->
                 _errorMessage.value = "Error loading user: ${exception.message}"
                 _isLoading.value = false
             }
         }
     }
 }


