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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.auth.firebase.FirebaseRemotDataSource
import com.iti.itp.bazaar.auth.firebase.FirebaseReposatory
import com.iti.itp.bazaar.auth.viewModel.AuthViewModel
import com.iti.itp.bazaar.auth.viewModel.AuthViewModelFactory
import com.iti.itp.bazaar.databinding.FragmentLoginBinding
import com.iti.itp.bazaar.dto.order.Address
import com.iti.itp.bazaar.dto.CustomerRequest


import com.iti.itp.bazaar.dto.PostedCustomer
import com.iti.itp.bazaar.dto.cutomerResponce.CustomerResponse
import com.iti.itp.bazaar.mainActivity.MainActivity
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {
    lateinit var binding: FragmentLoginBinding
    lateinit var vmFactory: AuthViewModelFactory
    lateinit var authViewModel: AuthViewModel
    lateinit var mAuth: FirebaseAuth
    lateinit var sharedPreferences: SharedPreferences
    val address = Address(
        last_name = "alaa",
        first_name = "eisa",
        address1 = "a;qma",
        city = "ismailia",
        province = "CA",
        phone = "+01008313390",
        zip = "12345",
        country = ""
    )

    val customer = PostedCustomer(
        first_name = "alaa",
        last_name = "eisa",
        email = "3laaesia@gmail.com",
        phone = "01005750730",
        verified_email = false,
        password = "aA12345#",
        password_confirmation = "aA12345#",
        addresses = listOf(address),
        send_email_welcome = true
    )
    val customerRequest = CustomerRequest(customer)
    override fun onStart() {
        super.onStart()
        checkIfEmailVerified()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences =
            requireActivity().getSharedPreferences(
                MyConstants.MY_SHARED_PREFERANCE,
                Context.MODE_PRIVATE
            )

        binding = FragmentLoginBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        vmFactory = AuthViewModelFactory(
            FirebaseReposatory.getInstance(FirebaseRemotDataSource(mAuth)),
            Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService))
        )
        authViewModel = ViewModelProvider(this, vmFactory).get(AuthViewModel::class.java)

        binding.tvGuestMode.setOnClickListener {

            // this is to be use in all project to check if the user is in guest mode
            sharedPreferences.edit().putString(MyConstants.IS_GUEST, "true").apply()

        }

        binding.btnLogIn.setOnClickListener {

            val email = binding.etEmailLogIn.text.toString()
            val password = binding.etPassLogIn.text.toString()
            if (!email.isNullOrBlank() || !password.isNullOrBlank()) {

                logIn(email, password)
            } else {
                Snackbar.make(requireView(), "Please Enter Your Full Credintial", 2000).show()

            }

        }
        binding.tvGoToSignUp.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            Navigation.findNavController(binding.root).navigate(action)

        }

    }

    fun logIn(email: String, password: String) {

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
                authViewModel.postCustomer(customerRequest)
                ObserveOnPostingCustomer()
                startActivity(Intent(requireActivity(), MainActivity::class.java))

                Snackbar.make(requireView(), "Authentication success.", 2000).show()
            } else {
                Snackbar.make(requireView(), "checkIfEmailVerified: Email is not verified", 2000)
                    .show()
            }
        } else {

        }
    }

    fun ObserveOnPostingCustomer() {
        lifecycleScope.launch {
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
                        val productsList = customerPostResponse.customers
                        Log.d("TAG", "postCustomer success:${productsList.get(0).id} ")
                    }
                }


            }

        }
    }


}