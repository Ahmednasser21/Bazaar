package com.iti.itp.bazaar.mainActivity.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.productinfoform_commerce.productInfo.viewModel.ProductInfoViewModel
import com.example.productinfoform_commerce.productInfo.viewModel.ProuductIfonViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentHomeBinding
import com.iti.itp.bazaar.dto.DraftOrder
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.PriceRuleDto
import com.iti.itp.bazaar.handlers.FavoritesHandler
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.mainActivity.ui.categories.CategoriesViewModel
import com.iti.itp.bazaar.mainActivity.ui.categories.CategoriesViewModelFactory
import com.iti.itp.bazaar.mainActivity.ui.products.OnFavouriteClickListener
import com.iti.itp.bazaar.mainActivity.ui.products.OnProductClickListener
import com.iti.itp.bazaar.mainActivity.ui.products.ProductsAdapter
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.responses.PriceRulesResponse
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.responses.SmartCollectionsResponse
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.search.viewModel.SearchViewModel
import com.iti.itp.bazaar.search.viewModel.SearchViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "HomeFragment"

class HomeFragment : Fragment(), OnBrandClickListener, OnProductClickListener,
    OnFavouriteClickListener,OnCategoryClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var brandsAdapter: BrandsAdapter
    private lateinit var brandsRecycler: RecyclerView
    private lateinit var saleRecycler: RecyclerView
    private lateinit var saleProgressBar: ProgressBar
    private lateinit var list: List<PriceRuleDto>
    private lateinit var categoriesRec: RecyclerView
    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var saleProductsAdapter: ProductsAdapter
    private lateinit var productInfoViewModel: ProductInfoViewModel
    private lateinit var productInfoViewModelFactory: ProuductIfonViewModelFactory
    private lateinit var sharedPrefs:SharedPreferences
    private var customerId:String? = null
    private var FavoriteDraftOrderId:String? = null
    var draftOrder:DraftOrderRequest? = null
    private lateinit var favoritesHandler: FavoritesHandler


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory = HomeViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            )
        )
        val categoriesFactory = CategoriesViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            )
        )
        val searchFactory = SearchViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            )
        )
        categoriesViewModel =
            ViewModelProvider(this, categoriesFactory)[CategoriesViewModel::class.java]
        searchViewModel =
            ViewModelProvider(requireActivity(), searchFactory)[SearchViewModel::class.java]
        homeViewModel = ViewModelProvider(requireActivity(), factory)[HomeViewModel::class.java]
        brandsAdapter = BrandsAdapter(this)
        saleProductsAdapter = ProductsAdapter(true,this, this)
        productInfoViewModelFactory = ProuductIfonViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            ), CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        productInfoViewModel =
            ViewModelProvider(this, productInfoViewModelFactory).get(ProductInfoViewModel::class.java)
        sharedPrefs = requireActivity().getSharedPreferences(MyConstants.MY_SHARED_PREFERANCE, Context.MODE_PRIVATE)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesHandler = FavoritesHandler(productInfoViewModel, sharedPrefs)

        // 2. Initialize the cache (optional, but recommended)
        lifecycleScope.launch {
            favoritesHandler.initialize()
        }

        customerId = sharedPrefs.getString(MyConstants.CUSOMER_ID, "0")
        FavoriteDraftOrderId = sharedPrefs.getString(MyConstants.FAV_DRAFT_ORDERS_ID, "0")
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner){showExitAlertDialog()}
        categoriesRec = binding.recCategoriesHome.apply {
            adapter = CategoriesAdapter(categoriesList(),this@HomeFragment)
            layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
        }

        binding.imageSlider.setImageList(getListOfImageAds())
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        homeViewModel.getPriceRules()


        lifecycleScope.launch {
            homeViewModel.priceRules.collect { state ->
                when (state) {
                    is DataState.Loading -> {}
                    is DataState.OnFailed -> Snackbar.make(
                        requireView(),
                        "Failed to get coupons",
                        2000
                    ).show()

                    is DataState.OnSuccess<*> -> {
                        val data = state.data as PriceRulesResponse
                        list = data.priceRules
                    }
                }
            }
        }

        binding.imageSlider.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                when (position) {
                    0 -> {
                        val clip = ClipData.newPlainText("ad", list[position].title)
                        clipboard.setPrimaryClip(clip)
                        Snackbar.make(view, "Coupon is copied", 2000).show()
                    }

                    1 -> {
                        val clip = ClipData.newPlainText("ad", list[position].title)
                        clipboard.setPrimaryClip(clip)
                        Snackbar.make(view, "Coupon is copied", 2000).show()
                    }

                    2 -> {
                        val clip = ClipData.newPlainText("ad", list[position].title)
                        clipboard.setPrimaryClip(clip)
                        Snackbar.make(view, "Coupon is copied", 2000).show()
                    }
                }
            }

            override fun doubleClick(position: Int) {
                // Do not use onItemSelected if you are using a double click listener at the same time.
                // Its just added for specific cases.
                // Listen for clicks under 250 milliseconds.
            }
        })

        saleProgressBar =binding.saleProgressBar

        brandsRecycler = binding.recBrands.apply {
            adapter = brandsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
        categoriesViewModel.getCategoryProducts(480515522864)
        searchViewModel.getAllProducts()
        getCategoryProducts()
        saleRecycler = binding.recSaleHome.apply {
            adapter = saleProductsAdapter
            layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL,false)
        }
        // brandsProgressBar = binding.progBrands
        homeViewModel.getVendors()
        getProductVendors()
    }

    private fun getProductVendors() {
        lifecycleScope.launch {
            homeViewModel.brandStateFlow.collectLatest { result ->

                when (result) {

                    is DataState.Loading -> {
                        //brandsProgressBar.visibility = View.VISIBLE
                        brandsRecycler.visibility = View.INVISIBLE
                    }

                    is DataState.OnSuccess<*> -> {
                        //brandsProgressBar.visibility = View.GONE
                        brandsRecycler.visibility = View.VISIBLE
                        val smartCollectionsResponse = result.data as SmartCollectionsResponse
                        val brandList = smartCollectionsResponse.smartCollections
                        Log.i(TAG, "getProductVendors: $brandList ")
                        brandsAdapter.submitList(brandList)

                    }

                    is DataState.OnFailed -> {
                        //brandsProgressBar.visibility = View.GONE
                        Snackbar.make(requireView(), "Failed to get data", Snackbar.LENGTH_SHORT)
                            .show()
                    }

                }

            }
        }
    }

    override fun onBrandClick(brandTitle: String) {
        val action = HomeFragmentDirections.actionNavHomeToBrandProducts(brandTitle)
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun getListOfImageAds(): List<SlideModel> {
        val adsImages: List<SlideModel> = listOf(
            SlideModel(
                "https://img.freepik.com/premium-vector/sale-offer-label-banner-discount-offer-promotion_157027-1265.jpg",
                "",
                ScaleTypes.FIT
            ),
            SlideModel(
                "https://cdn.shopify.com/app-store/listing_images/fca0d2f332428be47d5491598535bbb3/icon/CKvtw8z0lu8CEAE=.jpg",
                "",
                ScaleTypes.FIT
            ),
            SlideModel(
                "https://www.shutterstock.com/image-vector/best-deals-sale-banner-design-600nw-2469448247.jpg",
                "",
                ScaleTypes.FIT
            ),
        )
        return adsImages
    }

    private fun showExitAlertDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Exit Bazaar")
            .setMessage("Are you sure you want to exit Bazaar?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                requireActivity().finishAffinity()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun categoriesList(): List<CategoryItem> {
        val categoriesList = listOf(
            CategoryItem("Women", R.drawable.women),
            CategoryItem("Men", R.drawable.men),
            CategoryItem("Kids", R.drawable.kids),
        )
        return categoriesList
    }

    override fun onFavProductClick(product: Products) {
        favoritesHandler.addProductToFavorites(
            product = product,
            onAdded = {
                // Handle success (e.g., update UI)
                Snackbar.make(requireView(),"Added to favorites", 2000).show()
            },
            onAlreadyExists = {
                // Handle already in favorites case
                favoritesHandler.removeFromFavorites(
                    product = product,
                    onRemoved = {
                        // Handle successful removal
                        Snackbar.make(requireView(),"Removed from favorites", 2000).show()
                    }
                )
            }
        )

    }

    override fun onProductClick(id: Long) {
        val action =HomeFragmentDirections.actionNavHomeToProuductnfoFragment(id)
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun getCategoryProducts() {
        lifecycleScope.launch {
            categoriesViewModel.categoryProductStateFlow.collectLatest { result ->
                when (result) {
                    is DataState.Loading -> {}
                    is DataState.OnSuccess<*> -> {
                        val productResponse = result.data as ProductResponse
                        val productsList = productResponse.products
                        getListWithProductPrice(productsList)
                    }
                    is DataState.OnFailed -> {
                        Snackbar.make(requireView(), "Failed to get data", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun getListWithProductPrice(categoryProducts: List<Products>) {
        lifecycleScope.launch {
            searchViewModel.searchStateFlow.collectLatest { result ->
                when (result) {
                    is DataState.Loading -> {
                        saleRecycler.visibility = View.INVISIBLE
                    }
                    is DataState.OnSuccess<*> -> {
                        val productResponse = result.data as ProductResponse
                        val productsList = productResponse.products
                        val filteredProducts = productsList.filter { product ->
                            categoryProducts.any { it.id == product.id }
                        }
                        saleProgressBar.visibility = View.GONE
                        saleRecycler.visibility = View.VISIBLE
                        saleProductsAdapter.submitList(filteredProducts)
                    }
                    is DataState.OnFailed -> {
                        saleProgressBar.visibility = View.GONE
                        Snackbar.make(requireView(), "Failed to get data", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }


    override fun onCategoryClick(categoryName: String) {
       val action = HomeFragmentDirections.actionNavHomeToNavCategories(categoryName)
        Navigation.findNavController(requireView()).navigate(action)
    }

    suspend fun getSpecificDraftOrder(){
        productInfoViewModel.getSpecificDraftOrder(FavoriteDraftOrderId?.toLong()?:0)
        productInfoViewModel.specificDraftOrders.collect{
            when(it){
                DataState.Loading -> {}
                is DataState.OnFailed -> {}
                is DataState.OnSuccess<*> -> {
                    draftOrder = it.data as DraftOrderRequest
                }
            }
        }
    }

}