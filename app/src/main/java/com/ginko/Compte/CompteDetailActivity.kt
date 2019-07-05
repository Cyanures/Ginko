package com.ginko.Compte

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View.OnClickListener
import android.widget.*
import com.ginko.Opérations.TextViewDatePicker
import kotlinx.android.synthetic.main.activity_compte_detail.*
import java.sql.Date
import java.text.SimpleDateFormat


class CompteDetailActivity() : AppCompatActivity(), OnClickListener, View.OnLongClickListener {
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


    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onLongClick(view: View): Boolean {
        //Sur un clic long, proposer de supprimer l'operation (modale --> "êtes-vous sûr de vouloir supprimer l'opération ??"
        OpenDeleteOperation(view.tag as Int)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(view: View) {
        if (view.tag != null) {
            OpenModifOperation(view.tag as Int)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compte_detail)
        database = App.database

        //Récupération de la toolbar
        val toolbar = findViewById<Toolbar>(R.id.ToolbarDetailCompte)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        //Récupération du compte sur lequel on est
        compte = intent.getParcelableExtra<Compte>(EXTRA_COMPTE)

        //Récupération de la liste des opérations
        MaJListeOperations()

        //Récupération du FAB et passage de l'action lors du clic
        val fab = findViewById<FloatingActionButton>(R.id.fabOperation)
        fab.setOnClickListener { OpenCreateOperation() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detailcompte, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_editCompte -> {
                OpenEditCompte()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun OpenEditCompte() {
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Modifier un Compte")

        val view = layoutInflater.inflate(R.layout.activity_compte_create, null)
        val nomCompte = view.findViewById<EditText>(R.id.saisieNomCompte)
        //ne pas afficher le solde pour ne pas pouvoir le modifier
        val soldeCompte = view.findViewById<EditText>(R.id.saisieSoldeCompte)
        soldeCompte.visibility = View.INVISIBLE


        val includedInBalance = view.findViewById<CheckBox>(R.id.saisieIncludedInBalance)
        nomCompte.setText("${compte.nomCompte}")
        soldeCompte.setText("${compte.solde.toString()}")
        includedInBalance.isChecked = compte.includedInBalance == 1

        builder.setView(view)

        //Bouton Modifier
        builder.setPositiveButton("Modifier") { _, _ ->
            //Permet de modifier le compte
            compte.nomCompte = nomCompte.text.toString()
            compte.includedInBalance = if (includedInBalance.isChecked) 1 else 0
            //si le compte est modifié en BDD on répercute le chengement de nom sur la page actuelle
            if(database.ModifierCompte(compte)){
                nomCompteDetail.setText("${compte.nomCompte}")
            }
            //on indique à compteActivity qu'une modification a été effectuée et il faut qu'il mette à jour le solde de la balance
            intent = Intent()
            setResult(REQUEST_MaJ_SOLDE, intent)
        }
        //Bouton Annuler
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()

    }

    private fun MaJListeOperations() {
        operations = database.getOperations(compte.idCompte)
        adapter = OperationAdapter(operations, this, this)
        val recyclerView = findViewById<RecyclerView>(R.id.OperationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        compteDetail = findViewById(R.id.nomCompteDetail)
        compteDetail.text = compte.nomCompte

        soldeDetail = findViewById(R.id.soldeCompteDetail)
        soldeDetail.text = compte.solde.toString()

        //rafraichissement de la couleur du solde si celui-ci change après l'affectation d'une recette ou d'une dépense
        if (soldeDetail.text.toString().toDouble() > 0) {
            soldeDetail.setTextColor(Color.parseColor("#2bbf38"))
            soldeDetail.setText(String.format("%.2f", compte.solde) + " €")

        } else {
            soldeDetail.setTextColor(Color.parseColor("#d62f2f"))
            soldeDetail.setText(String.format("%.2f", compte.solde) + " €")
        }
    }

    //Récupération et mise à jour du solde + affichage d'un toast sur l'opération ajoutée
    fun MaJSolde(operation: Operation, raison: String) {
        if (raison == "Ajouter" || raison == "Modifier") {
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
        } else if (raison == "Supprimer") {
            compte.solde -= operation.montantOperation
            database.AffecterOperation(compte)
        }

        findViewById<TextView>(R.id.soldeCompteDetail).setText(String.format("%.2f", compte.solde) + " €")
        intent = Intent()
        setResult(RESULT_OK, intent)
    }

    //Affichage de la modale de saisie d'une opération
    @RequiresApi(Build.VERSION_CODES.O)
    private fun OpenCreateOperation() {
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Ajouter une Opération")

        val view = layoutInflater.inflate(R.layout.activity_operation_create, null)

        val saisieLibOperation = view.findViewById<EditText>(R.id.saisieLibOperation)
        val saisieMontantOperation = view.findViewById<EditText>(R.id.saisieMontantOperation)
        val saisieDate = view.findViewById<EditText>(R.id.saisieDate)
        TextViewDatePicker(context, saisieDate)

        val depense = view.findViewById<RadioButton>(R.id.checkboxDepense)


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
                if (depense.isChecked) {
                    montantOperationSaisie = (-saisieMontantOperation.text.toString().toDouble()).toString()
                }
                val operation =
                    Operation(libOperationSaisie, montantOperationSaisie.toDouble(), date.time, compte.idCompte)
                saveOperation(operation)
                MaJSolde(operation, "Ajouter")
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

    //Affichage de la modale de modification d'une opération
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun OpenModifOperation(position: Int) {

        val operation = operations[position]
        val ancienneoperation = Operation(
            operation.libelleOperation,
            operation.montantOperation,
            operation.dateOperation,
            operation.idCompte
        )
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Modifier une Opération")

        val view = layoutInflater.inflate(R.layout.activity_operation_create, null)
        val checkboxDepense = view.findViewById<RadioButton>(R.id.checkboxDepense)
        val checkboxRecette = view.findViewById<RadioButton>(R.id.checkboxRecette)
        val saisieLibOperation = view.findViewById<EditText>(R.id.saisieLibOperation)
        saisieLibOperation.setText(operation.libelleOperation)
        if (operation.montantOperation < 0) {
            operation.montantOperation = -operation.montantOperation
            checkboxDepense.isChecked = true
        } else {
            checkboxRecette.isChecked = true
        }
        val saisieMontantOperation = view.findViewById<EditText>(R.id.saisieMontantOperation)
        saisieMontantOperation.setText(operation.montantOperation.toString())

        val saisieDate = view.findViewById<EditText>(R.id.saisieDate)
        saisieDate.setText(SimpleDateFormat("dd-MM-yyyy").format(Date(operation.dateOperation)).toString())
        TextViewDatePicker(context, saisieDate)

        builder.setView(view)

        //Bouton Modifier
        builder.setPositiveButton("Modifier") { _, _ ->
            //Permet de supprimer l'ancien montant afin d'affecter seulement le nouveau montant
            compte.solde += (ancienneoperation.montantOperation * -1)

            val idOperation = operation.idOperation
            val libOperationSaisie = saisieLibOperation.text.toString()
            var montantOperationSaisie = saisieMontantOperation.text.toString()
            val dateOperationSaisie = saisieDate.text.toString()
            val date = SimpleDateFormat("dd-MM-yyyy").parse(dateOperationSaisie)

            //Ajouter le controle sur les dates
            if (libOperationSaisie == "" || montantOperationSaisie == "") {
                Toast.makeText(this, "Impossible de créer une opération sans montant ou libelle", Toast.LENGTH_LONG)
                    .show()
            } else {
                Log.i(
                    "Creation Operation",
                    "Operation cree $libOperationSaisie montant : $montantOperationSaisie sur le compte ${compte.idCompte} date opération : $dateOperationSaisie"
                )
                if (checkboxDepense.isChecked) {
                    montantOperationSaisie = (-saisieMontantOperation.text.toString().toDouble()).toString()
                }
                val nouvelleOperation = Operation(
                    idOperation,
                    libOperationSaisie,
                    montantOperationSaisie.toDouble(),
                    date.time,
                    compte.idCompte
                )
                ModifOperation(nouvelleOperation, position)
                MaJSolde(nouvelleOperation, "Modifier")
                MaJListeOperations()

            }


        }
        //Bouton Annuler
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun ModifOperation(operation: Operation, position: Int) {
        operations[position] = operation
        if (database.ModifierOperation(operation)) {
            Toast.makeText(this, "Opération modifiée avec succès", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Erreur survenue lors de la modification de l'opération", Toast.LENGTH_SHORT).show()
        }
    }


    //Affichage de la modale de suppresion d'une opération
    @RequiresApi(Build.VERSION_CODES.O)
    private fun OpenDeleteOperation(position: Int) {
        val operation = operations[position]
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Supprimer une Opération")
        builder.setMessage(
            "Êtes-vous sûr de vouloir supprimer \n " +
                    "l'operation ${operation.libelleOperation} d'un montant de ${operation.montantOperation} € ?"
        )
        builder.create()

        //Bouton Supprimer
        builder.setPositiveButton("Supprimer") { _, _ ->

            SupprimerOperation(position)
            MaJListeOperations()
        }
        //Bouton Annuler
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }


    private fun SupprimerOperation(position: Int) {
        val operation = operations[position]
        if (database.SupprimerOperation(operation)) {
            MaJSolde(operation, "Supprimer")
            operations.remove(operation)
            Toast.makeText(this, "Opération supprimée avec succès", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Erreur survenue lors de la suppression de l'opération", Toast.LENGTH_SHORT).show()
        }
    }
}

