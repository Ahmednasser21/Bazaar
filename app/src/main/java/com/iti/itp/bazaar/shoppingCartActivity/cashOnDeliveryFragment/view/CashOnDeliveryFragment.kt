package com.iti.itp.bazaar.shoppingCartActivity.cashOnDeliveryFragment.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.iti.itp.bazaar.databinding.FragmentCashOnDeliveryBinding
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.responses.ExchangeRateResponse
import com.iti.itp.bazaar.network.responses.PriceRulesResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.shoppingCartActivity.cashOnDeliveryFragment.viewModel.CashOnDeliveryViewModel
import com.iti.itp.bazaar.shoppingCartActivity.cashOnDeliveryFragment.viewModel.CashOnDeliveryViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs


class CashOnDeliveryFragment : Fragment() {
    private lateinit var binding: FragmentCashOnDeliveryBinding
    private lateinit var factory:CashOnDeliveryViewModelFactory
    private lateinit var cashOnDeliveryViewModel: CashOnDeliveryViewModel
    private lateinit var currencySharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        factory = CashOnDeliveryViewModelFactory(
            Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)),
            CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        cashOnDeliveryViewModel = ViewModelProvider(this,factory).get(CashOnDeliveryViewModel::class.java)
        currencySharedPreferences = requireContext().getSharedPreferences("currencySharedPrefs", Context.MODE_PRIVATE)
        binding = FragmentCashOnDeliveryBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        lifecycleScope.launch(Dispatchers.IO) {
            cashOnDeliveryViewModel.getCoupons()
            withContext(Dispatchers.Main) {
                cashOnDeliveryViewModel.coupons.collect { state ->
                    when (state) {
                        is DataState.Loading -> {}
                        is DataState.OnFailed -> {}
                        is DataState.OnSuccess<*> -> {
                            val data = state.data as PriceRulesResponse
                            binding.tvValidate.setOnClickListener {
                                val couponCode = binding.etCoupons.text.toString()
                                var discountAmount: Double
                                binding.tvValidate.isClickable = true

                                data.priceRules.forEach { priceRule ->
                                    if (priceRule.title == couponCode) {
                                        if (binding.etCoupons.text.isNotEmpty()){
                                            if (priceRule.valueType == "percentage") {
                                                // Calculate discount
                                                val subTotalPrice = binding.tvSubTotalPrice.text.toString().toDoubleOrNull() ?: 0.0
                                                discountAmount = subTotalPrice * abs(priceRule.value.toDouble()) / 100
                                                binding.tvDiscountValue.text = discountAmount.toString()

                                                val totalSubPriceAfterDiscount = (binding.tvSubTotalPrice.text.toString().toDoubleOrNull() ?: 0.0) - discountAmount
                                                val totalGrandPriceAfterDiscount = (totalSubPriceAfterDiscount + binding.tvShippingFees.text.toString().toDouble())

                                                binding.tvValidate.isClickable = false
                                                binding.etCoupons.isEnabled = false

                                                // Update the total price after discount
                                                binding.tvSubTotalPrice.text = totalSubPriceAfterDiscount.toString()
                                                binding.tvGrandTotal.text = totalGrandPriceAfterDiscount.toString()
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }


        val currency = currencySharedPreferences.getString("currency", "EGP")
        lifecycleScope.launch(Dispatchers.IO){
            if (currency == "USD"){
                cashOnDeliveryViewModel.exchangeCurrency("EGP","USD")
            }
            withContext(Dispatchers.Main){
                cashOnDeliveryViewModel.currency.collect{state->
                    when (state) {
                        is DataState.Loading -> {}
                        is DataState.OnFailed -> {}
                        is DataState.OnSuccess<*> ->{
                            val data = state.data as ExchangeRateResponse
                            changeAllTextViewsCurrency(data.conversion_rate)
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun changeAllTextViewsCurrency(conversionRate: Double) {
        binding.tvSubTotalPrice.text = String.format("%.1f", (binding.tvSubTotalPrice.text.toString().toDouble() * conversionRate))
        binding.tvShippingFees.text = String.format("%.1f", (binding.tvShippingFees.text.toString().toDouble() * conversionRate))
        binding.tvDiscountValue.text = String.format("%.1f", (binding.tvDiscountValue.text.toString().toDouble() * conversionRate))
        binding.tvGrandTotal.text = String.format("%.1f", (binding.tvGrandTotal.text.toString().toDouble() * conversionRate))
    }


}