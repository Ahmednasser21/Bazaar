package com.iti.itp.bazaar.mainActivity.ui.profile

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.AuthActivity
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentMeBinding
import com.iti.itp.bazaar.dto.ListOfAddresses
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class ProfileFragment : Fragment() {
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var meViewModel: MeViewModel
    private lateinit var meFactory: MeViewModelFactory
    private lateinit var binding: FragmentMeBinding
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
        getAddressCount()

        binding.btnLogout.setOnClickListener {
           showLogoutDialog()
        }


        binding.cardViewOrders.setOnClickListener {
            val action = ProfileFragmentDirections.actionNavMeToOrderFragment(customerID)
            Navigation.findNavController(it).navigate(action)
        }

        binding.cardViewCurrency.setOnClickListener {
            setupCurrencySelectionDialog()
        }

        binding.cardViewContactUs.setOnClickListener {
            val action = ProfileFragmentDirections.actionNavProfileToContactUsFragment2()
            Navigation.findNavController(view).navigate(action)
        }

        binding.cardViewAddresses.setOnClickListener {
            val action = ProfileFragmentDirections.actionNavProfileToAddressFragment2()
            Navigation.findNavController(view).navigate(action)
        }

        binding.cardViewAboutUs.setOnClickListener {
            val action = ProfileFragmentDirections.actionNavProfileToAboutUsFragment2()
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
                    binding.currencyValue.text = "USD"
                }

                R.id.radioButtonEGP -> {

                    binding.currencyValue.text = "EGP"
                }
            }
            dialog.dismiss()
        }

        dialog.show()

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
                    DataState.Loading -> {binding.progressBar3.visibility = View.VISIBLE}
                    is DataState.OnFailed ->{
                        binding.progressBar3.visibility = View.GONE
                        Snackbar.make(requireView(), "failed to get customer data", 2000).show()}
                    is DataState.OnSuccess<*> -> {
                        binding.progressBar3.visibility = View.GONE
                        val data = it.data as SingleCustomerResponse
                        binding.customerName.text = auth.currentUser?.displayName
                        binding.customerEmail.text = data.customer.email
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun getAddressCount(){
        lifecycleScope.launch {
            meViewModel.getAddressCount(customerID.toLong())
            meViewModel.addresses.collect{
                when(it){
                    DataState.Loading -> {}
                    is DataState.OnFailed -> Snackbar.make(requireView(), "failed to get addresses count", 2000).show()
                    is DataState.OnSuccess<*> -> {
                        val response = it.data as ListOfAddresses
                        binding.addressCount.text = "${response.addresses.size} Addresses"
                    }
                }
            }
        }
    }
    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Logout") { dialog, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireActivity(), AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                dialog.dismiss()
            }
            .show()
    }


}
