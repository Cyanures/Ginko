package com.ginko.Compte

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import com.ginko.R
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.ginko.App
import com.ginko.Database
import kotlinx.android.synthetic.main.activity_compte_create.*
import kotlinx.android.synthetic.main.activity_main.*

class CompteActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {


    lateinit var adapter: CompteAdapter
    private lateinit var database: Database
    private lateinit var comptes: MutableList<Compte>


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //On récupere notre classe Database
        database = App.database

        //initialisation des comptes de la page d'acceuil
        MaJCompteAccueil()
        MaJBalanceAccueil()

        //Bouton FAB
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { OpenCreateCompte() }

    }
    //on mets à jour la liste des comptes affichés
    private fun MaJCompteAccueil() {
        comptes = database.getComptes()
        adapter = CompteAdapter(comptes, this, this)
        val recyclerView = findViewById<RecyclerView>(R.id.compteRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    //on met à jour la balance de l'accueil
    private fun MaJBalanceAccueil() {
        var comptesBalance = mutableListOf<Compte>()
        comptesBalance = database.getComptesIncludedInBalance()
        var balance: Double = 0.00
        for (compte: Compte in comptesBalance) {
            balance += compte.solde
        }
        Balance.setText(String.format("%.2f", balance) + " €")
    }



    //Clic sur un compte, lance l'activity_compte_detail
    override fun onClick(view: View) {
        if (view.tag != null) {
            AfficherDetailCompte(view.tag as Int)
        }
    }
    //on affiche le détail du compte cliqué
    fun AfficherDetailCompte(compteIndex: Int) {
        val compte = comptes[compteIndex]

        val intent = Intent(this, CompteDetailActivity::class.java)
        //on passe le compte sur lequel on a cliqué en extra
        intent.putExtra(CompteDetailActivity.EXTRA_COMPTE, compte)
        startActivityForResult(intent, CompteDetailActivity.REQUEST_MaJ_SOLDE)
    }


    //Quand on reçoit un resultat de CompteDetailActivity (quand une operation est ajoutée)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CompteDetailActivity.REQUEST_MaJ_SOLDE -> {
                MaJCompteAccueil()
                MaJBalanceAccueil()
            }
        }
    }
    //Ouverture de la modale de création d'un compte
    private fun OpenCreateCompte() {
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Ajouter un compte")

        val view = layoutInflater.inflate(R.layout.activity_compte_create, null)


        val saisieNomCompte = view.findViewById<EditText>(R.id.saisieNomCompte)
        val saisieSoldeCompte = view.findViewById<EditText>(R.id.saisieSoldeCompte)
        val includedInBalance = view.findViewById<CheckBox>(R.id.saisieIncludedInBalance)


        builder.setView(view);

        // boutton ok
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val nomCompteSaisi = saisieNomCompte.text.toString()
            val soldeCompteSaisi = saisieSoldeCompte.text.toString()
            val includedInBalanceSaisie = if (includedInBalance.isChecked) 1 else 0



            if (nomCompteSaisi == "" || soldeCompteSaisi == "") {
                Toast.makeText(this, "Impossible de créer un compte sans solde ou nom", Toast.LENGTH_LONG).show()
            } else {
                saveCompte(Compte(nomCompteSaisi, soldeCompteSaisi.toDouble(), includedInBalanceSaisie))
            }


        }

        //boutton annuler
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun saveCompte(compte: Compte) {
        //si l'ajout en BDD a fonctionné on met à jour les comptes et la balance de l'accueil
        if (database.createCompte(compte)) {
            MaJCompteAccueil()
            MaJBalanceAccueil()
            Toast.makeText(this, "Création du compte effectué avec succès", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Erreur survenue lors de la creation du compte", Toast.LENGTH_SHORT).show()
        }
    }


    //Sur un clic Long, demande si on souhaite supprimer un compte
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onLongClick(view: View): Boolean {
        OpenDeleteCompte(view.tag as Int)
        return true
    }

    //Affichage de la modale de suppresion d'une opération
    @RequiresApi(Build.VERSION_CODES.O)
    private fun OpenDeleteCompte(position: Int) {
        //on recupere le compte sur lequel on as cliqué
        val compte = comptes[position]

        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Supprimer un Compte")
        builder.setMessage(
            "Êtes-vous sûr de vouloir supprimer \n " +
                    "le compte ${compte.nomCompte} ayant un solde de ${compte.solde} € ?"
        )
        builder.create()

        //Bouton Supprimer
        builder.setPositiveButton("Supprimer") { _, _ ->

            SupprimerCompte(position)
            MaJCompteAccueil()
            MaJBalanceAccueil()
        }
        //Bouton Annuler
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun SupprimerCompte(position: Int) {
        val compte = comptes[position]
        //si on a réussi à supprimer toutes les OP du compte et le compte alors on rafraichit les comptes et la balance sur l'accueil
        if (database.SupprimerOperationsCompte(compte) && database.SupprimerCompte(compte)) {
            MaJCompteAccueil()
            MaJBalanceAccueil()
            Toast.makeText(this, "Compte supprimé avec succès", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Erreur survenue lors de la suppression du compte", Toast.LENGTH_SHORT).show()
        }
    }


}


