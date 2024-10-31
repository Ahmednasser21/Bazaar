package com.iti.itp.bazaar.productInfo.view

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.productinfoform_commerce.productInfo.viewModel.ProuductIfonViewModelFactory
import com.example.productinfoform_commerce.productInfo.viewModel.ProductInfoViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.ColorItemBottomSheetBinding
import com.iti.itp.bazaar.databinding.FragmentProuductnfoBinding
import com.iti.itp.bazaar.databinding.SizeItemBottomSheetBinding
import com.iti.itp.bazaar.dto.AppliedDiscount
import com.iti.itp.bazaar.dto.Customer
import com.iti.itp.bazaar.dto.DraftOrder
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.LineItem
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.productInfo.OnClickListner
import com.iti.itp.bazaar.productInfo.OnColorClickListner
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


class ProuductnfoFragment : Fragment(), OnClickListner<AvailableSizes>, OnColorClickListner {

    var binding: FragmentProuductnfoBinding? = null
    lateinit var productInfoViewModel: ProductInfoViewModel
    lateinit var vmFActory: ProuductIfonViewModelFactory
    lateinit var sizeBottomSheetBinding: SizeItemBottomSheetBinding
    lateinit var colorBottomSheetBinding: ColorItemBottomSheetBinding
    private lateinit var sizeDialog: BottomSheetDialog
    private lateinit var colorDialog: BottomSheetDialog
    private lateinit var customerId: String
    lateinit var mySharedPrefrence: SharedPreferences
    lateinit var draftOrderRequest: DraftOrderRequest

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
    private var cartDraftOrderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        CurrencySharedPreferences = requireActivity().getSharedPreferences(
//            "currencySharedPrefs",
//            Context.MODE_PRIVATE
//        )
        mySharedPrefrence = requireActivity().getSharedPreferences(
            MyConstants.MY_SHARED_PREFERANCE,
            Context.MODE_PRIVATE
        )
//        Currentcurrency = CurrencySharedPreferences.getFloat("currency", 1F) ?: 1F
//        Log.d("TAG", "onViewCreated: ek currency rate is :$Currentcurrency ")
//        IsGuestMode = mySharedPrefrence.getString(MyConstants.IS_GUEST, "false") ?: "false"
        customerId = mySharedPrefrence.getString(MyConstants.CUSOMER_ID, "0").toString()

        binding = FragmentProuductnfoBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        FavDraftOrderId = mySharedPrefrence.getString(MyConstants.FAV_DRAFT_ORDERS_ID, "") ?: ""
        cartDraftOrderId = mySharedPrefrence.getString(MyConstants.CART_DRAFT_ORDER_ID, "0")
        Log.d("TAG", "onViewCreated fav draft order id : ${FavDraftOrderId} ")
        binding?.sizeCardView?.setOnClickListener {
            showSizeBottomSheet()
        }
        binding?.colorCardView?.setOnClickListener {
            showColorBottomSheet()
        }
        vmFActory = ProuductIfonViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            ), CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        productInfoViewModel =
            ViewModelProvider(this, vmFActory).get(ProductInfoViewModel::class.java)
// here i should recive args from any string with id : Long

        getProductDetailsById()


