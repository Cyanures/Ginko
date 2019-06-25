package com.ginko

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.ginko.Compte.Compte
import com.ginko.Opérations.Operation

//DEFINITION DES CONSTANTES DE LA BDD
private const val DATABASE_NAME = "Ginko.db"
private const val DATABASE_VERSION = 1


///////////////////COMPTE////////////////////////
// TABLE COMPTE
private const val COMPTE_TABLE_NAME = "Compte"
private const val COMPTE_KEY_ID = "idCompte"
private const val COMPTE_KEY_Name = "nomCompte"
private const val COMPTE_KEY_Solde = "solde"
private const val COMPTE_KEY_IncludedInBalance = "includedInBalance"

// CREATION DE LA TABLE COMPTE
private const val COMPTE_TABLE_CREATE = """
    CREATE TABLE $COMPTE_TABLE_NAME (
    $COMPTE_KEY_ID INTEGER PRIMARY KEY,
    $COMPTE_KEY_Name TEXT,
    $COMPTE_KEY_Solde DOUBLE,
    $COMPTE_KEY_IncludedInBalance INTEGER
    );
    """
//////////////////FIN COMPTE//////////////////////


///////////////OPERATION////////////////////////
// TABLE OPERATION
private const val OPERATION_TABLE_NAME = "Operation"
private const val OPERATION_KEY_ID = "idOperation"
private const val OPERATION_KEY_Date = "dateOperation"
private const val OPERATION_KEY_Libelle = "libelleOperation"
private const val OPERATION_KEY_Montant = "montantOperation"
private const val OPERATION_KEY_IdCompte = "idCompteOperation"

// CREATION DE LA TABLE OPERATION
private const val OPERATION_TABLE_CREATE = """
    CREATE TABLE $OPERATION_TABLE_NAME (
    $OPERATION_KEY_ID INTEGER PRIMARY KEY,
    $OPERATION_KEY_Libelle TEXT,
    $OPERATION_KEY_Montant LONG,
    $OPERATION_KEY_Date NUMERIC,
    $OPERATION_KEY_IdCompte INTEGER,
    FOREIGN KEY ($OPERATION_KEY_IdCompte) REFERENCES $COMPTE_TABLE_NAME($COMPTE_KEY_ID)
    );
    """
/////////////////FIN OPERATION////////////////////////

//////////////////REQUETES//////////////////////////

//RECUPERATION DE TOUS LES COMPTES
private const val COMPTE_QUERY_SELECT_ALL = "SELECT * FROM $COMPTE_TABLE_NAME"

//RECUPERATION DES COMPTES INCLUS DANS LA BALANCE
private const val COMPTE_QUERY_SELECT_ONLYINCLUDEDINBALANCE = "SELECT * FROM $COMPTE_TABLE_NAME WHERE $COMPTE_KEY_IncludedInBalance = 1"

//RECUPERATION DE TOUTES LES OPERATIONS
private const val OPERATION_QUERY_SELECT_ALL = "SELECT * FROM $OPERATION_TABLE_NAME"

//////////////////FIN REQUETES//////////////////////////


class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(COMPTE_TABLE_CREATE)
        db?.execSQL(OPERATION_TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //Creation d'un compte en BDD
    fun createCompte(compte: Compte): Boolean {
        val values = ContentValues()
        values.put(COMPTE_KEY_Name, compte.nomCompte)
        values.put(COMPTE_KEY_Solde, compte.solde)
        values.put(COMPTE_KEY_IncludedInBalance,compte.includedInBalance)

        val id = writableDatabase.insert(COMPTE_TABLE_NAME, null, values)
        compte.idCompte = id.toInt()

        Log.i("Creation", "Creation du compte ${values} avec l'id ${id}")
        return id > 0

    }

    //Creation d'une opération en BDD
    fun createOperation(operation: Operation): Boolean {
        val values = ContentValues()
        values.put(OPERATION_KEY_Libelle, operation.libelleOperation)
        values.put(OPERATION_KEY_Montant, operation.montantOperation)
        values.put(OPERATION_KEY_Date, operation.dateOperation)
        values.put(OPERATION_KEY_IdCompte, operation.idCompte)


        val id = writableDatabase.insert(OPERATION_TABLE_NAME, null, values)
        operation.idOperation = id.toInt()

        Log.i("Creation", "Creation de l'opération ${values} avec l'id ${id}")
        return id > 0

    }

    fun getComptes(): MutableList<Compte> {
        var comptes = mutableListOf<Compte>()

        readableDatabase.rawQuery(COMPTE_QUERY_SELECT_ALL, null).use { cursor ->
            while (cursor.moveToNext()) {
                val compte = Compte(
                    cursor.getInt(cursor.getColumnIndex(COMPTE_KEY_ID)),
                    cursor.getString(cursor.getColumnIndex(COMPTE_KEY_Name)),
                    cursor.getDouble(cursor.getColumnIndex(COMPTE_KEY_Solde)),
                    cursor.getInt(cursor.getColumnIndex(COMPTE_KEY_IncludedInBalance))
                )

                comptes.add(compte)
            }
        }


        return comptes

    }

    fun getComptesIncludedInBalance(): MutableList<Compte> {
        var comptes = mutableListOf<Compte>()

        readableDatabase.rawQuery(COMPTE_QUERY_SELECT_ONLYINCLUDEDINBALANCE, null).use { cursor ->
            while (cursor.moveToNext()) {
                val compte = Compte(
                    cursor.getInt(cursor.getColumnIndex(COMPTE_KEY_ID)),
                    cursor.getString(cursor.getColumnIndex(COMPTE_KEY_Name)),
                    cursor.getDouble(cursor.getColumnIndex(COMPTE_KEY_Solde)),
                    cursor.getInt(cursor.getColumnIndex(COMPTE_KEY_IncludedInBalance))
                )
                comptes.add(compte)
            }
        }
        return comptes
    }

    fun getOperations(idCompte: Int): MutableList<Operation> {
        var operations = mutableListOf<Operation>()
        readableDatabase.rawQuery(OPERATION_QUERY_SELECT_ALL + " WHERE ${OPERATION_KEY_IdCompte} = $idCompte ORDER BY ${OPERATION_KEY_Date} DESC", null)
            .use { cursor ->
                while (cursor.moveToNext()) {
                    val operation = Operation(
                        cursor.getInt(cursor.getColumnIndex(OPERATION_KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(OPERATION_KEY_Libelle)),
                        cursor.getDouble(cursor.getColumnIndex(OPERATION_KEY_Montant)),
                        cursor.getLong(cursor.getColumnIndex(OPERATION_KEY_Date)),
                        cursor.getInt(cursor.getColumnIndex(OPERATION_KEY_IdCompte))
                    )
                    operations.add(operation)
                }
            }
        return operations
    }

    //Affectation d'une depense ou recette sur un compte
    fun AffecterOperation(compte: Compte): Boolean {
        val values = ContentValues()
        values.put(COMPTE_KEY_Solde, compte.solde)
        val id = writableDatabase.update(
            COMPTE_TABLE_NAME,
            values,
            COMPTE_KEY_ID + "=?",
            arrayOf(compte.idCompte.toString())
        )
        return id > 0
    }


}

