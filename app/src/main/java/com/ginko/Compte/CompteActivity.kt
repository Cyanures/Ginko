package com.ginko.Compte

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ginko.R
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.ginko.App
import com.ginko.Database

class CompteActivity : AppCompatActivity(), View.OnClickListener {

    //lateinit var comptes: MutableList<Compte>
    lateinit var adapter: CompteAdapter
    private lateinit var database: Database
    private lateinit var comptes: MutableList<Compte>


    override fun onCreate(savedInstanceState: Bundle?) {
        database = App.database
        //initialisation des comptes de la page d'acceuil

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MaJCompteAccueil()

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { OpenCreateCompte() }

    }

    override fun onClick(view: View) {
        if (view.tag != null) {
            AfficherDetailCompte(view.tag as Int)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        when (requestCode) {
            CompteDetailActivity.REQUEST_MaJ_SOLDE -> {
                MaJCompteAccueil()
            }
        }
    }

    private fun MaJCompteAccueil() {
        comptes = database.getComptes()
        adapter = CompteAdapter(comptes, this)
        val recyclerView = findViewById<RecyclerView>(R.id.compteRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    fun AfficherDetailCompte(compteIndex: Int) {
        val compte = comptes[compteIndex]

        val intent = Intent(this, CompteDetailActivity::class.java)
        intent.putExtra(CompteDetailActivity.EXTRA_COMPTE, compte)
        //intent.putExtra(CompteDetailActivity.EXTRA_COMPTE_INDEX, compteIndex)

        startActivityForResult(
            intent,
            CompteDetailActivity.REQUEST_MaJ_SOLDE
        ) //A la place, mettre un startActivityForResult afin d'être notifié du changement sur le solde
    }

    private fun OpenCreateCompte() {
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Ajouter un compte")

        val view = layoutInflater.inflate(R.layout.activity_compte_create, null)

        val saisieNomCompte = view.findViewById<EditText>(R.id.saisieNomCompte)
        val saisieSoldeCompte = view.findViewById<EditText>(R.id.saisieSoldeCompte)


        builder.setView(view);

        // set up the ok button
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val nomCompteSaisi = saisieNomCompte.text.toString()
            val soldeCompteSaisi = saisieSoldeCompte.text.toString()

            if (nomCompteSaisi == "" || soldeCompteSaisi == "") {
                Toast.makeText(this, "Impossible de créer un compte sans solde ou nom", Toast.LENGTH_LONG).show()
            } else {
                saveCompte(Compte(nomCompteSaisi, soldeCompteSaisi.toDouble()))
            }


        }


        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }


    private fun saveCompte(compte: Compte) {
        if (database.createCompte(compte)) {
            comptes.add(compte)
        } else {
            Toast.makeText(this, "Erreur survenue lors de la creation du compte", Toast.LENGTH_SHORT).show()
        }
    }


}


