package com.iti.itp.bazaar.mainActivity.ui.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.databinding.FragmentCategoriesBinding
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.search.viewModel.SearchViewModel
import com.iti.itp.bazaar.search.viewModel.SearchViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "CategoriesFragment"

class CategoriesFragment : Fragment(), OnProductClickListener, OnFavouriteProductClickListener {

    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var categoryGroup: ChipGroup
    private lateinit var menChip: Chip
    private lateinit var womenChip: Chip
    private lateinit var kidChip: Chip
    private lateinit var saleChip: Chip
    private lateinit var categoryProductsRec: RecyclerView
    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabAccessories: FloatingActionButton
    private lateinit var fabTShirt: FloatingActionButton
    private lateinit var fabShoes: FloatingActionButton
    private lateinit var categoryProductsAdapter: CategoryProductsAdapter
    private lateinit var categoriesProg: ProgressBar
    private lateinit var products: List<Products>
    private var isFabOpen = false
    private var categoryID = 480514900272
    private var previousChip: Chip? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        categoryProductsAdapter = CategoryProductsAdapter(this, this)

        binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initialiseUI()
        categoriesViewModel.getCategoryProducts(categoryID)
        searchViewModel.getAllProducts()
        getCategoryProducts()
        fabMain.setOnClickListener { toggleFabMenu() }

        menChip.setOnClickListener {

            animateChip(menChip, 480515424560)
        }
        womenChip.setOnClickListener {
            animateChip(womenChip, 480515457328)
        }
        kidChip.setOnClickListener {
            animateChip(kidChip, 480515490096)
        }

        saleChip.setOnClickListener {
            animateChip(saleChip, 480515522864)
        }

        fabAccessories.setOnClickListener {
            val filteredProducts = products.filter { it.productType == "ACCESSORIES" }
            if (filteredProducts.isEmpty()) {
                setAnimationVisible()
            } else {
                setAnimationInvisible()
            }
            categoryProductsAdapter.submitList(filteredProducts)
        }
        fabShoes.setOnClickListener {
            val filteredProducts = products.filter { it.productType == "SHOES" }
            if (filteredProducts.isEmpty()) {
                setAnimationVisible()
            } else {
                setAnimationInvisible()
            }
            categoryProductsAdapter.submitList(filteredProducts)
        }

        fabTShirt.setOnClickListener {
            val filteredProducts = products.filter { it.productType == "T-SHIRTS" }
            if (filteredProducts.isEmpty()) {
                setAnimationVisible()
            } else {
                setAnimationInvisible()
            }
            categoryProductsAdapter.submitList(filteredProducts)
        }


    }

    private fun initialiseUI() {
        fabMain = binding.fabMain
        fabShoes = binding.fabShoes
        fabTShirt = binding.fabTshirt
        fabAccessories = binding.fabAccessories
        categoryGroup = binding.collectionGroup
        womenChip = binding.women
        menChip = binding.men
        kidChip = binding.kid
        saleChip = binding.sale
        categoriesProg = binding.progCategories
        categoryProductsRec = binding.recCategoryProducts.apply {
            adapter = categoryProductsAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun toggleFabMenu() {
        if (isFabOpen) {
            closeFabMenu()
        } else {
            openFabMenu()
        }
    }

    private fun openFabMenu() {
        isFabOpen = true
        fabAccessories.show()
        fabTShirt.show()
        fabShoes.show()
        fabMain.animate().rotation(45f)
        fabMain.setImageResource(R.drawable.close)
    }

    private fun closeFabMenu() {
        isFabOpen = false
        fabAccessories.hide()
        fabTShirt.hide()
        fabShoes.hide()
        fabMain.animate().rotation(0f)
        fabMain.setImageResource(R.drawable.filter)
        categoryProductsAdapter.submitList(products)
        if (products.isNotEmpty()) {
            setAnimationInvisible()
        }

    }

    private fun animateChip(chip: Chip, categoryId: Long) {
        if (chip == previousChip && categoryID != 480514900272) {
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            chip.textSize = 16f
            categoryID = 480514900272
            categoriesViewModel.getCategoryProducts(categoryID)
            chip.clearAnimation()
        } else {
            previousChip?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            previousChip?.textSize = 16f
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.chip_anim)
            chip.startAnimation(animation)
            chip.textSize = 18f
            previousChip = chip
            categoryID = categoryId
            categoriesViewModel.getCategoryProducts(categoryId)
        }
    }

    private fun getCategoryProducts() {
        lifecycleScope.launch {
            categoriesViewModel.categoryProductStateFlow.collectLatest { result ->
                when (result) {
                    is DataState.Loading -> {}

                    is DataState.OnSuccess<*> -> {
                        categoriesProg.visibility = View.GONE
                        categoryProductsRec.visibility = View.VISIBLE
                        val productResponse = result.data as ProductResponse
                        val productsList = productResponse.products
                        Log.i(TAG, "getCategoryProducts:${productsList}")
                        if (productsList.isEmpty()) {
                            setAnimationVisible()
                        } else {
                            setAnimationInvisible()
                        }
                        if(categoryID!=480514900272){
                        categoryProductsAdapter.submitList(getListWithProductPrice(productsList))
                        }
                        else{
                            getListWithProductPrice(productsList)
                        }

                    }

                    is DataState.OnFailed -> {
                        categoriesProg.visibility = View.GONE
                        Snackbar.make(requireView(), "Failed to get data", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun getListWithProductPrice(categoryProducts: List<Products>): List<Products> {
        var filteredProducts: List<Products> = listOf()
        lifecycleScope.launch {
            searchViewModel.searchStateFlow.collectLatest { result ->
                when (result) {
                    is DataState.Loading -> {
                        categoriesProg.visibility = View.VISIBLE
                        categoryProductsRec.visibility = View.INVISIBLE
                    }

                    is DataState.OnSuccess<*> -> {
                        categoriesProg.visibility = View.GONE
                        categoryProductsRec.visibility = View.VISIBLE
                        val productResponse = result.data as ProductResponse
                        val productsList = productResponse.products
                        Log.i(TAG, "getCategoryProducts:${productsList}")
                        if (productsList.isEmpty()) {
                            setAnimationVisible()
                        } else {
                            setAnimationInvisible()
                        }
                        filteredProducts = productsList.filter { product ->
                            categoryProducts.any { it.id == product.id }
                        }
                        if(categoryID==480514900272){
                            val filteredList =productsList.filter { !it.image?.src.isNullOrBlank() }
                            products = filteredList
                            categoryProductsAdapter.submitList(filteredList)
                        }
                        else{
                            products = filteredProducts
                        }

                    }

                    is DataState.OnFailed -> {
                        categoriesProg.visibility = View.GONE
                        Snackbar.make(requireView(), "Failed to get data", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
        return filteredProducts
    }

    private fun setAnimationVisible() {
        binding.emptyBoxAnimation.visibility = View.VISIBLE
        categoryProductsRec.visibility = View.INVISIBLE
    }

    private fun setAnimationInvisible() {
        binding.emptyBoxAnimation.visibility = View.INVISIBLE
        categoryProductsRec.visibility = View.VISIBLE
    }

    override fun onProductClick(id: Long) {
        val action = CategoriesFragmentDirections.actionNavCategoriesToProuductnfoFragment(id)
        Navigation.findNavController(binding.root).navigate(action)
    }

    override fun onFavProductClick() {

    }

}