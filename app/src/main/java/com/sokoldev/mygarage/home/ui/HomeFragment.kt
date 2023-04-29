package com.sokoldev.mygarage.home.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sokoldev.mygarage.databinding.FragmentHomeBinding
import com.sokoldev.mygarage.vehicle.data.Car

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        getCarList()

    }

    private fun getCarList() {

        binding.progressBar.visibility = View.VISIBLE

        var db = FirebaseFirestore.getInstance()
        var productsRef = db.collection("cars")


        val query: Query =
            productsRef.whereEqualTo(
                "owner",
                FirebaseAuth.getInstance().currentUser?.uid.toString()
            )
        query.addSnapshotListener(EventListener { snapshot, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                binding.progressBar.visibility = View.GONE
                return@EventListener
            }
            val carList: ArrayList<Car> = ArrayList()
            for (document in snapshot!!.documents) {
                val car = document.toObject(Car::class.java)
                if (car != null) {
                    carList.add(car)
                }
            }
            showData(carList)
        })
    }

    private fun showData(list: ArrayList<Car>) {
        binding.progressBar.visibility = View.GONE
        val adapter = HomeAdapter(list)
        binding.rvHome.adapter = adapter
    }
}