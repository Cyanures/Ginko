package com.ginko.Compte

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.ginko.App
import com.ginko.Opérations.Operation
import com.ginko.Opérations.OperationAdapter
import com.ginko.R
import com.ginko.Database as Database
import com.ginko.Compte.CompteActivity

class CompteDetailActivity() : AppCompatActivity() {


    companion object {
        val REQUEST_MaJ_SOLDE = 1
        val EXTRA_COMPTE = "compte"
    }

    private lateinit var compte: Compte
    private lateinit var database: Database
    lateinit var adapter: OperationAdapter
    private lateinit var operations: MutableList<Operation>

    constructor(parcel: Parcel) : this() {
        compte = parcel.readParcelable(Compte::class.java.classLoader)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compte_detail)
        database = App.database

        //Récupération du compte sur lequel on est
        compte = intent.getParcelableExtra<Compte>(EXTRA_COMPTE)


        //recycle view
        operations = database.getOperations(compte.idCompte)
        adapter = OperationAdapter(operations, null)
        val recyclerView = findViewById<RecyclerView>(R.id.OperationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        //


        //Récupération du FAB et passage de l'action lors du clic
        val fab = findViewById<FloatingActionButton>(R.id.fabOperation)
        fab.setOnClickListener { OpenCreateOperation() }

        //Affichage du compte et du solde sur lequel on est
        findViewById<TextView>(R.id.nomCompteDetail).text = compte.nomCompte
        findViewById<TextView>(R.id.soldeCompteDetail).text = compte.solde.toString() + " €"


    }


    //Affichage de la modale de saisie d'une opération
    private fun OpenCreateOperation() {
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Ajouter un compte")

        val view = layoutInflater.inflate(R.layout.activity_operation_create, null)

        val saisieLibOperation = view.findViewById<EditText>(R.id.saisieLibOperation)
        val saisieMontantOperation = view.findViewById<EditText>(R.id.saisieMontantOperation)


        builder.setView(view);

        // set up the ok button
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val libOperationSaisie = saisieLibOperation.text.toString()
            val montantOperationSaisie = saisieMontantOperation.text.toString()

            if (libOperationSaisie == "" || montantOperationSaisie == "") {
                Toast.makeText(this, "Impossible de créer une opération sans montant ou libelle", Toast.LENGTH_LONG)
                    .show()
            } else {
                Log.i(
                    "Creation Operation",
                    "Operation cree $libOperationSaisie montant : $montantOperationSaisie sur le compte ${compte.idCompte}"
                )
                var operation = Operation(libOperationSaisie, montantOperationSaisie.toDouble(), compte.idCompte)
                saveOperation(operation)
                mAjSolde(operation)

            }


        }
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

    //Récupération et mise à jour du solde
    fun mAjSolde(operation: Operation) {
        compte.solde += operation.montantOperation
        if (database.AffecterOperation(compte)) {
            if (operation.montantOperation > 0) {
                Toast.makeText(this, "Recette ajoutée avec succès", Toast.LENGTH_SHORT)
            } else {
                Toast.makeText(this, "Dépense ajoutée avec succès", Toast.LENGTH_SHORT)
            }

        } else {
            if (operation.montantOperation > 0) {
                Toast.makeText(this, "Erreur survenue lors de l'ajout de la recette", Toast.LENGTH_SHORT)
            } else {
                Toast.makeText(this, "Erreur survenue lors de l'ajout de la dépense", Toast.LENGTH_SHORT)
            }
        }

        findViewById<TextView>(R.id.soldeCompteDetail).text = compte.solde.toString() + " €"
        intent = Intent()
        setResult(RESULT_OK, intent)

    }

}

