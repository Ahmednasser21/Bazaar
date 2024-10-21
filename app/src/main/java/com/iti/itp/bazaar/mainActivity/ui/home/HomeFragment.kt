package com.iti.itp.bazaar.mainActivity.ui.home

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.databinding.FragmentHomeBinding
import com.iti.itp.bazaar.dto.PriceRuleDto
import com.iti.itp.bazaar.mainActivity.MainActivity
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.responses.PriceRulesResponse
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.responses.SmartCollectionsResponse
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "HomeFragment"

class HomeFragment : Fragment(), OnBrandClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var brandsAdapter: BrandsAdapter
    private lateinit var brandsRecycler: RecyclerView
    private lateinit var brandsProgressBar: ProgressBar
    private lateinit var list: List<PriceRuleDto>

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
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        brandsAdapter = BrandsAdapter(this)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitAlertDialog()
            }
        })
        binding.imageSlider.setImageList(getListOfImageAds())
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        homeViewModel.getPriceRules()


        lifecycleScope.launch {
            homeViewModel.priceRules.collect { state ->
                when (state) {
                    is DataState.Loading -> ""
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
                        val clip = ClipData.newPlainText("ad", list.get(position).title)
                        clipboard.setPrimaryClip(clip)
                        Snackbar.make(view, "Coupon is copied", 2000).show()
                    }

                    1 -> {
                        val clip = ClipData.newPlainText("ad", list.get(position).title)
                        clipboard.setPrimaryClip(clip)
                        Snackbar.make(view, "Coupon is copied", 2000).show()
                    }

                    2 -> {
                        val clip = ClipData.newPlainText("ad", list.get(position).title)
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

        brandsRecycler = binding.recBrands.apply {
            adapter = brandsAdapter
            layoutManager = GridLayoutManager(requireContext(), 2, HORIZONTAL, false)
        }
        brandsProgressBar = binding.progBrands
        homeViewModel.getVendors()
        getProductVendors()
        }

    private fun getProductVendors() {
        lifecycleScope.launch {
            homeViewModel.brandStateFlow.collectLatest { result ->

                when (result) {

                    is DataState.Loading -> {
                        brandsProgressBar.visibility = View.VISIBLE
                        brandsRecycler.visibility = View.INVISIBLE
                    }

                    is DataState.OnSuccess<*> -> {
                        brandsProgressBar.visibility = View.GONE
                        brandsRecycler.visibility = View.VISIBLE
                        val smartCollectionsResponse = result.data as SmartCollectionsResponse
                        val brandList = smartCollectionsResponse.smartCollections
                        Log.i(TAG, "getProductVendors: $brandList ")
                        brandsAdapter.submitList(brandList)

                    }

                    is DataState.OnFailed -> {
                        brandsProgressBar.visibility = View.GONE
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
                "https://t4.ftcdn.net/jpg/04/65/12/75/360_F_465127589_BfwtgftgEboy01GSVVQZP5hC9XJGXTO1.jpg",
                "",
                ScaleTypes.FIT
            ),
            SlideModel(
                "https://png.pngtree.com/png-vector/20220527/ourmid/pngtree-coupon-design-isolated-on-white-background-png-image_4759153.png",
                "",
                ScaleTypes.FIT
            ),
            SlideModel(
                "https://ajaxparkingrus.com/wp-content/uploads/2016/10/coupon.jpg",
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

}