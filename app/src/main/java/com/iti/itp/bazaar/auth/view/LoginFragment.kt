package com.iti.itp.bazaar.auth.view

import ReceivedOrdersResponse
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
import com.example.productinfoform_commerce.productInfo.viewModel.ProuductIfonViewModelFactory
import com.example.productinfoform_commerce.productInfo.viewModel.prouductInfoViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.auth.firebase.FirebaseRemotDataSource
import com.iti.itp.bazaar.auth.firebase.FirebaseReposatory
import com.iti.itp.bazaar.auth.viewModel.AuthViewModel
import com.iti.itp.bazaar.auth.viewModel.AuthViewModelFactory
import com.iti.itp.bazaar.databinding.FragmentLoginBinding
import com.iti.itp.bazaar.dto.Address
import com.iti.itp.bazaar.dto.AppliedDiscount
import com.iti.itp.bazaar.dto.Customer
import com.iti.itp.bazaar.dto.CustomerRequest
import com.iti.itp.bazaar.dto.CustomerUpdate
import com.iti.itp.bazaar.dto.DraftOrder
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.LineItem


import com.iti.itp.bazaar.dto.PostedCustomer
import com.iti.itp.bazaar.dto.UpdateCustomerRequest
import com.iti.itp.bazaar.dto.cutomerResponce.CustomerByEmailResponce

