package com.iti.itp.bazaar.productInfo.view

import ReceivedLineItem
import ReceivedOrdersResponse
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.productinfoform_commerce.productInfo.viewModel.ProuductIfonViewModelFactory
import com.example.productinfoform_commerce.productInfo.viewModel.prouductInfoViewModel
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentProuductnfoBinding
import com.iti.itp.bazaar.dto.AppliedDiscount
import com.iti.itp.bazaar.dto.Customer
import com.iti.itp.bazaar.dto.DraftOrder
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.LineItem
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.products.Option
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.responses.ExchangeRateResponse
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.productInfo.OnClickListner
import com.iti.itp.bazaar.productInfo.OnColorClickListner
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log
import kotlin.random.Random


class ProuductnfoFragment : Fragment(), OnClickListner<AvailableSizes>, OnColorClickListner {

    var binding: FragmentProuductnfoBinding? = null
    lateinit var ProductInfoViewModel: prouductInfoViewModel
    lateinit var vmFActory: ProuductIfonViewModelFactory
    lateinit var availableSizesAdapter: AvailableSizesAdapter
    lateinit var availableColorsAdapter: AvailableColorAdapter
    lateinit var CurrencySharedPreferences: SharedPreferences
    lateinit var mySharedPrefrence: SharedPreferences
    lateinit var draftOrderRequest: DraftOrderRequest
    lateinit var IsGuestMode: String
    var conversionRate: Double? = 0.0
    var choosenSize: String? = null
    var choosenColor: String? = null
    lateinit var FavDraftOrderId: String
    var productTitle: String = ""
    lateinit var proudct: Products
    var IS_Liked = false
    var Currentcurrency: Float? = 1F
    private val ratingList = listOf(2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f)
    private val reviewList = listOf(
        "Excellent product, highly recommend!",
        "Good quality but could be improved.",
        "Not bad, but not the best I've seen.",
        "Amazing! Exceeded my expectations.",
        "Wouldn't recommend, not worth the price."
    )
    private var cartDraftOrderId:String?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        CurrencySharedPreferences = requireActivity().getSharedPreferences(
            "currencySharedPrefs",
            Context.MODE_PRIVATE
        )
        mySharedPrefrence = requireActivity().getSharedPreferences(
            MyConstants.MY_SHARED_PREFERANCE,
            Context.MODE_PRIVATE
        )
        Currentcurrency = CurrencySharedPreferences.getFloat("currency", 1F) ?: 1F
        Log.d("TAG", "onViewCreated: ek currency rate is :$Currentcurrency ")
        IsGuestMode = mySharedPrefrence.getString(MyConstants.IS_GUEST, "false") ?: "false"

        binding = FragmentProuductnfoBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FavDraftOrderId = mySharedPrefrence.getString(MyConstants.FAV_DRAFT_ORDERS_ID, "") ?: ""
        cartDraftOrderId = mySharedPrefrence.getString(MyConstants.CART_DRAFT_ORDER_ID, "0")
        Log.d("TAG", "onViewCreated fav draft order id : ${FavDraftOrderId} ")
        vmFActory = ProuductIfonViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            ), CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        ProductInfoViewModel =
            ViewModelProvider(this, vmFActory).get(prouductInfoViewModel::class.java)
// here i should recive args from any string with id : Long

        getProductDetailsById()

// size adapter
        availableSizesAdapter = AvailableSizesAdapter(this)
        binding!!.rvAvailableSizes.apply {
            adapter = availableSizesAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        }
