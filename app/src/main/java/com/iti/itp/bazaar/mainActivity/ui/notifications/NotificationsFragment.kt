package com.iti.itp.bazaar.mainActivity.ui.notifications

import android.annotation.SuppressLint
import android.content.Context
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
import com.iti.itp.bazaar.databinding.FragmentNotificationsBinding
import com.iti.itp.bazaar.dto.SingleCustomerResponse
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsFragment : Fragment() {
    private lateinit var notificationsViewModel:NotificationsViewModel
    private lateinit var factory:NotificationViewModelFactory
    private lateinit var currencySharePrefs:SharedPreferences
    private lateinit var binding:FragmentNotificationsBinding
    private lateinit var moreOrders:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        factory = NotificationViewModelFactory(
            CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service)),
            repository = Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService))
        )
        notificationsViewModel = ViewModelProvider(this, factory)[NotificationsViewModel::class.java]
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        currencySharePrefs = requireActivity().applicationContext.getSharedPreferences("currencySharedPrefs", Context.MODE_PRIVATE)
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moreOrders = binding.moreOrders
        moreOrders.setOnClickListener{
            val action = NotificationsFragmentDirections.actionNavMeToOrderFragment("customer_id:8220771418416")
            Navigation.findNavController(it).navigate(action)
        }
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