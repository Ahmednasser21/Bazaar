package com.iti.itp.bazaar.mainActivity.ui.me

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentMeBinding
import com.iti.itp.bazaar.dto.SingleCustomerResponse
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.mainActivity.ui.order.OrderViewModel
import com.iti.itp.bazaar.mainActivity.ui.order.OrderViewModelFactory
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.responses.ExchangeRateResponse
import com.iti.itp.bazaar.network.responses.OrdersResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class MeFragment : Fragment() {
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var meViewModel: MeViewModel
    private lateinit var meFactory: MeViewModelFactory
    private lateinit var currencySharePrefs: SharedPreferences
    private lateinit var binding: FragmentMeBinding

    //private lateinit var moreOrders: TextView
    lateinit var mAuth: FirebaseAuth
    private lateinit var userDataSharedPreferences: SharedPreferences
    private lateinit var customerID: String
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userDataSharedPreferences = requireActivity().getSharedPreferences(
            MyConstants.MY_SHARED_PREFERANCE,
            Context.MODE_PRIVATE
        )
        customerID = userDataSharedPreferences.getString(MyConstants.CUSOMER_ID, "0").toString()

        val factory = OrderViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            )
        )
        orderViewModel = ViewModelProvider(requireActivity(), factory)[OrderViewModel::class.java]
        orderViewModel.getOrdersByCustomerID(customerID)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        meFactory = MeViewModelFactory(CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service)),
            Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)))
        meViewModel = ViewModelProvider(this,meFactory)[MeViewModel::class.java]
        binding = FragmentMeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getOrdersCount()

        binding.cardViewOrders.setOnClickListener {
            val action = MeFragmentDirections.actionNavMeToOrderFragment(customerID)
            Navigation.findNavController(it).navigate(action)
        }

        binding.cardViewCurrency.setOnClickListener {
            setupCurrencySelectionDialog()
        }

        binding.cardViewContactUs.setOnClickListener {
            val action = MeFragmentDirections.actionNavProfileToContactUsFragment2()
            Navigation.findNavController(view).navigate(action)
        }

        binding.cardViewAddresses.setOnClickListener {
            val action = MeFragmentDirections.actionNavProfileToAddressFragment2()
            Navigation.findNavController(view).navigate(action)
        }

        binding.cardViewAboutUs.setOnClickListener {
            val action = MeFragmentDirections.actionNavProfileToAboutUsFragment2()
            Navigation.findNavController(view).navigate(action)
        }

        getCustomerDataById()
    }


    private fun setupCurrencySelectionDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_currency_selection)

        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup)
        val buttonOk = dialog.findViewById<Button>(R.id.buttonOk)

        buttonOk.setOnClickListener {
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioButtonUSD -> {
//                        lifecycleScope.launch(Dispatchers.IO) {
//                            meViewModel.changeCurrency("EGP", "USD")
//                            withContext(Dispatchers.Main){
//                                observeCurrency()
//                            }
//                        }
                    binding.currencyValue.text = "USD"
                }

                R.id.radioButtonEGP -> {
                    // currencySharedPreferences.edit().putFloat(MyConstants.CURRENCY, 1F).apply()
                    binding.currencyValue.text = "EGP"
                }
            }
            dialog.dismiss()
        }

        dialog.show()

    }


    private suspend fun observeCurrency() {
        meViewModel.currency.collect { state ->
            when (state) {
                DataState.Loading -> showSnackbar("Loading")
                is DataState.OnFailed -> showSnackbar("Failed to change the currency")
                is DataState.OnSuccess<*> -> {
                    val data = state.data as? ExchangeRateResponse
                    //currencySharedPreferences.edit().putFloat("currency", data?.conversion_rate?.toFloat()?:1F).apply()
                }
            }

        }
    }


    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }
    private fun getOrdersCount() {
        lifecycleScope.launch {
            orderViewModel.ordersStateFlow.collectLatest { result ->
                when (result) {
                    is DataState.Loading -> {}

                    is DataState.OnSuccess<*> -> {
                        val ordersResponse = result.data as OrdersResponse
                        val ordersList = ordersResponse.orders
                        binding.orderCount.text = "Already have ${String.format(Locale.getDefault(),"%d", ordersList.size)} orders"


                    }

                    is DataState.OnFailed -> {}
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun getCustomerDataById(){
        lifecycleScope.launch {
            meViewModel.getCustomerById(customerID.toLong())
            meViewModel.customer.collect{
                when(it){
                    DataState.Loading -> Snackbar.make(requireView(), "loading customer data", 2000).show()
                    is DataState.OnFailed -> Snackbar.make(requireView(), "failed to get customer data", 2000).show()
                    is DataState.OnSuccess<*> -> {
                        val data = it.data as SingleCustomerResponse
                        binding.customerName.text = auth.currentUser?.displayName
                        binding.customerEmail.text = data.customer.email
                    }
                }
            }
        }
    }



}