import com.iti.itp.bazaar.dto.cutomerResponce.CustomerResponse
import com.iti.itp.bazaar.mainActivity.MainActivity
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {
    lateinit var binding: FragmentLoginBinding
    lateinit var vmFactory: AuthViewModelFactory
    lateinit var authViewModel: AuthViewModel
    lateinit var mAuth: FirebaseAuth
    lateinit var sharedPreferences: SharedPreferences
    lateinit var ProductInfoViewModel : prouductInfoViewModel
    lateinit var DraftvmFActory : ProuductIfonViewModelFactory
var email:String?=null
var password:String?=null

    val updateCustomerRequest = UpdateCustomerRequest(
        customer = CustomerUpdate(
            id = 0L,
            first_name = "",
            last_name = ""
        )
    )
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
        // don't forget to check on the logged in user
       // checkIfEmailVerified()
        Log.d("TAG", "onStart: ")
        if (mAuth.currentUser != null)
        {
            if (mAuth.currentUser!!.isEmailVerified)
            {
                startActivity(Intent(requireActivity(), MainActivity::class.java))
            }

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
            requireActivity().getSharedPreferences(
                MyConstants.MY_SHARED_PREFERANCE,
                Context.MODE_PRIVATE
            )
        DraftvmFActory = ProuductIfonViewModelFactory( Repository.getInstance(
            ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
        ) , CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        ProductInfoViewModel = ViewModelProvider(this , DraftvmFActory).get(prouductInfoViewModel::class.java)


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
            startActivity(Intent(requireActivity(), MainActivity::class.java))

        }

        binding.btnLogIn.setOnClickListener {

             email = binding.etEmailLogIn.text.toString()
             password = binding.etPassLogIn.text.toString()
            if (!email.isNullOrBlank() || !password.isNullOrBlank()) {

                logIn(email!!, password!!)
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
// to ckeck if he is verfird on shopufi
                email?.let { ObserveOnGettingCustomerByEmail(it) }


            } else {
                Snackbar.make(requireView(), "checkIfEmailVerified: Email is not verified", 2000)
                    .show()
            }
        } else {

        }
    }
////////////////// wa2ef hena ya lol
    fun ObserveOnGettingCustomerByEmail(email : String) {
        lifecycleScope.launch {

            authViewModel.getCustomerByEmail(email)
            authViewModel.customerByEmailStateFlow .collectLatest { result ->
                when (result) {
                    is DataState.Loading -> {
                        Log.d("TAG", "ObserveOnGettingCustomerByEmail: Loading")
                    }

                    is DataState.OnFailed -> {
                        Log.d("TAG", "ObserveOnGettingCustomerByEmail faliour and error msg is ->: ${result.msg}")
                    }

                    is DataState.OnSuccess<*> -> {
                        val customerPostResponse = result.data as CustomerByEmailResponce
                        val customerByEmail = customerPostResponse.customers/*.id*/
                        if (customerByEmail.isNullOrEmpty())
                        {
                            Snackbar.make(requireView(), "Email wasn't found 1", 2000).show()
                        }
                        else {
                            Snackbar.make(requireView(), "Authentication success.", 2000).show()

                            sharedPreferences.edit().putString(MyConstants.IS_GUEST, "false").apply()
                            Log.d("TAG", "ObserveOnGettingCustomerByEmail success w da el object kamel ->:${customerByEmail.get(0).id} ")
                            //saving customer id i shared pref
                            sharedPreferences.edit().putString(MyConstants.CUSOMER_ID,customerByEmail.get(0).id.toString()).apply()
                            // now i want to create a method for creating to draft orders (Fav And Cart ) and then post thier id to this customer again
                            if (customerByEmail.get(0).first_name.isNullOrBlank()&&customerByEmail.get(0).last_name.isNullOrBlank())
                            {
                                updateCustomerRequest.customer.id=customerByEmail.get(0).id
                                CreatCartDraftOrder(customerByEmail.get(0).id)

                                delay(2000)
                                CreatFavDraftOrder(customerByEmail.get(0).id)

                                delay(3000)

                                // to update the customer details with his draft orders idS
                                updateCustomerById(customerByEmail.get(0).id,updateCustomerRequest)

                                startActivity(Intent(requireActivity(), MainActivity::class.java))
                            }else
                            {
                                sharedPreferences.edit().putString(MyConstants.CART_DRAFT_ORDER_ID,"${customerByEmail.get(0).first_name}").apply()
                                sharedPreferences.edit().putString(MyConstants.FAV_DRAFT_ORDERS_ID,"${customerByEmail.get(0).last_name}").apply()

                                startActivity(Intent(requireActivity(), MainActivity::class.java))

                            }



                        }
                         }
                }


            }

        }
    }

    fun CreatCartDraftOrder(customerId : Long){
        lifecycleScope.launch {
            ProductInfoViewModel.createOrder(creatDraftOrderRequest(customerId))
            delay(2000) // tb be able to retrive my draftOrderId
            ProductInfoViewModel.getAllDraftOrders()
            ProductInfoViewModel.allDraftOrders.collectLatest { result->
                when(result){
                    DataState.Loading -> {
                        Log.d("TAG", "CreatCartDraftOrder: loading ")
                    }
                    is DataState.OnFailed ->{
                        Log.d("TAG", "CreatCartDraftOrder: failure ")
                    }
                    is DataState.OnSuccess<*> ->{

                        val draftOrder = (result.data as ReceivedOrdersResponse)
                        val draftOrderId =draftOrder.draft_orders.get(draftOrder.draft_orders.size-1).id
                        sharedPreferences.edit().putString(MyConstants.CART_DRAFT_ORDER_ID,"$draftOrderId").apply()
                        Log.d("TAG", "CreatCartDraftOrder: success wel draftorder id is ->${draftOrderId} ")
                        updateCustomerRequest.customer.first_name=draftOrderId.toString()
                    }
                }

            }
        }

    }

    fun CreatFavDraftOrder(customerId : Long){
        lifecycleScope.launch {
            ProductInfoViewModel.createFavDraftOrder(creatDraftOrderRequest(customerId))
            delay(2000) // tb be able to retrive my draftOrderId
            // el moshkela momken tkon hena 3shan b observ b wa7da bs 3al etnen
            ProductInfoViewModel.getAllDraftOrdersForFav()
            ProductInfoViewModel.allDraftOrdersFav .collectLatest { result->
                when(result){
                    DataState.Loading -> {
                        Log.d("TAG", "CreatFavDraftOrder: loading ")
                    }
                    is DataState.OnFailed ->{
                        Log.d("TAG", "CreatFavDraftOrder:  success")
                    }
                    is DataState.OnSuccess<*> ->{
                        val draftOrder = (result.data as ReceivedOrdersResponse)
                        val draftOrderId =draftOrder.draft_orders.get(draftOrder.draft_orders.size-1).id
                        sharedPreferences.edit().putString(MyConstants.FAV_DRAFT_ORDERS_ID,"$draftOrderId").apply()
                        Log.d("TAG", "CreatFavDraftOrder:  success wel id hwa $draftOrderId")
                        updateCustomerRequest.customer.last_name=draftOrderId.toString()
                    }
                }

            }
        }

    }

    private fun creatDraftOrderRequest(customerId : Long): DraftOrderRequest {
        val draftOrderRequest = DraftOrderRequest(
            draft_order = DraftOrder(
                line_items = listOf(
                    LineItem(

                        product_id = 0L,
                        sku = "emptySKU",
                        title = "asdasda", price = "", quantity = 1)
                ),
                use_customer_default_address = true,
                applied_discount = AppliedDiscount(),
                customer = Customer(customerId)
            )

        )
        return draftOrderRequest
    }

    fun updateCustomerById (customerId : Long , updateCustomerRequest: UpdateCustomerRequest){
        lifecycleScope.launch {
            authViewModel.updateCustomerById(customerId,updateCustomerRequest)
            delay(2000)

            authViewModel.updateCustomerByIdStateFlow.collectLatest { result->
                when (result){
                    DataState.Loading -> {
                    }

                    is DataState.OnFailed -> {
                    }

                    is DataState.OnSuccess<*> -> {

                    }
                }


            }

        }

    }





}