package com.iti.itp.bazaar.settings.ui.settingsFragment.view

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
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentSettingsBinding
import com.iti.itp.bazaar.dto.ListOfAddresses
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.responses.ExchangeRateResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.settings.ui.settingsFragment.viewModel.SettingsViewModel
import com.iti.itp.bazaar.settings.ui.settingsFragment.viewModel.SettingsViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var factory: SettingsViewModelFactory
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var currencySharedPreferences: SharedPreferences
    private lateinit var draftOrderSharedPreferences: SharedPreferences
    private var customerId:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        factory = SettingsViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            ),
            CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        draftOrderSharedPreferences = requireActivity().getSharedPreferences(MyConstants.MY_SHARED_PREFERANCE, Context.MODE_PRIVATE)
        settingsViewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]
        currencySharedPreferences = requireActivity().applicationContext.getSharedPreferences(MyConstants.CURRENCY_SHARED_PREFS, Context.MODE_PRIVATE)
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customerId = draftOrderSharedPreferences.getString(MyConstants.CUSOMER_ID, "0")
        setupNavigationListeners()
    }

    override fun onStart() {
        super.onStart()
        updateCurrencyDisplay()
        fetchAndDisplayDefaultAddress()
        setupCurrencySelectionDialog()
    }

    private fun setupNavigationListeners() {
        binding.apply {
            cvAddress.setOnClickListener { navigateTo(SettingsFragmentDirections.actionSettingsFragmentToAddressFragment()) }
            cvContactUs.setOnClickListener { navigateTo(SettingsFragmentDirections.actionSettingsFragmentToContactUsFragment()) }
            cvAboutUs.setOnClickListener { navigateTo(SettingsFragmentDirections.actionSettingsFragmentToAboutUsFragment()) }
        }
    }

    private fun navigateTo(action: androidx.navigation.NavDirections) {
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun updateCurrencyDisplay() {
        val currency = currencySharedPreferences.getFloat(MyConstants.CURRENCY, 1F)
        binding.currency.text = if (currency == 1.0f) "EGP" else "USD"
    }

    private fun fetchAndDisplayDefaultAddress() {
        lifecycleScope.launch(Dispatchers.IO) {
            settingsViewModel.getAddressesForCustomer(customerId?.toLong()?:0)
            withContext(Dispatchers.Main) {
                settingsViewModel.addresses.collect { state ->
                    when (state) {
                        is DataState.Loading -> showSnackbar("Loading")
                        is DataState.OnFailed -> showSnackbar("Failed to fetch default address")
                        is DataState.OnSuccess<*> -> {
                            val data = state.data as? ListOfAddresses
                            val defaultAddress = data?.addresses?.find { it.default == true }
                            binding.address.text = defaultAddress?.country ?: "No default countries"
                        }
                    }
                }
            }
        }
    }

    private fun setupCurrencySelectionDialog() {
        binding.cvCurrency.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_currency_selection)

            val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup)
            val buttonOk = dialog.findViewById<Button>(R.id.buttonOk)

            buttonOk.setOnClickListener {
                when (radioGroup.checkedRadioButtonId) {
                    R.id.radioButtonUSD -> {
                       lifecycleScope.launch(Dispatchers.IO) {
                           settingsViewModel.changeCurrency("EGP", "USD")
                           withContext(Dispatchers.Main){
                               observeCurrency()
                           }
                       }
                        binding.currency.text = "USD"
                    }
                    R.id.radioButtonEGP -> {
                        currencySharedPreferences.edit().putFloat(MyConstants.CURRENCY, 1F).apply()
                        binding.currency.text = "EGP"
                    }
                }
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private suspend fun observeCurrency() {
            settingsViewModel.currency.collect { state ->
                when (state) {
                    DataState.Loading -> showSnackbar("Loading")
                    is DataState.OnFailed -> showSnackbar("Failed to change the currency")
                    is DataState.OnSuccess<*> -> {
                        val data = state.data as? ExchangeRateResponse
                        currencySharedPreferences.edit().putFloat("currency", data?.conversion_rate?.toFloat()?:1F).apply()
                    }
                }

        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }
}