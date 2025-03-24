package com.example.firebase_demo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var  userRef : DatabaseReference

    private lateinit var edtEmail : EditText
    private lateinit var edtPassword : EditText
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: Button
    private lateinit var btnShowData: Button
    private lateinit var tvData: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        edtEmail = findViewById(R.id.edtEmailAddress)
        edtPassword = findViewById(R.id.edtPassword)
        btnRegister = findViewById(R.id.btnSignup)
        btnLogin = findViewById(R.id.btnLogin)
        btnShowData = findViewById(R.id.btnShow)
        tvData = findViewById(R.id.tvData)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users")


        btnRegister.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()){
                registerUser(email, password)
            } else {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()){
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show()
            }
        }

        btnShowData.setOnClickListener {
            showUserData()
        }

    }

    // Hàm đăng ký người dùng
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                    // Sau khi đăng ký, lưu thông tin người dùng vào Realtime Database
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        // Tạo đối tượng người dùng (chỉ lưu email ở ví dụ này, có thể mở rộng thêm thông tin)
                        val userMap = mapOf<String, String>(
                            "email" to email
                        )
                        userRef.child(it).setValue(userMap)
                            .addOnSuccessListener {
                                Log.d("DB", "Thông tin người dùng đã được lưu vào Database")
                            }
                            .addOnFailureListener { e ->
                                Log.e("DB", "Lỗi khi lưu dữ liệu: ${e.message}")
                            }
                    }
                } else {
                    Toast.makeText(this, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Hàm đăng nhập người dùng
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                    // Sau khi đăng nhập thành công, cập nhật hoặc lưu thông tin người dùng (tùy nhu cầu)
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        val userMap = mapOf<String, String>(
                            "email" to email
                        )
                        userRef.child(it).setValue(userMap)
                    }
                } else {
                    Toast.makeText(this, "Đăng nhập thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



    // Hàm đọc và hiển thị dữ liệu từ Firebase Realtime Database
    private fun showUserData() {
        // Ở đây ví dụ: đọc dữ liệu của người dùng đang đăng nhập
        val userId = auth.currentUser?.uid
        if (userId != null) {
            userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        // Lấy dữ liệu (ví dụ chỉ email)
                        val email = snapshot.child("email").value.toString()
                        tvData.text = "Email: $email"
                    } else {
                        tvData.text = "Không tìm thấy dữ liệu của người dùng."
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show()
        }
    }





}