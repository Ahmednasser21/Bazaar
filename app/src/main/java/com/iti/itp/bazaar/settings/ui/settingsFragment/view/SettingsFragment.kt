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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.R
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
    private lateinit var currencySharedPreferences:SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        factory = SettingsViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            ),
            CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        settingsViewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)
        currencySharedPreferences = requireContext().getSharedPreferences("currencySharedPrefs", Context.MODE_PRIVATE)
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cvAddress.setOnClickListener {
            val action = SettingsFragmentDirections.actionSettingsFragmentToAddressFragment()
            Navigation.findNavController(view).navigate(action)
        }

        binding.cvContactUs.setOnClickListener{
            val action = SettingsFragmentDirections.actionSettingsFragmentToContactUsFragment()
            Navigation.findNavController(view).navigate(action)
        }

        binding.cvAboutUs.setOnClickListener{
            val action = SettingsFragmentDirections.actionSettingsFragmentToAboutUsFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

    override fun onStart() {
        super.onStart()

        val currency = currencySharedPreferences.getString("currency", "EGP")
        if (currency == "EGP"){
            binding.currency.text = "EGP"
        }else{
            binding.currency.text = "USD"
        }

        lifecycleScope.launch(Dispatchers.IO) {
            settingsViewModel.getAddressesForCustomer(8220771385648)
            withContext(Dispatchers.Main) {
                settingsViewModel.addresses.collect { state ->
                    when (state) {
                        is DataState.Loading -> Snackbar.make(requireView(), "Loading", 2000).show()
                        is DataState.OnFailed -> Snackbar.make(
                            requireView(),
                            "Failed to fetch default address",
                            2000
                        ).show()

                        is DataState.OnSuccess<*> -> {
                            val data = state.data as ListOfAddresses
                            val defaultAddress = data.addresses.find {
                                it.default == true
                            }
                            binding.address.text = defaultAddress?.country ?: "No default countries"
                        }
                    }
                }
            }
        }


        binding.cvCurrency.setOnClickListener {
            // Create a Dialog
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_currency_selection)

            // Find the radio buttons and button in the dialog
            val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup)
            val buttonOk = dialog.findViewById<Button>(R.id.buttonOk)

            // Set the OK button's click listener
            buttonOk.setOnClickListener {
                // Get the selected radio button
                val selectedId = radioGroup.checkedRadioButtonId
                when (selectedId) {
                    R.id.radioButtonUSD -> {
                        currencySharedPreferences.edit().putString("currency", "USD").apply()
                        binding.currency.text = "USD" // Update the displayed text to USD
                    }
                    R.id.radioButtonEUR -> {
                        currencySharedPreferences.edit().putString("currency", "EGP").apply()
                        binding.currency.text = "EGP" // Update the displayed text to EGP
                    }
                }

                // Dismiss the dialog
                dialog.dismiss()
            }

            // Show the dialog
            dialog.show()
        }


    }
}

