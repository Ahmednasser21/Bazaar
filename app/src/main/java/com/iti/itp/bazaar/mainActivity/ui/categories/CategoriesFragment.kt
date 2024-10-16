package com.iti.itp.bazaar.mainActivity.ui.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.databinding.FragmentCategoriesBinding
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "CategoriesFragment"
class CategoriesFragment : Fragment(),OnProductClickListener,OnFavouriteProductClickListener {

    private lateinit var binding :FragmentCategoriesBinding
    private lateinit var categoryGroup: ChipGroup
    private lateinit var menChip:Chip
    private lateinit var womenChip: Chip
    private lateinit var kidChip: Chip
    private lateinit var saleChip:Chip
    private lateinit var categoryProductsRec: RecyclerView
    private lateinit var categoriesViewModel:CategoriesViewModel
    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabRings: FloatingActionButton
    private lateinit var fabTshirt: FloatingActionButton
    private lateinit var fabShoes: FloatingActionButton
    private lateinit var categoryProductsAdapter:CategoryProductsAdapter
    private lateinit var categoriesProg:ProgressBar
    private var isFabOpen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory = CategoriesViewModelFactory(
            Repository.getInstance(
            ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
        ))
        categoriesViewModel = ViewModelProvider(this,factory)[CategoriesViewModel::class.java]
        categoryProductsAdapter = CategoryProductsAdapter(this,this)
        binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initialiseUI()
        categoriesViewModel.getCategoryProducts(480515457328)
        getCategoryProducts()
        fabMain.setOnClickListener { toggleFabMenu() }


    }

    private fun initialiseUI(){
        fabMain = binding.fabMain
        fabShoes = binding.fabShoes
        fabTshirt = binding.fabTshirt
        fabRings = binding.fabAccessories
        categoryGroup = binding.collectionGroup
        womenChip = binding.women
        menChip = binding.men
        kidChip = binding.kid
        saleChip = binding.sale
        categoriesProg = binding.progCategories
        categoryProductsRec = binding.recCategoryProducts.apply {
            adapter = categoryProductsAdapter
            layoutManager = GridLayoutManager(requireContext(),2)
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
        fabRings.show()
        fabTshirt.show()
        fabShoes.show()
        fabMain.animate().rotation(45f)
        fabMain.setImageResource(R.drawable.close)
    }

    private fun closeFabMenu() {
        isFabOpen = false
        fabRings.hide()
        fabTshirt.hide()
        fabShoes.hide()
        fabMain.animate().rotation(0f)
        fabMain.setImageResource(R.drawable.filter)
    }
    private fun getCategoryProducts() {
        lifecycleScope.launch {
            categoriesViewModel.categoryProductStateFlow.collectLatest { result ->
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
                            binding.emptyBoxAnimationFav.visibility = View.VISIBLE
                        }
                        categoryProductsAdapter.submitList(productsList)
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

    override fun onProductClick(id: Long) {
        TODO("Not yet implemented")
    }

    override fun onFavProductClick() {
        TODO("Not yet implemented")
    }

}