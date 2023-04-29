package com.sokoldev.mygarage.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.sokoldev.mygarage.auth.RegisterActivity
import com.sokoldev.mygarage.databinding.FragmentSettingBinding
import com.sokoldev.mygarage.vehicle.AddCarActivity


class SettingFragment : Fragment() {
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"
    private lateinit var binding: FragmentSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences =
            context?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)


// Set an OnCheckedChangeListener for the switch
        binding.switchTheme.setOnCheckedChangeListener { buttonView, isChecked -> // Set the night mode based on the switch's checked state
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                sharedPreferences?.edit {
                    putBoolean("isNight", true)
                }
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPreferences?.edit {
                    putBoolean("isNight", false)
                }

            }

        }

        binding.switchKm.setOnCheckedChangeListener { buttonView, isChecked -> // Set the night mode based on the switch's checked state
            if (isChecked) {
                sharedPreferences?.edit {
                    putBoolean("isKm", true)
                }
            } else {
                sharedPreferences?.edit {
                    putBoolean("isKm", false)
                }

            }
        }

        val isNight = sharedPreferences?.getBoolean("isNight", false)
        binding.switchTheme.isChecked = isNight == true

        val isKm = sharedPreferences?.getBoolean("isKm", false)
        if (isKm == true) {
            binding.switchKm.isChecked = true
        }

        binding.buttonAddCar.setOnClickListener {
            startActivity(Intent(requireContext(), AddCarActivity::class.java))
        }

        binding.buttonLogout.setOnClickListener {

            context?.deleteSharedPreferences("MyPrefs")
            val mAuth = FirebaseAuth.getInstance()
            mAuth.signOut()
            startActivity(Intent(context, RegisterActivity::class.java))
            activity?.finish()
        }

    }

}
