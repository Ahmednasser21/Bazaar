package com.iti.itp.bazaar.productInfo.view

import ReceivedLineItem
import ReceivedOrdersResponse
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.productinfoform_commerce.productInfo.viewModel.ProuductIfonViewModelFactory
import com.example.productinfoform_commerce.productInfo.viewModel.prouductInfoViewModel
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.databinding.FragmentProuductnfoBinding
import com.iti.itp.bazaar.dto.AppliedDiscount
import com.iti.itp.bazaar.dto.Customer
import com.iti.itp.bazaar.dto.DraftOrder
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.LineItem
import com.iti.itp.bazaar.dto.UpdateDraftOrder
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
import com.iti.itp.bazaar.dto.UpdateLineItem
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.products.Option
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.responses.ExchangeRateResponse
import com.iti.itp.bazaar.network.responses.PriceRulesResponse
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.productInfo.OnClickListner
import com.iti.itp.bazaar.productInfo.OnColorClickListner
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


class ProuductnfoFragment : Fragment() , OnClickListner<AvailableSizes> , OnColorClickListner{

    lateinit var binding : FragmentProuductnfoBinding
    lateinit var ProductInfoViewModel : prouductInfoViewModel
    lateinit var vmFActory : ProuductIfonViewModelFactory
    lateinit var availableSizesAdapter : AvailableSizesAdapter
    lateinit var availableColorsAdapter : AvailableColorAdapter
    lateinit var sharedPreferences: SharedPreferences
    var conversionRate : Double? = 0.0
    var choosenSize : String? = null
    var choosenColor : String?= null

