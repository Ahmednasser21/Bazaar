package com.iti.itp.bazaar.auth.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.auth.firebase.FirebaseRemotDataSource
import com.iti.itp.bazaar.auth.firebase.FirebaseReposatory
import com.iti.itp.bazaar.auth.viewModel.AuthViewModel
import com.iti.itp.bazaar.auth.viewModel.AuthViewModelFactory
import com.iti.itp.bazaar.databinding.FragmentLoginBinding
import com.iti.itp.bazaar.mainActivity.MainActivity


class LoginFragment : Fragment() {
lateinit var binding : FragmentLoginBinding
    lateinit var vmFactory : AuthViewModelFactory
    lateinit var authViewModel : AuthViewModel
    lateinit var mAuth : FirebaseAuth
    lateinit var sharedPreferences: SharedPreferences
    override fun onStart() {
        super.onStart()
        val user = authViewModel.checkIfEmailVerified()
        if(user!=null)
        {
            // here should be the navigation step to the home screen casue the user is aready logged in
        startActivity(Intent(requireActivity(),MainActivity::class.java))
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences =
            requireActivity().getSharedPreferences(MyConstants.MY_SHARED_PREFERANCE, Context.MODE_PRIVATE)

        binding = FragmentLoginBinding.inflate(inflater,container ,false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        vmFactory = AuthViewModelFactory(FirebaseReposatory.getInstance(FirebaseRemotDataSource(mAuth)))
        authViewModel = ViewModelProvider(this , vmFactory).get (AuthViewModel::class.java)

        binding.tvGuestMode.setOnClickListener{

            // this is to be use in all project to check if the user is in guest mode
            sharedPreferences.edit().putString(MyConstants.IS_GUEST, "true").apply()

        }

        binding.btnLogIn.setOnClickListener{

            val email =binding.etEmailLogIn.text .toString()
            val password =binding.etPassLogIn.text .toString()
            if (!email.isNullOrBlank() || !password .isNullOrBlank()  )
            {

                logIn(email , password)
            }
            else {
                Snackbar.make(requireView(), "Please Enter Your Full Credintial", 2000).show()

            }

        }
        binding.tvGoToSignUp.setOnClickListener{
            val action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            Navigation.findNavController(binding.root).navigate(action)

        }

    }

    fun logIn(email : String , password:String) {

        authViewModel.logIn(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    checkIfEmailVerified()
                } else {
                    Snackbar.make(requireView(), "Authentication failed.", 2000)
                        .show()

                }
            }
    }
    private fun checkIfEmailVerified() {
        val user = authViewModel.checkIfEmailVerified()
        if (user != null) {
            if (user.isEmailVerified) {
                    // here also navigate to home screen
                startActivity(Intent(requireActivity(),MainActivity::class.java))
                Snackbar.make(requireView(), "Authentication success.", 2000).show()
            } else {
                Snackbar.make(requireView(), "checkIfEmailVerified: Email is not verified", 2000).show()
                 }
        } else {

        }
    }


}