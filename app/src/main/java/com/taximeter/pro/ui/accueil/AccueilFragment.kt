package com.taximeter.pro.ui.accueil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taximeter.pro.R
import com.taximeter.pro.databinding.FragmentAccueilBinding

class AccueilFragment : Fragment() {

    private var _binding: FragmentAccueilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccueilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStartTrip.setOnClickListener {
            // Navigate directly to compteurFragment using its ID
            findNavController().navigate(R.id.compteurFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}