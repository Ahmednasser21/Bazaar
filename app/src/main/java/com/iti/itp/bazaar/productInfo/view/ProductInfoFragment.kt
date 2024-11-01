package com.iti.itp.bazaar.productInfo.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import com.iti.itp.bazaar.auth.AuthActivity
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.ColorItemBottomSheetBinding
import com.iti.itp.bazaar.databinding.FragmentProuductnfoBinding
import com.iti.itp.bazaar.databinding.SizeItemBottomSheetBinding
import com.iti.itp.bazaar.dto.*
import com.iti.itp.bazaar.handlers.FavoritesHandler
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

class ProductInfoFragment : Fragment(), OnClickListner<AvailableSizes>, OnColorClickListner {
    private var binding: FragmentProuductnfoBinding? = null
    private lateinit var productInfoViewModel: ProductInfoViewModel
    private lateinit var vmFactory: ProuductIfonViewModelFactory
    private lateinit var sizeBottomSheetBinding: SizeItemBottomSheetBinding
    private lateinit var colorBottomSheetBinding: ColorItemBottomSheetBinding
    private lateinit var sizeDialog: BottomSheetDialog
    private lateinit var colorDialog: BottomSheetDialog
    private lateinit var customerId: String
    private lateinit var mySharedPreference: SharedPreferences
    private lateinit var favoritesHandler: FavoritesHandler
    private lateinit var favDraftOrderId: String
    private var cartDraftOrderId: String? = null
    private lateinit var isGuestMode: String
    private var product: Products? = null
    private var productTitle: String = ""

    private val ratingList = listOf(2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mySharedPreference = requireActivity().getSharedPreferences(
            MyConstants.MY_SHARED_PREFERANCE,
            Context.MODE_PRIVATE
        )
        customerId = mySharedPreference.getString(MyConstants.CUSOMER_ID, "0").toString()
        isGuestMode = mySharedPreference.getString(MyConstants.IS_GUEST, "false") ?: "false"
        binding = FragmentProuductnfoBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        initializeData()
        setupClickListeners()
    }

