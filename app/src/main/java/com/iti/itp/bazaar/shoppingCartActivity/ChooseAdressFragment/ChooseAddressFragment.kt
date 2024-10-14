package com.iti.itp.bazaar.shoppingCartActivity.ChooseAdressFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.databinding.FragmentChooseAddressBinding

class ChooseAddressFragment : Fragment() {
    private lateinit var binding:FragmentChooseAddressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChooseAddressBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.continueToPayment.setOnClickListener {
            val action = ChooseAddressFragmentDirections.actionChooseAddressFragmentToPaymentMethods()
            Navigation.findNavController(view).navigate(action)
        }
    }
}