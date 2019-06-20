package com.ginko.Opérations

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ginko.R

class OperationAdapter(val operations: List<Operation>, val itemClickListener: View.OnClickListener?)
        : RecyclerView.Adapter<OperationAdapter.ViewHolder>() {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val cardViewOperation = itemView.findViewById<CardView>(R.id.cardViewOperation)
            val nomOperation = cardViewOperation.findViewById<TextView>(R.id.nomOperation)
            val montantOperation = cardViewOperation.findViewById<TextView>(R.id.montantOperation)
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val viewItem = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_operation, parent, false)

            return ViewHolder(viewItem)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val operation = operations[position]
            holder.cardViewOperation.setOnClickListener(itemClickListener)
            holder.cardViewOperation.tag = position
            holder.nomOperation.text = operation.libelleOperation
            holder.montantOperation.text = operation.montantOperation.toString() + " €"

        }

        override fun getItemCount(): Int {
            return operations.size
        }

    }