    private fun setupViewModel() {
        vmFactory = ProuductIfonViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            ),
            CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        productInfoViewModel = ViewModelProvider(this, vmFactory)[ProductInfoViewModel::class.java]
    }

    private fun initializeData() {
        favoritesHandler = FavoritesHandler(productInfoViewModel, mySharedPreference)
        lifecycleScope.launch {
            favoritesHandler.initialize()
        }

        favDraftOrderId = mySharedPreference.getString(MyConstants.FAV_DRAFT_ORDERS_ID, "") ?: ""
        cartDraftOrderId = mySharedPreference.getString(MyConstants.CART_DRAFT_ORDER_ID, "0")

        getProductDetailsById()
        if (favDraftOrderId.isNotEmpty()) {
            getSpecificDraftOrderById(favDraftOrderId.toLong())
        }
    }
    private fun getSpecificDraftOrderById(draftOrderId: Long) {
        lifecycleScope.launch {
            try {
                productInfoViewModel.getSpecificDraftOrder(draftOrderId)
                productInfoViewModel.specificDraftOrders.collectLatest { result ->
                    when (result) {
                        is DataState.Loading -> {
                            Log.d("TAG", "getSpecificDraftOrderById: loading")
                        }
                        is DataState.OnFailed -> {
                            Log.d("TAG", "getSpecificDraftOrderById: failure ${result.msg}")
                        }
                        is DataState.OnSuccess<*> -> {
                            val draftOrderRequest = result.data as DraftOrderRequest
                            // Safe call to product since it might not be initialized yet
                            product?.let { currentProduct ->
                                if (favoritesHandler.isProductInFavorites(draftOrderRequest, currentProduct.id.toString())) {
                                    binding?.ivAddProuductToFavorite?.setImageResource(R.drawable.filled_favorite)
                                } else {
                                    binding?.ivAddProuductToFavorite?.setImageResource(R.drawable.favorite)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TAG", "Error in getSpecificDraftOrderById: ${e.message}")
                withContext(Dispatchers.Main) {
                    showSnackbar("Error loading draft order")
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding?.apply {
            sizeCardView.setOnClickListener { showSizeBottomSheet() }
            colorCardView.setOnClickListener { showColorBottomSheet() }
            btnAddToCart.setOnClickListener { handleAddToCart() }
            ivAddProuductToFavorite.setOnClickListener { handleAddToFavorites() }
        }
    }

    private fun handleAddToCart() {
        if (isGuestMode == "true") {
            Snackbar.make(
                requireView(),
                "Signup first to use this feature",
                Snackbar.LENGTH_LONG
            ).setAction("Signup") {
                val intent = Intent(requireActivity(), AuthActivity::class.java)
                intent.putExtra("navigateToFragment", "SignUpFragment")
                startActivity(intent)
            }.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
                .show()
        }else{
            binding?.let { binding ->
                when {
                    binding.tvSize.text == "Size" -> {
                        showSnackbar("You must choose a size")
                    }
                    binding.tvColor.text == "Color" -> {
                        showSnackbar("You must choose a color")
                    }
                    else -> {
                        addProductToCart()
                    }
                }
            }
        }

    }

    private fun addProductToCart() {
            viewLifecycleOwner.lifecycleScope.launch {
                product?.let { currentProduct ->
                    try {
                        productInfoViewModel.getPriceRules()
                        cartDraftOrderId?.toLong()?.let { cartId ->
                            productInfoViewModel.getSpecificDraftOrder(cartId)
                            productInfoViewModel.specificDraftOrders.collect { state ->
                                handleCartState(state, currentProduct)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showSnackbar("Error adding product to cart")
                        }
                    }
                }
            }

    }

    private suspend fun handleCartState(state: DataState, currentProduct: Products) {
        when (state) {
            is DataState.OnSuccess<*> -> {
                val data = state.data as DraftOrderRequest
                val existingOrder = data.draft_order
                updateCartDraftOrder(existingOrder, currentProduct)
                withContext(Dispatchers.Main) {
                    showSnackbar("Product is added to your cart")
                }
            }
            else -> {} // Handle other states if needed
        }
    }

    private fun updateCartDraftOrder(existingOrder: DraftOrder, currentProduct: Products) {
        val updatedLineItems = (existingOrder.line_items).toMutableList()
        binding?.let { binding ->
            updatedLineItems.add(
                LineItem(
                    sku = "${ProductInfoFragmentArgs.fromBundle(requireArguments()).productId}##${binding.tvColor.text}##${binding.tvSize.text}",
                    id = ProductInfoFragmentArgs.fromBundle(requireArguments()).productId,
                    variant_title = "variant",
                    product_id = ProductInfoFragmentArgs.fromBundle(requireArguments()).productId,
                    title = currentProduct.title,
                    price = currentProduct.variants[0].price,
                    quantity = 1
                )
            )
        }

        cartDraftOrderId?.toLong()?.let { cartId ->
            productInfoViewModel.updateDraftOrder(
                cartId,
                UpdateDraftOrderRequest(
                    DraftOrder(
                        applied_discount = AppliedDiscount(
                            null, null, null, null, null
                        ),
                        customer = Customer(8220771418416),
                        use_customer_default_address = true,
                        line_items = updatedLineItems.map {
                            LineItem(
                                sku = it.sku,
                                product_id = it.product_id,
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

    private fun handleAddToFavorites() {
        if (isGuestMode == "true") {
            Snackbar.make(
                requireView(),
                "Signup first to use this feature",
                Snackbar.LENGTH_LONG
            ).setAction("Signup") {
                val intent = Intent(requireActivity(), AuthActivity::class.java)
                intent.putExtra("navigateToFragment", "SignUpFragment")
                startActivity(intent)
            }.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
                .show()
        }else{
            product?.let { currentProduct ->
                favoritesHandler.addProductToFavorites(
                    product = currentProduct,
                    onAdded = {
                        showSnackbar("Added to your favorite")
                        binding?.ivAddProuductToFavorite?.setImageResource(R.drawable.filled_favorite)
                    },
                    onAlreadyExists = {
                        favoritesHandler.removeFromFavorites(
                            product = currentProduct,
                            onRemoved = {
                                showSnackbar("Deleted successfully")
                                binding?.ivAddProuductToFavorite?.setImageResource(R.drawable.favorite)
                            }
                        )
                    }
                )
            }
        }
    }

    private fun getProductDetailsById() {
        val productId = ProductInfoFragmentArgs.fromBundle(requireArguments()).productId
        if (productId != 0L) {
            productInfoViewModel.getProductDetails(productId)
            observeProductDetails()
        } else {
            showSnackbar("There was a problem fetching this product details at the moment")
        }
    }

    private fun observeProductDetails() {
        lifecycleScope.launch {
            productInfoViewModel.productDetailsStateFlow.collectLatest { result ->
                when (result) {
                    is DataState.Loading -> {
                        Log.d("TAG", "getProductDetails: loading")
                    }
                    is DataState.OnSuccess<*> -> {
                        val productsList = (result.data as ProductResponse).products
                        if (productsList.isNotEmpty()) {
                            product = productsList[0]
                            productTitle = product?.title ?: ""
                            setProductDetailToUI(productsList)
                        }
                    }
                    is DataState.OnFailed -> {
                        Log.d("TAG", "getProductDetails: failure and error msg is ${result.msg}")
                    }
                }
            }
        }
    }

    private fun setProductDetailToUI(productsList: List<Products>) {
        binding?.apply {
            val title = productsList[0].title.split("|")
            tvProuductName.text = title.getOrNull(1) ?: title[0]
            tvProuductDesc.text = productsList[0].bodyHtml
            tvBrand.text = productsList[0].vendor

            val randomRating = ratingList[Random.nextInt(ratingList.size)]
            rbProuductRatingBar.rating = randomRating
            ratingOfTen.text = "(${randomRating * 2})"
            rbProuductRatingBar.setIsIndicator(true)

            val imageSlideModels = productsList[0].images.map {
                SlideModel(it.src, "", ScaleTypes.FIT)
            }
            isProuductImage.setImageList(imageSlideModels)
        }
    }

    private fun showSizeBottomSheet() {
        product?.let { currentProduct ->
            sizeDialog = BottomSheetDialog(requireContext())
            sizeBottomSheetBinding = SizeItemBottomSheetBinding.inflate(layoutInflater)
            sizeDialog.setContentView(sizeBottomSheetBinding.root)

            val adapter = AvailableSizesAdapter(this)
            sizeBottomSheetBinding.recyclerView2.apply {
                this.adapter = adapter
                layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
            }

            if (currentProduct.options[0].name == "Size") {
                val availableSizesList = currentProduct.options[0].values.map { AvailableSizes(it) }
                adapter.submitList(availableSizesList)
            }

            sizeDialog.show()
        }
    }

    private fun showColorBottomSheet() {
        product?.let { currentProduct ->
            colorDialog = BottomSheetDialog(requireContext())
            colorBottomSheetBinding = ColorItemBottomSheetBinding.inflate(layoutInflater)
            colorDialog.setContentView(colorBottomSheetBinding.root)

            val adapter = AvailableColorAdapter(this)
            colorBottomSheetBinding.recyclerView2.apply {
                this.adapter = adapter
                layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
            }

            if (currentProduct.options[1].name == "Color") {
                val availableColorsList = currentProduct.options[1].values.map { AvailableColor(it) }
                adapter.submitList(availableColorsList)
            }

            colorDialog.show()
        }
    }
    private fun refreshDraftOrder() {
        if (favDraftOrderId.isNotEmpty()) {
            getSpecificDraftOrderById(favDraftOrderId.toLong())
        }
    }

    private fun showSnackbar(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }

    override fun OnClick(t: AvailableSizes) {
        binding?.tvSize?.text = t.size
        sizeDialog.dismiss()
    }

    override fun OnColorClick(t: AvailableColor) {
        binding?.tvColor?.text = t.color
        colorDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
    override fun onStart() {
        super.onStart()
        getProductDetailsById()
        refreshDraftOrder()
    }
}