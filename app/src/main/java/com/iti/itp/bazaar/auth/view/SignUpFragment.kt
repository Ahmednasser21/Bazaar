package com.iti.itp.bazaar.auth.view

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar

import com.google.firebase.auth.FirebaseAuth
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.firebase.FirebaseRemotDataSource
import com.iti.itp.bazaar.auth.firebase.FirebaseReposatory
import com.iti.itp.bazaar.auth.viewModel.AuthViewModel
import com.iti.itp.bazaar.auth.viewModel.AuthViewModelFactory
import com.iti.itp.bazaar.databinding.FragmentSignUpBinding
import java.util.regex.Pattern


class SignUpFragment : Fragment() {

    lateinit var binding : FragmentSignUpBinding
    lateinit var vmFactory : AuthViewModelFactory
    lateinit var authViewModel : AuthViewModel
    lateinit var mAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        vmFactory = AuthViewModelFactory(FirebaseReposatory.getInstance(FirebaseRemotDataSource(mAuth)))
        authViewModel = ViewModelProvider(this , vmFactory).get (AuthViewModel::class.java)



        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etEmail.error = null // Clear the error
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etPassword.error = null // Clear the error
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etReEnterPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etReEnterPassword.error = null // Clear the error
            }
            override fun afterTextChanged(s: Editable?) {}
        })


        binding.btnSingUp.setOnClickListener{
            val email = binding.etEmail .text.toString()
            val password = binding.etPassword.text.toString()
            val rePassword = binding.etReEnterPassword .text.toString()

            if (isEmailValid(email))
            {
                if (isPasswordValid(password))
                {
                    if (isPasswordMatching(password,rePassword))
                    {
                        signUp(email,password)
                    }
                    else {
                        binding.etReEnterPassword .error = "Passwords do not match"
                       // binding.etReEnterPassword.background.setTint(Color.RED)
                    }
                }
                else {
                    binding.etPassword .error = "password isn't valid  "
                    Snackbar.make(requireView(), "passwprd must be more than 8 letter and conatines simpols.", 2000)
                        .show()
                    //binding.etReEnterPassword.background.setTint(Color.RED)
                }
            }
            else {
                binding.etEmail .error = "Email isn't valid    "
              //  binding.etReEnterPassword.background.setTint(Color.RED)

            }


        }

        binding.tvBackToLogIn.setOnClickListener{

          val action =  SignUpFragmentDirections.actionSignUpFragmentToLoginFragment()
            Navigation.findNavController(binding.root).navigate(action)
        }


    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return Pattern.compile(emailPattern).matcher(email).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) return false

        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }

        val hasDigit = password.any { it.isDigit() }

        val specialCharacters = "!@#\$%^&*()-_=+{}[]|:;\"'<>,.?/~`"
        val hasSpecialChar = password.any { it in specialCharacters }

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    }

    fun isPasswordMatching(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun signUp (email :String , password:String ){
        authViewModel.signUp(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // FirebaseUser user = mAuth.getCurrentUser();
                    Snackbar.make(requireView(), "Authentication success .. Please Verfiy this Email", 2000)
                        .show()
                    authViewModel.sendVerificationEmail(mAuth.currentUser)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Snackbar.make(requireView(), "Verification email sent to " + email, 2000).show()
                            } else {

                                Snackbar.make(requireView(), "Failed to send verification email.", 2000).show()
                            }
                        }
                } else {
                    Snackbar.make(requireView(), "Authentication failed.", 2000)
                        .show()


                }

            }


    }


}