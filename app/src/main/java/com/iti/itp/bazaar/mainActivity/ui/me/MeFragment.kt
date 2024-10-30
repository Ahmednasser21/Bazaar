package com.iti.itp.bazaar.mainActivity.ui.me

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.AuthActivity
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentMeBinding
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
    //private lateinit var moreOrders: TextView
    lateinit var mAuth: FirebaseAuth
    private lateinit var userDataSharedPreferences: SharedPreferences
    private lateinit var customerID: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.cardViewOrders.setOnClickListener{

        }


        binding.cardViewCurrency.setOnClickListener{
            setupCurrencySelectionDialog()
        }

        binding.cardViewContactUs.setOnClickListener{
            val action = MeFragmentDirections.actionNavProfileToContactUsFragment2()
            Navigation.findNavController(view).navigate(action)
        }

        binding.cardViewAddresses.setOnClickListener{
            val action = MeFragmentDirections.actionNavProfileToAddressFragment2()
            Navigation.findNavController(view).navigate(action)
        }

        binding.cardViewAboutUs.setOnClickListener{
            val action = MeFragmentDirections.actionNavProfileToAboutUsFragment2()
            Navigation.findNavController(view).navigate(action)
        }
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

}