package com.ginko

import android.app.Application
import android.provider.ContactsContract

/*
 * On va instancier la BDD ici car la BDD peut être utilisée par  plusieurs activity ou plusieurs fragment(cela évite de la recréer à chaque fois dans chacun de ces contextes)
  * de plus la BDD est plutôt liée au cycle de vie de l'application et pas au cycle de vie d'une activity
  *
  * Comme ça chaque activity/fragment pourra récupérer l'Application et travailler avec la BDD de l'application
*/

class App : Application() {
    // le fait de le mettre en companion object on pourra faire un App.instance directement
    companion object{
        lateinit var instance: App
        //de ce fait, je vais pouvoir utiliser dans mes activity App.database et directement accéder à ma base de donnée
        val database: Database by lazy {
            Database(instance)
        }
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}