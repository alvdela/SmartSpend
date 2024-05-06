package com.alvdela.smartspend.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.alvdela.smartspend.R

class CustomSpinnerAdapter(context: Context, items: List<String>) :
    ArrayAdapter<String>(context, R.layout.item_spinner_layout, items) {

    /*override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_spinner_layout, parent, false)

        val textView: TextView = view.findViewById(android.R.id.text1)
        textView.text = getItem(position)

        return view
    }*/

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_spinner_layout, parent, false)

        val textView: TextView = view.findViewById(android.R.id.text1)
        textView.text = getItem(position)

        return view
    }
}
