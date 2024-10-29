package com.iti.itp.bazaar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.iti.itp.bazaar.databinding.FragmentSuccessOrderPageBinding


class SuccessOrderPage : Fragment() {
    private lateinit var binding:FragmentSuccessOrderPageBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSuccessOrderPageBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnContinueShopping.setOnClickListener {
            val navController = Navigation.findNavController(view)
            val action = SuccessOrderPageDirections.actionSuccessOrderPageToNavHome()
            navController.navigate(action) {
                popUpTo(R.id.nav_home) {
                    inclusive = true
                }
            }
        }
    }
}