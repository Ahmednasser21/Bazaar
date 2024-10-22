package com.iti.itp.bazaar.auth.view

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar

import com.google.firebase.auth.FirebaseAuth
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.auth.firebase.FirebaseRemotDataSource
import com.iti.itp.bazaar.auth.firebase.FirebaseReposatory
import com.iti.itp.bazaar.auth.viewModel.AuthViewModel
import com.iti.itp.bazaar.auth.viewModel.AuthViewModelFactory
import com.iti.itp.bazaar.databinding.FragmentSignUpBinding
import com.iti.itp.bazaar.dto.Address
import com.iti.itp.bazaar.dto.Customer
import com.iti.itp.bazaar.dto.CustomerRequest
import com.iti.itp.bazaar.dto.PostedCustomer
import com.iti.itp.bazaar.dto.cutomerResponce.CustomerResponse
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.shopify.ProductService
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.regex.Pattern


class SignUpFragment : Fragment() {

    lateinit var binding : FragmentSignUpBinding
    lateinit var vmFactory : AuthViewModelFactory
    lateinit var authViewModel : AuthViewModel
    lateinit var mAuth : FirebaseAuth
    lateinit var sharedPreferences: SharedPreferences


    val newCustomer = PostedCustomer(
        first_name = "",
        last_name = "",
        email = "",
        phone = "01090313390",
        verified_email = true,
        addresses = listOf(
            Address(
                address1 = "",
                city = "",
                province = "",
                phone = "",
                zip = "",
                last_name = "",
                first_name = "",
                country = ""
            )
        ),
        password = "",
        password_confirmation = "",
        send_email_welcome = false
    )
    var customerRequest: CustomerRequest = CustomerRequest(newCustomer)

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

        sharedPreferences =
            requireContext().getSharedPreferences(MyConstants.MY_SHARED_PREFERANCE, Context.MODE_PRIVATE)
        mAuth = FirebaseAuth.getInstance()
        vmFactory = AuthViewModelFactory(FirebaseReposatory.getInstance(FirebaseRemotDataSource(mAuth)) ,
            Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)))
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
                    //navigateToLogin()

                    customerRequest.customer.email = email
                    customerRequest.customer.password=password
                    customerRequest.customer.password_confirmation=password
                    customerRequest.customer.phone= ""

                    ObserveOnPostingCustomer(customerRequest)

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
    fun navigateToLogin (){
        val action = SignUpFragmentDirections.actionSignUpFragmentToLoginFragment()
        Navigation.findNavController(binding.root).navigate(action)
    }

    fun ObserveOnPostingCustomer(customerRequest: CustomerRequest) {
        lifecycleScope.launch {
            authViewModel.postCustomer(customerRequest)
            authViewModel.customerStateFlow.collectLatest { result ->
                when (result) {
                    is DataState.Loading -> {
                        Log.d("TAG", "postCustomer: Loading")
                    }

                    is DataState.OnFailed -> {
                        Log.d("TAG", "postCustomer faliour and error msg is ->: ${result.msg}")
                    }

                    is DataState.OnSuccess<*> -> {
                        val customerPostResponse = result.data as CustomerResponse
                        val productsList = customerPostResponse.customer
                        Log.d("TAG", "postCustomer success w da el id bta3 el new customer->:${productsList.id} ")
                        sharedPreferences.edit().putString(MyConstants.CUSOMER_ID ,productsList.id.toString() )
                    }
                }


            }

        }
    }



}