        //handel btn_addToCart
        binding!!.btnAddToCart.setOnClickListener {

            if (binding!!.tvSize.text == "Size") {
                Snackbar.make(
                    requireView(),
                    "You must choose a size",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else if (binding!!.tvColor.text == "Color") {
                Snackbar.make(
                    requireView(),
                    "You must choose a color",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    productInfoViewModel.getPriceRules()
                    productInfoViewModel.getSpecificDraftOrder(cartDraftOrderId?.toLong()!!)
                    productInfoViewModel.specificDraftOrders.collect { state ->
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
                                        ).productId.toString() + "##" + binding?.tvColor?.text + "##" + binding?.tvSize?.text,
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
                                productInfoViewModel.updateDraftOrder(
                                    cartDraftOrderId?.toLong() ?: 0,
                                    UpdateDraftOrderRequest(
                                        DraftOrder(
                                            applied_discount = AppliedDiscount(
                                                null,
                                                value_type = null,
                                                value = null,
                                                amount = null,
                                                title = null
                                            ),
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

        //getting the specificFavDraftOrder

        getSpecificDraftOrderById(FavDraftOrderId.toLong())

        //handel btn add to favourite
        binding!!.ivAddProuductToFavorite.setOnClickListener {
            val originalList = draftOrderRequest.draft_order.line_items.toMutableList()

            // Check if the product is already in favorites
            val x = originalList.any {//0
                val string = it.sku?.split("##")
                val id = string?.get(0)
                id == proudct.id.toString()
            }

            if (x) {
                Snackbar.make(requireView(), "This item is already in your favorite list", 2000)
                    .show()
                Log.i("TAG", "onViewCreated: ${proudct.id}")
            } else {
                originalList.add(
                    LineItem(
                        sku = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId.toString() + "##" + proudct.image?.src,
                        id = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId,
                        variant_title = "dgldsjglk",
                        product_id = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId,
                        title = proudct.title,
                        price = proudct.variants[0].price,
                        quantity = 1
                    )
                )

                val draftOrderRequest = UpdateDraftOrderRequest(
                    DraftOrder(
                        originalList,
                        applied_discount = null,
                        customer = Customer(customerId.toLong()),
                        use_customer_default_address = true,
                    )
                )

                productInfoViewModel.updateDraftOrder(FavDraftOrderId.toLong(), draftOrderRequest)
                binding!!.ivAddProuductToFavorite.setImageResource(R.drawable.filled_favorite)
            }
        }




    }

    private fun getProductDetails() {

        lifecycleScope.launch {
            productInfoViewModel.productDetailsStateFlow.collectLatest { result ->
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
                        //setUpTheAvailableSizesAndColors(productsList.get(0).options)


                    }
                }

            }
        }


    }

    fun setProudctDetailToUI(productsList: List<Products>) {
        val title = productsList.get(0).title.split("|")
        binding!!.tvProuductName.text = title[1]
        binding!!.tvProuductDesc.text = productsList[0].bodyHtml
        binding!!.tvBrand.text = productsList[0].vendor

        // da el se3r w hyt8yar based on shared pref in setting
        when (Currentcurrency) {
            1F -> {
                binding!!.tvProuductPrice.text = "${productsList.get(0).variants.get(0).price} EGP"
            }

            else -> {
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
        binding?.let { it.ratingOfTen.text  = "(${it.rbProuductRatingBar.rating*2})"}
        binding!!.rbProuductRatingBar.setIsIndicator(true) // to make the rating bar unchangable
        //binding!!.tvProuductReview.text = randomReview
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

//    fun setUpTheAvailableSizesAndColors(optionList: List<Option>) {
//        optionList.forEach {
//            when (it.name) {
//                "Color" -> {
//                    val availableColorsList = it.values.map { AvailableColor(it) }
//                    availableColorsAdapter.submitList(availableColorsList)
//                }
//
//                "Size" -> {
//                    val availableSizesList = it.values.map { AvailableSizes(it) }
//                    availableSizesAdapter.submitList(availableSizesList)
//
//
//                }
//
//                else -> {}
//            }
//        }
//    }

    override fun OnClick(t: AvailableSizes) {
        binding?.tvSize?.text = t.size
        sizeDialog.dismiss()
    }

    override fun OnColorClick(t: AvailableColor) {
        binding?.tvColor?.text = t.color
        colorDialog.dismiss()
    }


    fun getProductDetailsById() {
        val productId = ProuductnfoFragmentArgs.fromBundle(requireArguments()).productId

        if (productId != null || productId != 0L) {
            productInfoViewModel.getProductDetails(productId)
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
            productInfoViewModel.currencyStateFlow.collectLatest { result ->
                when (result) {
                    DataState.Loading -> {
                        Log.d("TAG", "getCurrencyRate: loading   ")
                    }

                    is DataState.OnFailed -> {
                        Log.d("TAG", "getCurrencyRate: failure    ")
                    }

                    is DataState.OnSuccess<*> -> {

//                        conversionRate = (result.data as ExchangeRateResponse).conversion_rate
//                        Log.d("TAG", "getCurrencyRate: succes   $conversionRate   ")
//                        binding!!.tvProuductPrice.text =
//                            String.format("%.2f", (price * conversionRate!!)) + " USD"


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
                applied_discount = AppliedDiscount(
                    description = null,
                    value_type = null,
                    value = null,
                    amount = null,
                    title = null
                ),
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
            productInfoViewModel.getSpecificDraftOrder(FavDraftOrderId)
            productInfoViewModel.specificDraftOrders.collectLatest { result ->
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

                        val originalList = draftOrderRequest.draft_order.line_items.toMutableList()

                        // Check if the product is already in favorites
                        val x = originalList.any {//0
                            val string = it.sku?.split("##")
                            val id = string?.get(0)
                            id == proudct.id.toString()
                        }

                        if (x) {
                            getProductDetails()
                            lifecycleScope.launch(Dispatchers.IO){
                                withContext(Dispatchers.Main){
                                    binding!!.ivAddProuductToFavorite.setImageResource(R.drawable.filled_favorite)
                                    binding!!.ivAddProuductToFavorite.setOnClickListener {
                                        // Remove the item by filtering out the matching SKU
                                        val updatedLineItems = draftOrderRequest.draft_order.line_items.filter { lineItem ->
                                            val skuParts = lineItem.sku?.split("##")
                                            skuParts?.firstOrNull() != proudct.id.toString()
                                        }

                                        val updateRequest = UpdateDraftOrderRequest(
                                            DraftOrder(
                                                line_items = updatedLineItems,
                                                applied_discount = null,
                                                customer = Customer(customerId.toLong()),
                                                use_customer_default_address = true,
                                            )
                                        )

                                        productInfoViewModel.updateDraftOrder(FavDraftOrderId, updateRequest)
                                        binding!!.ivAddProuductToFavorite.setImageResource(R.drawable.favorite)

                                        // Show feedback to user
                                        Snackbar.make(
                                            requireView(),
                                            "Removed from favorites",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }


                        }

//                        if (IS_Liked) {
//                            binding?.ivAddProuductToFavorite?.setColorFilter(Color.BLUE)
//                        } else {
//                            binding?.ivAddProuductToFavorite?.setColorFilter(Color.BLACK)
//                        }

                    }
                }


            }

        }

    }

    override fun onStart() {
        super.onStart()
        getProductDetails()
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
                applied_discount = AppliedDiscount(
                    description = null,
                    value_type = null,
                    value = null,
                    amount = null,
                    title = null
                ),
                customer = Customer(8220771385648)
            )

        )
        return draftOrderRequest
    }

    override fun onDestroy() {
        super.onDestroy()

    }


    private fun showSizeBottomSheet() {
        sizeDialog = BottomSheetDialog(requireContext())
        sizeBottomSheetBinding = SizeItemBottomSheetBinding.inflate(layoutInflater)
        sizeDialog.setContentView(sizeBottomSheetBinding.root)
        val adapter = AvailableSizesAdapter(this@ProuductnfoFragment)
        sizeBottomSheetBinding.recyclerView2.apply {
            this.adapter = adapter
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        }

        when (proudct.options[0].name) {
            "Size" -> {
                val availableSizesList = proudct.options[0].values.map { AvailableSizes(it) }
                adapter.submitList(availableSizesList)

            }
        }

        sizeDialog.show()
    }


    private fun showColorBottomSheet() {

        colorDialog = BottomSheetDialog(requireContext())
        colorBottomSheetBinding = ColorItemBottomSheetBinding.inflate(layoutInflater)
        colorDialog.setContentView(colorBottomSheetBinding.root)
        val adapter = AvailableColorAdapter(this@ProuductnfoFragment)
        colorBottomSheetBinding.recyclerView2.apply {
            this.adapter = adapter
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        }

        when (proudct.options[1].name) {
            "Color" -> {
                Log.i("TAG", "colors are : ${proudct.options[1].values} ").toString()
                val availableColorsList = proudct.options[1].values.map { AvailableColor(it) }
                adapter.submitList(availableColorsList)
            }
        }

        colorDialog.show()
    }


}