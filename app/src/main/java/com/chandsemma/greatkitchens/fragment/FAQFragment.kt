package com.chandsemma.greatkitchens.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chandsemma.greatkitchens.R
import com.chandsemma.greatkitchens.util.DrawerLocker

class FAQFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val view = inflater.inflate(R.layout.fragment_faq, container, false)
        (activity as DrawerLocker).setDrawerEnabled(true)
        return view
    }
}
