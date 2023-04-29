package com.sokoldev.mygarage.home.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sokoldev.mygarage.R
import com.sokoldev.mygarage.vehicle.CarDetailsActivity
import com.sokoldev.mygarage.vehicle.data.Car

class HomeAdapter(val arrayList: ArrayList<Car>) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    lateinit var context: Context

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: AppCompatImageView = itemView.findViewById(R.id.imageCar)
        var name: AppCompatTextView = itemView.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.design_home, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val car = arrayList[position]
        holder.name.text = car.make + " " + car.model
        showImage(car.image, holder.image)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CarDetailsActivity::class.java)
            intent.putExtra("car", arrayList[position])
            context.startActivity(intent)
        }
    }

    private fun showImage(downloadUrl: String?, image: AppCompatImageView) {
        Glide.with(context)
            .load(downloadUrl)
            .into(image)
    }
}