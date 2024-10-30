package com.iti.itp.bazaar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.Navigation
import com.iti.itp.bazaar.databinding.FragmentSuccessOrderPageBinding


class SuccessOrderPage : Fragment() {
    private lateinit var binding: FragmentSuccessOrderPageBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSuccessOrderPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnContinueShopping.setOnClickListener {
            navigateHome(it)
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner){navigateHome(requireView())}
    }

    private fun navigateHome(view: View) {
        val action = SuccessOrderPageDirections.actionSuccessOrderPageToNavHome()
        Navigation.findNavController(view).navigate(action)
    }

}