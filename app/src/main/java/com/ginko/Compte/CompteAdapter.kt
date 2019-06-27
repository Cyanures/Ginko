package com.ginko.Compte

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import com.ginko.R
import kotlinx.android.synthetic.main.activity_main.view.*

class CompteAdapter(val comptes: List<Compte>, val itemClickListener: View.OnClickListener, val itemLongClickListener: View.OnLongClickListener) :
    RecyclerView.Adapter<CompteAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView = itemView.findViewById<CardView>(R.id.cardView)
        val nomCompte = cardView.findViewById<TextView>(R.id.nomCompte)
        val soldeCompte = cardView.findViewById<TextView>(R.id.soldeCompte)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compte, parent, false)
        viewItem.findViewById<TextView>(R.id.soldeCompteDetail)

        return ViewHolder(viewItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val compte = comptes[position]
        holder.cardView.setOnClickListener(itemClickListener)
        holder.cardView.setOnLongClickListener(itemLongClickListener)
        holder.cardView.tag = position
        holder.nomCompte.text = compte.nomCompte
        holder.soldeCompte.setText(String.format("%.2f", compte.solde) +" €")

    }

    override fun getItemCount(): Int {
        return comptes.size
    }

}