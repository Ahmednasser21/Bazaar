package com.iti.itp.bazaar.mainActivity.ui.me

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.iti.itp.bazaar.auth.AuthActivity
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentMeBinding
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.mainActivity.ui.order.OrderViewModel
import com.iti.itp.bazaar.mainActivity.ui.order.OrderViewModelFactory
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.responses.OrdersResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.fixedRateTimer

class MeFragment : Fragment() {
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var meViewModel: MeViewModel
    private lateinit var meFactory: MeViewModelFactory
    private lateinit var currencySharePrefs: SharedPreferences
    private lateinit var binding: FragmentMeBinding
    private lateinit var moreOrders: TextView
    lateinit var mAuth: FirebaseAuth
    private lateinit var userDataSharedPreferences: SharedPreferences
    private lateinit var customerID: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        meFactory = MeViewModelFactory(
            CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service)),
            repository = Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService))
        )
        meViewModel = ViewModelProvider(this, meFactory)[MeViewModel::class.java]

        val orderFactory = OrderViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            )
        )
        orderViewModel =
            ViewModelProvider(requireActivity(), orderFactory)[OrderViewModel::class.java]
        userDataSharedPreferences = requireActivity().getSharedPreferences(
            MyConstants.MY_SHARED_PREFERANCE,
            Context.MODE_PRIVATE
        )
        customerID = userDataSharedPreferences.getString(MyConstants.CUSOMER_ID, "0").toString()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        currencySharePrefs = requireActivity().applicationContext.getSharedPreferences(
            "currencySharedPrefs",
            Context.MODE_PRIVATE
        )
        binding = FragmentMeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderViewModel.getOrdersByCustomerID(customerID)
        getOrderItem()
        moreOrders = binding.moreOrders
        moreOrders.setOnClickListener {
            val action = MeFragmentDirections.actionNavMeToOrderFragment(customerID)
            Navigation.findNavController(it).navigate(action)
        }

        mAuth = FirebaseAuth.getInstance()
        mAuth.addAuthStateListener { firebaseAuth ->
            if (mAuth.currentUser == null) {
                context?.let {
                    val intent = Intent(it, AuthActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                } ?: Log.e("AccountFragment", "Context is null")
            }
        }
        binding.btnLogout.setOnClickListener {

            AlertDialog.Builder(context)
                .setTitle("Confirm Sign out")
                .setMessage("Are you sure that you want to Sign Out?")
                .setPositiveButton("Yes") { dialog, _ ->
                    mAuth.signOut()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()


        }
    }

    private fun getOrderItem() {
        lifecycleScope.launch {
            orderViewModel.ordersStateFlow.collectLatest { result ->
                when (result) {
                    is DataState.Loading -> {}

                    is DataState.OnSuccess<*> -> {
                        val ordersResponse = result.data as OrdersResponse
                        if (ordersResponse.orders.isNotEmpty()){
                            val firstOrder = ordersResponse.orders[0]
                            withContext(Dispatchers.Main) {
                                binding.priceValue.text = "${firstOrder.totalPrice} EGP"
                                binding.createdAt.text = formatOrderDate(firstOrder.createdAt)
                            }

                        }

                    }

                    is DataState.OnFailed -> {
                        Snackbar.make(requireView(), "Failed to get data", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun formatOrderDate(apiDate: String): String {
        val apiDateFormat =
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.getDefault())
        val readableDateFormat = java.text.SimpleDateFormat(
            "MMMM dd, yyyy 'at' HH:mm a",
            java.util.Locale.getDefault()
        )
        val date = apiDateFormat.parse(apiDate.replace("Z", "+0000"))
        return readableDateFormat.format(date ?: "Unknown date")

    }


    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onStart() {
        super.onStart()
//        val currency = currencySharePrefs.getFloat("currency", 1F)
//        lifecycleScope.launch(Dispatchers.IO){
//            notificationsViewModel.getCustomerById(8220771418416)
//            notificationsViewModel.customer.collect{state ->
//                withContext(Dispatchers.Main){
//                    when(state){
//                        DataState.Loading -> {}
//                        is DataState.OnFailed -> {}
//                        is DataState.OnSuccess<*>->{
//                            val data = state.data as SingleCustomerResponse
//                            binding.nameOfUser.text = "${data.customer.firstName} ${data.customer.lastName}"
//                            binding.createdAt.text = data.customer.createdAt
//                            binding.priceValue.text = String.format("%.2f", data.customer.totalSpent * currency)                        }
//                    }
//                }
//            }
//        }
    }

}