    lateinit var proudct :Products
    lateinit var Currentcurrency : String
    private val ratingList = listOf(2.5f,3.0f,3.5f, 4.0f, 4.5f, 5.0f)
    private val reviewList = listOf(
        "Excellent product, highly recommend!",
        "Good quality but could be improved.",
        "Not bad, but not the best I've seen.",
        "Amazing! Exceeded my expectations.",
        "Wouldn't recommend, not worth the price."
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentProuductnfoBinding.inflate(inflater,container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(
            "currencySharedPrefs",
            Context.MODE_PRIVATE
        )
        Currentcurrency = sharedPreferences.getString("currency","EGP") ?:"EGP"
        vmFActory = ProuductIfonViewModelFactory( Repository.getInstance(
            ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
        ) , CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        ProductInfoViewModel = ViewModelProvider(this , vmFActory).get(prouductInfoViewModel::class.java)
// here i should recive args from any string with id : Long

        getProductDetailsById()

// size adapter
        availableSizesAdapter = AvailableSizesAdapter(this)
        binding.rvAvailableSizes.apply {
            adapter = availableSizesAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        }
// color adapter
        availableColorsAdapter = AvailableColorAdapter(this)
        binding.rvAvailableColors.apply {
            adapter = availableColorsAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

// handel btn_addToCart
        // handel btn_addToCart
        binding.btnAddToCart.setOnClickListener {
            when {
                choosenSize.isNullOrBlank() -> {
                    Snackbar.make(requireView(), "You must choose a size to proceed with this action", Snackbar.LENGTH_SHORT).show()
                }
                choosenColor.isNullOrBlank() -> {
                    Snackbar.make(requireView(), "You must choose a color to proceed with this action", Snackbar.LENGTH_SHORT).show()
                }
                else -> {
                    // samy's work
                    // chosenSize, chosenColor, product (global variable taken its value when success in getProductDetails())
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        ProductInfoViewModel.getPriceRules()
                        ProductInfoViewModel.getAllDraftOrders()
                        ProductInfoViewModel.allDraftOrders.collect { state ->
                            when (state) {
                                is DataState.Loading -> {}
                                is DataState.OnFailed -> {}
                                is DataState.OnSuccess<*> -> {
                                    val data = state.data as ReceivedOrdersResponse
                                    if (data.draft_orders.isEmpty()) {
                                        ProductInfoViewModel.createOrder(draftOrderRequest())
                                    } else {
                                        // Use the first existing draft order
                                        val existingOrder = data.draft_orders.first()
                                        Log.i("TAG", "product id is: ${ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId}")
                                        val updatedLineItems = (existingOrder.line_items ?: emptyList()).toMutableList().apply {
                                            add(ReceivedLineItem(
                                                sku = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId.toString(),
                                                id = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId,
                                                variant_title = "dgldsjglk",
                                                product_id = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId,
                                                title = proudct.title,
                                                price = proudct.variants[0].price,
                                                quantity = 1
                                            ))
                                        }
                                        ProductInfoViewModel.updateDraftOrder(
                                            existingOrder.id,
                                            UpdateDraftOrderRequest(
                                                DraftOrder(
                                                    applied_discount = AppliedDiscount(null),
                                                    customer = Customer(8220771418416),
                                                    use_customer_default_address = true,
                                                    line_items = updatedLineItems.map {
                                                        LineItem(
                                                            sku = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId.toString(),
                                                            product_id = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId,
                                                            title = it.title!!, price = it.price, quantity = it.quantity ?: 1)
                                                    },
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getProductDetails( ){

        lifecycleScope.launch {
            ProductInfoViewModel.productDetailsStateFlow.collectLatest {result ->
                when (result){
                    is DataState.Loading -> {
                        Log.d("TAG", "getProductDetails: loading")
                    }
                    is DataState.OnFailed -> {
                        Log.d("TAG", "getProductDetails: failure and error msg is  ${result.msg}")
                    }
                    is DataState.OnSuccess<*> -> {
                        val productResponse = result.data as ProductResponse
                        val productsList = productResponse.products
                        Log.d("TAG", "getProductDetails: ${productsList}")
                        proudct = productsList.get(0)
                        setProudctDetailToUI(productsList)
                        setUpTheAvailableSizesAndColors(productsList.get(0).options)



                    }
                }

            }
        }


    }

    fun setProudctDetailToUI ( productsList : List<Products>){
        binding.tvProuductName.text = productsList.get(0).title
        binding.tvProuductDesc.text = productsList.get(0).bodyHtml

        // da el se3r w hyt8yar based on shared pref in setting
        when (Currentcurrency){
            "EGP"->{
                binding.tvProuductPrice.text = "${productsList.get(0).variants.get(0).price} EGP"
            }
            "USD"->{
                ProductInfoViewModel.getCurrencyRate("EGP","USD")
                val prics = productsList.get(0).variants.get(0).price.toDouble()
                getCurrencyRate( prics)

//               val newPrice = (prics * conversionRate!!)
//               binding.tvProuductPrice.text = "${newPrice} USD"
            }
        }


        val randomRating = ratingList[Random.nextInt(ratingList.size)]
        val randomReview = reviewList[Random.nextInt(reviewList.size)]

        // Set the rating and review to the UI
        binding.rbProuductRatingBar.rating = randomRating
        binding.rbProuductRatingBar.setIsIndicator(true) // to make the rating bar unchangable
        binding.tvProuductReview.text = randomReview
        Log.d("TAG", "getProductDetails: url sora  ${productsList.get(0).images.get(0).src} ")

        // Set the image src to slider
        val imageSlideModels = productsList.get(0).images.map { SlideModel(it.src,"",
            ScaleTypes.FIT) }
        binding.isProuductImage .setImageList(imageSlideModels)
    }

    fun setUpTheAvailableSizesAndColors (optionList : List<Option>)
    {
        optionList .forEach{
            when (it .name ) {
                "Color"->{
                    val availableColorsList=  it.values.map{AvailableColor(it)}
                    availableColorsAdapter.submitList(availableColorsList)
                }
                "Size"->{
                    val availableSizesList=  it.values.map{AvailableSizes(it)}
                    availableSizesAdapter.submitList(availableSizesList)


                }
                else ->{}
            }
        }
    }

    override fun OnClick(t: AvailableSizes) {

        if (choosenSize .isNullOrBlank())
        {
            choosenSize = t.size
            Log.d("TAG", "OnColorClick: choosen size  is  ${choosenSize} ")
            Snackbar.make(requireView(), "you have choosen a size ${t.size} ", 2000).show()
        }
        else {

            choosenSize = t.size
            Log.d("TAG", "OnColorClick: choosen size  is  ${choosenSize} ")
            Snackbar.make(requireView(), "Your choosen Size has been Changed To Be ${t.size} ", 2000).show()
        }

    }

    override fun OnColorClick(t: AvailableColor) {
        if (choosenColor.isNullOrBlank())
        {
            choosenColor = t.color
            Log.d("TAG", "OnColorClick: choosen colr is  ${choosenColor} ")
            Snackbar.make(requireView(), "you have choosen a color ${t.color} ", 2000).show()
        }
        else
        {
            choosenColor  = t.color
            Log.d("TAG", "OnColorClick: choosen colr is  ${choosenColor} ")
            Snackbar.make(requireView(), "your choosen color has been changed to be ${t.color} ", 2000).show()

        }

    }
    fun getProductDetailsById()
    {
        val productId = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId

        if (productId != null || productId != 0L)
        {
            ProductInfoViewModel.getProductDetails(productId)
            getProductDetails()
        }
        else
        {
            Snackbar.make(requireView(), "there was a problem fetshng this prouduct details at the moment   ", 2000).show()
        }
    }

    fun getCurrencyRate (price : Double) {

        lifecycleScope.launch {
            ProductInfoViewModel.currencyStateFlow.collectLatest { result ->
                when (result){
                    DataState.Loading -> {   Log.d("TAG", "getCurrencyRate: loading   ")}
                    is DataState.OnFailed ->{ Log.d("TAG", "getCurrencyRate: failure    ")}
                    is DataState.OnSuccess<*> -> {

                        conversionRate =  (result.data  as ExchangeRateResponse).conversion_rate
                        Log.d("TAG", "getCurrencyRate: succes   $conversionRate   ")
                        binding.tvProuductPrice.text = String.format("%.2f", (price * conversionRate!!))+" USD"


                    }
                }

            }
        }
    }



    private fun draftOrderRequest():DraftOrderRequest{
        val draftOrderRequest = DraftOrderRequest(
            draft_order = DraftOrder(
                line_items = listOf(
                    LineItem(
                        sku = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId.toString(),
                        name = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId.toString(),
                        id = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId,
                        product_id =  ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId,
                        title = proudct.title, price = proudct.variants[0].price, quantity = 1)
                ),
                use_customer_default_address = true,
                applied_discount = AppliedDiscount(),
                customer = Customer(8220771418416)
            )

        )
        return draftOrderRequest
    }


//    suspend fun priceRulesResult(){
//        ProductInfoViewModel.priceRules.collect{state->
//            when(state){
//                is DataState.Loading ->{}
//                is DataState.OnFailed ->{}
//                is DataState.OnSuccess<*> ->{
//                    val data = state.data as PriceRulesResponse
//                }
//            }
//        }
//    }

}