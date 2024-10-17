package com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.databinding.FragmentShoppingCartBinding


class ShoppingCartFragment : Fragment() {
    private lateinit var binding:FragmentShoppingCartBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentShoppingCartBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnProceedToCheckout.setOnClickListener{
            Navigation.findNavController(view).navigate(ShoppingCartFragmentDirections.actionShoppingCartFragmentToChooseAddressFragment())
        }

    }


}