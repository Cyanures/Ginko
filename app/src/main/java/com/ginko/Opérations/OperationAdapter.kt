package com.ginko.Opérations

import android.graphics.Color
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import com.ginko.R
import java.sql.Date
import java.text.SimpleDateFormat

class OperationAdapter(val operations: List<Operation>, val itemLongClickListener: View.OnLongClickListener?, val itemClickListener: View.OnClickListener?) :
    RecyclerView.Adapter<OperationAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val cardViewOperation = itemView.findViewById<CardView>(R.id.cardViewOperation)
        val nomOperation = cardViewOperation.findViewById<TextView>(R.id.nomOperation)
        val montantOperation = cardViewOperation.findViewById<TextView>(R.id.montantOperation)
        val dateOperation = cardViewOperation.findViewById<TextView>(R.id.dateOperation)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewItem = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_operation, parent, false)

        return ViewHolder(viewItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val operation = operations[position]
        holder.cardViewOperation.setOnLongClickListener(itemLongClickListener)
        holder.cardViewOperation.setOnClickListener(itemClickListener )
        holder.cardViewOperation.tag = position

        holder.nomOperation.text = operation.libelleOperation
        holder.montantOperation.text = operation.montantOperation.toString()
        holder.dateOperation.text = SimpleDateFormat("dd-MM-yyyy").format(Date(operation.dateOperation)).toString()
        //Mise en forme d'une opération (vert si montant positif et rouge si montant négatif)
        if (holder.montantOperation.text.toString().toDouble() > 0) {
            holder.montantOperation.setTextColor(Color.parseColor("#2bbf38"))
            holder.montantOperation.setText(String.format("%.2f", operation.montantOperation) +" €")
        }
        else {
            holder.montantOperation.setTextColor(Color.parseColor("#d62f2f"))
            holder.montantOperation.setText("${operation.montantOperation} €")
        }


    }

    override fun getItemCount(): Int {
        return operations.size
    }

}