// color adapter
        availableColorsAdapter = AvailableColorAdapter(this)
        binding!!.rvAvailableColors.apply {
            adapter = availableColorsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

// handel btn_addToCart
        // handel btn_addToCart
        binding!!.btnAddToCart.setOnClickListener {
            when (IsGuestMode){
                "true"->{
                    Snackbar.make(
                        requireView(),
                        "you cant add to cart in guest mode",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                else->{

                    when {
                        choosenSize.isNullOrBlank() -> {
                            Snackbar.make(
                                requireView(),
                                "You must choose a size to proceed with this action",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }

                        choosenColor.isNullOrBlank() -> {
                            Snackbar.make(
                                requireView(),
                                "You must choose a color to proceed with this action",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }

                        else -> {
                            // samy's work
                            // chosenSize, chosenColor, product (global variable taken its value when success in getProductDetails())
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                ProductInfoViewModel.getPriceRules()
                                ProductInfoViewModel.getSpecificDraftOrder(cartDraftOrderId?.toLong()!!)
                                ProductInfoViewModel.specificDraftOrders.collect { state ->
                                    when (state) {
                                        is DataState.Loading -> {}
                                        is DataState.OnFailed -> {}
                                        is DataState.OnSuccess<*> -> {
                                            val data = state.data as DraftOrderRequest
                                            // Use the first existing draft order
                                            val existingOrder = data.draft_order
                                            Log.i(
                                                "TAG",
                                                "product id is: ${
                                                    ProuductnfoFragmentArgs.fromBundle(
                                                        requireArguments()
                                                    ).productId
                                                }"
                                            )
                                            val updatedLineItems = (existingOrder.line_items
                                                ?: emptyList()).toMutableList()
                                            updatedLineItems.add(
                                                LineItem(
                                                    sku = ProuductnfoFragmentArgs.fromBundle(
                                                        requireArguments()
                                                    ).productId.toString(),
                                                    id = ProuductnfoFragmentArgs.fromBundle(
                                                        requireArguments()
                                                    ).productId,
                                                    variant_title = "dgldsjglk",
                                                    product_id = ProuductnfoFragmentArgs.fromBundle(
                                                        requireArguments()
                                                    ).productId,
                                                    title = proudct.title,
                                                    price = proudct.variants[0].price,
                                                    quantity = 1
                                                )
                                            )
                                            ProductInfoViewModel.updateDraftOrder(
                                                cartDraftOrderId?.toLong()?:0,
                                                UpdateDraftOrderRequest(
                                                    DraftOrder(
                                                        applied_discount = AppliedDiscount(null),
                                                        customer = Customer(8220771418416),
                                                        use_customer_default_address = true,
                                                        line_items = updatedLineItems.map {
                                                            LineItem(
                                                                sku = it.sku
                                                                    ?: ProuductnfoFragmentArgs.fromBundle(
                                                                        requireArguments()
                                                                    ).productId.toString(),  // Use existing SKU or new one
                                                                product_id = it.product_id
                                                                    ?: ProuductnfoFragmentArgs.fromBundle(
                                                                        requireArguments()
                                                                    ).productId,
                                                                title = it.title!!,
                                                                price = it.price,
                                                                quantity = it.quantity ?: 1
                                                            )
                                                        }
                                                    )
                                                )
                                            )

                                        }
                                    }

                                    withContext(Dispatchers.Main) {
                                        Snackbar.make(
                                            requireView(),
                                            "Product is added to your cart",
                                            2000
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                }

            }

        }

        // getting the specificFavDraftOrder

        getSpecificDraftOrderById(FavDraftOrderId.toLong())
        //handel btn add to favourite
        binding!!.ivAddProuductToFavorite.setOnClickListener {
            when (IsGuestMode){
                "true"->{
                    Snackbar.make(
                        requireView(),
                        "you cant add favorite in guest mode",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                else  ->{
                    Log.d("TAG", "onViewCreated:prouductInfo el fav click sh8al  ")
                    if (IS_Liked) { // kda el button pressed >> ezan ha3ml delete
                        Log.d("TAG", "onViewCreated: prouductInfo case en el product da is liked (case if ) w hamsa7 ${IS_Liked}  ")

                        AlertDialog.Builder(context)
                            .setTitle("Confirm item Delete")
                            .setMessage("Are you sure that you want to delete this item from your favourites?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                lifecycleScope.launch {
                                    binding!!.ivAddProuductToFavorite.setColorFilter(Color.BLACK)
                                    var currentDraftOrderItems: MutableList<LineItem> =
                                        mutableListOf() // to store my previous liked products

                                    draftOrderRequest.draft_order.line_items.forEach {
                                        Log.d("TAG", "onViewCreated: de el list abl el ta3del  ${it.title} ")
                                    }
                                    draftOrderRequest.draft_order.line_items.forEach {
                                        if (it.title != productTitle)

                                            currentDraftOrderItems.add(it) // getting the old list of line_items
                                    }

                                    currentDraftOrderItems.forEach{
                                        Log.d("TAG", "onViewCreated: de el list ba3d el ta3del  ${it.title} ")

                                    }

                                    val draft = draftOrderRequest.draft_order
                                    draft.line_items = currentDraftOrderItems
                                    ProductInfoViewModel.DeleteLineItemFromDraftOrder(FavDraftOrderId.toLong(), UpdateDraftOrderRequest(draft))

//                     // now i want to add this list to my new liked item
//                     currentDraftOrderItems.add(draftOrderRequest(proudct).draft_order.line_items.get(0))
//                     var updatedDraftOrder = draftOrderRequest(proudct).draft_order
//                     updatedDraftOrder.line_items = currentDraftOrderItems
//                     ProductInfoViewModel.updateDraftOrder(
//                         FavDraftOrderId.toLong(),
//                         UpdateDraftOrderRequest(updatedDraftOrder)
//                     )
                                }

                                IS_Liked = false

                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()


                    } else { // kda ha3ml add
                        Log.d("TAG", "onViewCreated: prouductInfo case en el product da is not  liked (case else ) w h3mel add  ${IS_Liked}  ")
                        lifecycleScope.launch {
                            var currentDraftOrderItems: MutableList<LineItem> =
                                mutableListOf() // to store my previous liked products
                            draftOrderRequest.draft_order.line_items.forEach {
                                currentDraftOrderItems.add(it) // getting the old list of line_items
                            }
                            // now i want to add this list to my new liked item
                            currentDraftOrderItems.add(draftOrderRequest(proudct).draft_order.line_items.get(0))
                            var updatedDraftOrder = draftOrderRequest(proudct).draft_order
                            updatedDraftOrder.line_items = currentDraftOrderItems
                            ProductInfoViewModel.updateDraftOrder(
                                FavDraftOrderId.toLong(),
                                UpdateDraftOrderRequest(updatedDraftOrder)
                            )
                            binding!!.ivAddProuductToFavorite.setColorFilter(Color.BLUE)


                        }
                        Snackbar.make(requireView(), "this products was saved to your favorite", 2000).show()



                        IS_Liked = true
                    }

                    Log.d("TAG", "onViewCreated:  sho8l el zrarez 5elels wel isliked b2a  ${IS_Liked} ")


                }
            }

        }

    }

    private fun getProductDetails() {

        lifecycleScope.launch {
            ProductInfoViewModel.productDetailsStateFlow.collectLatest { result ->
                when (result) {
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
                        productTitle = productsList.get(0).title
                        setUpTheAvailableSizesAndColors(productsList.get(0).options)


                    }
                }

            }
        }


    }

    fun setProudctDetailToUI(productsList: List<Products>) {
        binding!!.tvProuductName.text = productsList.get(0).title
        binding!!.tvProuductDesc.text = productsList.get(0).bodyHtml

        // da el se3r w hyt8yar based on shared pref in setting
        when (Currentcurrency) {
            1F -> {
                binding!!.tvProuductPrice.text = "${productsList.get(0).variants.get(0).price} EGP"
            }

            else  -> {
                val prics = productsList.get(0).variants.get(0).price.toDouble()
                binding!!.tvProuductPrice.text =
                    String.format("%.2f", (prics * Currentcurrency!!)) + " USD"
//
//                ProductInfoViewModel.getCurrencyRate("EGP", "USD")
//                val prics = productsList.get(0).variants.get(0).price.toDouble()
//                getCurrencyRate(prics)

//               val newPrice = (prics * conversionRate!!)
//               binding.tvProuductPrice.text = "${newPrice} USD"
            }
        }


        val randomRating = ratingList[Random.nextInt(ratingList.size)]
        val randomReview = reviewList[Random.nextInt(reviewList.size)]

        // Set the rating and review to the UI
        binding!!.rbProuductRatingBar.rating = randomRating
        binding!!.rbProuductRatingBar.setIsIndicator(true) // to make the rating bar unchangable
        binding!!.tvProuductReview.text = randomReview
        Log.d("TAG", "getProductDetails: url sora  ${productsList.get(0).images.get(0).src} ")

        // Set the image src to slider
        val imageSlideModels = productsList.get(0).images.map {
            SlideModel(
                it.src, "",
                ScaleTypes.FIT
            )
        }
        binding!!.isProuductImage.setImageList(imageSlideModels)
    }

    fun setUpTheAvailableSizesAndColors(optionList: List<Option>) {
        optionList.forEach {
            when (it.name) {
                "Color" -> {
                    val availableColorsList = it.values.map { AvailableColor(it) }
                    availableColorsAdapter.submitList(availableColorsList)
                }

                "Size" -> {
                    val availableSizesList = it.values.map { AvailableSizes(it) }
                    availableSizesAdapter.submitList(availableSizesList)


                }

                else -> {}
            }
        }
    }

    override fun OnClick(t: AvailableSizes) {

        when (IsGuestMode){

            "true"->{
                Snackbar.make(
                    requireView(),
                    "you cant choose a size in guest mode",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            else ->{
                if (choosenSize.isNullOrBlank()) {
                    choosenSize = t.size
                    Log.d("TAG", "OnColorClick: choosen size  is  ${choosenSize} ")
                    Snackbar.make(requireView(), "you have choosen a size ${t.size} ", 2000).show()
                } else {

                    choosenSize = t.size
                    Log.d("TAG", "OnColorClick: choosen size  is  ${choosenSize} ")
                    Snackbar.make(
                        requireView(),
                        "Your choosen Size has been Changed To Be ${t.size} ",
                        2000
                    ).show()
                }
            }
        }



    }

    override fun OnColorClick(t: AvailableColor) {
        when (IsGuestMode){
            "true"->{
                Snackbar.make(
                    requireView(),
                    "you cant do that  in guest mode",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            else ->{
                if (choosenColor.isNullOrBlank()) {
                    choosenColor = t.color
                    Log.d("TAG", "OnColorClick: choosen colr is  ${choosenColor} ")
                    Snackbar.make(requireView(), "you have choosen a color ${t.color} ", 2000).show()
                } else {
                    choosenColor = t.color
                    Log.d("TAG", "OnColorClick: choosen colr is  ${choosenColor} ")
                    Snackbar.make(
                        requireView(),
                        "your choosen color has been changed to be ${t.color} ",
                        2000
                    ).show()

                }
            }

        }


    }

    fun getProductDetailsById() {
        val productId = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId

        if (productId != null || productId != 0L) {
            ProductInfoViewModel.getProductDetails(productId)
            getProductDetails()
        } else {
            Snackbar.make(
                requireView(),
                "there was a problem fetshng this prouduct details at the moment   ",
                2000
            ).show()
        }
    }

    fun getCurrencyRate(price: Double) {

        lifecycleScope.launch {
            ProductInfoViewModel.currencyStateFlow.collectLatest { result ->
                when (result) {
                    DataState.Loading -> {
                        Log.d("TAG", "getCurrencyRate: loading   ")
                    }

                    is DataState.OnFailed -> {
                        Log.d("TAG", "getCurrencyRate: failure    ")
                    }

                    is DataState.OnSuccess<*> -> {

                        conversionRate = (result.data as ExchangeRateResponse).conversion_rate
                        Log.d("TAG", "getCurrencyRate: succes   $conversionRate   ")
                        binding!!.tvProuductPrice.text =
                            String.format("%.2f", (price * conversionRate!!)) + " USD"


                    }
                }

            }
        }
    }


    private fun draftOrderRequest(): DraftOrderRequest {
        val draftOrderRequest = DraftOrderRequest(
            draft_order = DraftOrder(
                line_items = listOf(
                    LineItem(
                        sku = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId.toString(),
                        name = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId.toString(),
                        id = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId,
                        product_id = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId,
                        title = proudct.title, price = proudct.variants[0].price, quantity = 1
                    )
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

    fun getSpecificDraftOrderById(FavDraftOrderId: Long) {
        lifecycleScope.launch {
            ProductInfoViewModel.getSpecificDraftOrder(FavDraftOrderId)
            ProductInfoViewModel.specificDraftOrders.collectLatest { result ->
                when (result) {
                    DataState.Loading -> {
                        Log.d("TAG", "getSpecificDraftOrderById prouductInfo: loading  ")
                    }

                    is DataState.OnFailed -> {
                        Log.d(
                            "TAG",
                            "getSpecificDraftOrderById: prouductInfo failie ${result.msg}  "
                        )
                    }


                    is DataState.OnSuccess<*> -> {
                        Log.d("TAG", "getSpecificDraftOrderById: prouductInfo success")
                        draftOrderRequest = result.data as DraftOrderRequest
                        draftOrderRequest.draft_order.line_items.forEach {

                            if (it.title == productTitle) {
                                Log.d(
                                    "TAG",
                                    "getSpecificDraftOrderById: prouductInfo case if >> kda el product da mwgod fel draft order "
                                )
                                IS_Liked = true
                            }
                        }

                        if (IS_Liked) {
                            binding?.ivAddProuductToFavorite?.setColorFilter(Color.BLUE)
                        } else {
                            binding?.ivAddProuductToFavorite?.setColorFilter(Color.BLACK)
                        }

                    }
                }


            }

        }

    }

    private fun draftOrderRequest(prduct: Products): DraftOrderRequest {
        val draftOrderRequest = DraftOrderRequest(
            draft_order = DraftOrder(
                line_items = listOf(
                    LineItem(
                        id = prduct.id,
                        product_id = prduct.id,
                        sku = "${prduct.id.toString()}##${prduct.image?.src}",
                        title = prduct.title, price = prduct.variants[0].price, quantity = 1
                    )
                ),
                use_customer_default_address = true,
                applied_discount = AppliedDiscount(),
                customer = Customer(8220771385648)
            )

        )
        return draftOrderRequest
    }

    override fun onDestroy() {
        super.onDestroy()

    }


}