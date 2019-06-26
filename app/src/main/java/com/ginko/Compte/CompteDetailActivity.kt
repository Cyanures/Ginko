package com.ginko.Compte

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.support.annotation.RequiresApi
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.ginko.App
import com.ginko.Opérations.Operation
import com.ginko.Opérations.OperationAdapter
import com.ginko.R
import com.ginko.Database as Database
import com.ginko.Compte.CompteActivity
import android.icu.util.TimeZone
import android.view.View.OnClickListener
import android.widget.*
import com.ginko.Opérations.TextViewDatePicker
import kotlinx.android.synthetic.main.activity_operation_create.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*


class CompteDetailActivity() : AppCompatActivity(), OnClickListener, View.OnLongClickListener {
    override fun onLongClick(v: View?): Boolean {
        //Sur un clic long, proposer de supprimer l'operation (modale --> "êtes-vous sûr de vouloir supprimer l'opération ??"
        Toast.makeText(this,"Long Click on item",Toast.LENGTH_SHORT).show()
        Log.i("LongClick","Long Click on item")
        return true
    }

    override fun onClick(v: View?) {
        //sur un clic simple proposer une page de modification de la recette/depense
        //recupérer la depense en cours et s'il y a changement supprimer l'ancienne pour ajouter la nouvelle
        Toast.makeText(this,"Click on item",Toast.LENGTH_SHORT).show()
        Log.i("Click","Simple Click on item")
    }


    companion object {
        val REQUEST_MaJ_SOLDE = 1
        val EXTRA_COMPTE = "compte"
    }

    private lateinit var compte: Compte
    private lateinit var database: Database
    lateinit var adapter: OperationAdapter
    private lateinit var operations: MutableList<Operation>
    private lateinit var compteDetail: TextView
    private lateinit var soldeDetail: TextView


    constructor(parcel: Parcel) : this() {
        compte = parcel.readParcelable(Compte::class.java.classLoader)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compte_detail)
        database = App.database

        //Récupération du compte sur lequel on est
        compte = intent.getParcelableExtra<Compte>(EXTRA_COMPTE)

        //Récupération de la liste des opérations
        MaJListeOperations()

        //Récupération du FAB et passage de l'action lors du clic
        val fab = findViewById<FloatingActionButton>(R.id.fabOperation)
        fab.setOnClickListener { OpenCreateOperation() }





    }

    private fun MaJListeOperations() {
        operations = database.getOperations(compte.idCompte)
        adapter = OperationAdapter(operations, this,this)
        val recyclerView = findViewById<RecyclerView>(R.id.OperationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        compteDetail = findViewById(R.id.nomCompteDetail)
        compteDetail.text = compte.nomCompte

        soldeDetail = findViewById(R.id.soldeCompteDetail)
        soldeDetail.text = compte.solde.toString()

        //rafraichissement de la couleur du solde si celui-ci change après l'affectation d'une recette ou d'une dépense
       if(soldeDetail.text.toString().toDouble() > 0){
           soldeDetail.setTextColor(Color.parseColor("#2bbf38"))
           soldeDetail.setText("${compte.solde} €")

        }
        else{
            soldeDetail.setTextColor(Color.parseColor("#d62f2f"))
            soldeDetail.setText("${compte.solde} €")
        }
    }


    //Affichage de la modale de saisie d'une opération
    @RequiresApi(Build.VERSION_CODES.O)
    private fun OpenCreateOperation() {
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Ajouter une Opération")

        val view = layoutInflater.inflate(R.layout.activity_operation_create,null)

        val saisieLibOperation = view.findViewById<EditText>(R.id.saisieLibOperation)
        val saisieMontantOperation = view.findViewById<EditText>(R.id.saisieMontantOperation)
        val saisieDate = view.findViewById<EditText>(R.id.saisieDate)
        TextViewDatePicker(context,saisieDate)

        val depense = view.findViewById<RadioButton>(R.id.checkboxDepense)
        val recette = view.findViewById<RadioButton>(R.id.checkboxRecette)




        builder.setView(view)

        //Bouton Ajouter
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val libOperationSaisie = saisieLibOperation.text.toString()
            var montantOperationSaisie = saisieMontantOperation.text.toString()
            val dateOperationSaisie = saisieDate.text.toString()
            val date = SimpleDateFormat("dd-MM-yyyy").parse(dateOperationSaisie)







            if (libOperationSaisie == "" || montantOperationSaisie == "") {
                Toast.makeText(this, "Impossible de créer une opération sans montant ou libelle", Toast.LENGTH_LONG)
                    .show()
            } else {
                Log.i(
                    "Creation Operation",
                    "Operation cree $libOperationSaisie montant : $montantOperationSaisie sur le compte ${compte.idCompte} date opération : $dateOperationSaisie"
                )
                if(depense.isChecked){
                    montantOperationSaisie = (-saisieMontantOperation.text.toString().toDouble()).toString()
                }
                val operation = Operation(libOperationSaisie,montantOperationSaisie.toDouble(),date.time, compte.idCompte)
                saveOperation(operation)
                mAjSolde(operation)
                MaJListeOperations()

            }


        }
        //Bouton Annuler
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }




    //Sauvegarde en base de l'opération
    private fun saveOperation(operation: Operation) {
        if (database.createOperation(operation)) {
            operations.add(operation)
        } else {
            Toast.makeText(this, "Erreur survenue lors de la creation du compte", Toast.LENGTH_SHORT).show()
        }
    }

    //Récupération et mise à jour du solde + affichage d'un toast sur l'opération ajoutée
    fun mAjSolde(operation: Operation) {
        compte.solde += operation.montantOperation
        if (database.AffecterOperation(compte)) {
            if (operation.montantOperation > 0) {
                Toast.makeText(this, "Recette ajoutée avec succès", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Dépense ajoutée avec succès", Toast.LENGTH_SHORT).show()
            }

        } else {
            if (operation.montantOperation > 0) {
                Toast.makeText(this, "Erreur survenue lors de l'ajout de la recette", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erreur survenue lors de l'ajout de la dépense", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<TextView>(com.ginko.R.id.soldeCompteDetail).setText("${compte.solde} €")
        intent = Intent()
        setResult(RESULT_OK, intent)

    }

}

