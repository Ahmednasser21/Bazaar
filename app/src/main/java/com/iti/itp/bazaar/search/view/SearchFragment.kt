package com.iti.itp.bazaar.search.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.databinding.FragmentProuductnfoBinding
import com.iti.itp.bazaar.databinding.FragmentSearchBinding
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.productInfo.OnClickListner
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.search.OnCardClickListner
import com.iti.itp.bazaar.search.OnSearchProductFavClick
import com.iti.itp.bazaar.search.viewModel.SearchViewModel
import com.iti.itp.bazaar.search.viewModel.SearchViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchFragment : Fragment() ,OnCardClickListner , OnSearchProductFavClick{

lateinit var binding : FragmentSearchBinding
lateinit var vmFactory : SearchViewModelFactory
lateinit var searchViewModel: SearchViewModel
lateinit var searshAdapter: SearchAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        vmFactory = SearchViewModelFactory(Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)))
        searchViewModel = ViewModelProvider(this ,vmFactory).get(SearchViewModel::class.java)
        searshAdapter= SearchAdapter(this,this)
        binding = FragmentSearchBinding.inflate(inflater,container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSearchProuducts.apply {
            adapter = searshAdapter
            layoutManager = GridLayoutManager(requireContext() , 2)
        }
        searchViewModel.getAllProducts()
        lifecycleScope.launch {
            searchViewModel.searchStateFlow.collectLatest { result->
                when(result){
                    DataState.Loading -> {}
                    is DataState.OnFailed -> {}
                    is DataState.OnSuccess<*> -> {
                        val allProuducts = result.data as ProductResponse
                     //   searshAdapter.submitList(allProuducts.products)
                        binding.svSearchbar .setOnQueryTextListener(object :
                            SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                lifecycleScope.launch {
                                    if (newText.isNullOrEmpty()) {
                                        searshAdapter.submitList(emptyList())
                                    } else {
                                        val filteredList = withContext(Dispatchers.Default) {
                                            allProuducts.products.filter { item ->
                                                item.title.contains(newText, ignoreCase = true)
                                            }
                                        }
                                        searshAdapter.submitList(filteredList)
                                    }
                                }
                                return true
                            }
                        })

                    }
                }

            }
        }
    }


    override fun onCardClick(prduct: Products) {
       val action = SearchFragmentDirections.actionSearchFragmentToProuductnfoFragment(prduct.id)
        Navigation.findNavController(binding.root).navigate(action)
    }

    override fun onFavClick(prduct: Products) {

    }


}