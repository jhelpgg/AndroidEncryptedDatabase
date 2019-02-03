package fr.jhelp.encrypteddatabase

import android.app.Activity
import android.os.Bundle
import android.util.Log
import fr.jhelp.utilities.parallel

const val TAG = "EncryptedDB"

class MainActivity : Activity()
{
    private lateinit var database: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ({
            try
            {
                Log.d(TAG, "START")
                this.database = DatabaseManager(this)
                Log.d(TAG, "Database created")
                this.database.addPerson(Person("John Doe", 73))
                Log.d(TAG, "Person 1 added")
                this.database.addPerson(Person("Baby d'Jo", 1))
                Log.d(TAG, "Person 2 added")
                this.database.addPerson(Person("Arthur Dent", 42))
                Log.d(TAG, "Person 3 added")
                this.database.addPerson(Person("Baby d'Jo twin", 1))
                Log.d(TAG, "Person 4 added")

                Log.d(TAG, "Will get all")
                var cursorPerson = this.database.getAllPerson()
                Log.d(TAG, "All get")
                var person = cursorPerson.nextPerson()

                while (person != null)
                {
                    Log.d(TAG, "person = $person")
                    person = cursorPerson.nextPerson()
                }

                Log.d(TAG, "Will get age 1")
                cursorPerson = this.database.getPerson(1)
                Log.d(TAG, "With age 1")
                person = cursorPerson.nextPerson()

                while (person != null)
                {
                    Log.d(TAG, "person = $person")
                    person = cursorPerson.nextPerson()
                }

                Log.d(TAG, "Babies get older")
                this.database.changeAge(1, 2)
                Log.d(TAG, "Will get all")
                cursorPerson = this.database.getAllPerson()
                Log.d(TAG, "All get")
                person = cursorPerson.nextPerson()

                while (person != null)
                {
                    Log.d(TAG, "person = $person")
                    person = cursorPerson.nextPerson()
                }

                Log.d(TAG, "Remove older 40")
                this.database.deletePersonOlderThan(40)
                Log.d(TAG, "Will get all")
                cursorPerson = this.database.getAllPerson()
                Log.d(TAG, "All get")
                person = cursorPerson.nextPerson()

                while (person != null)
                {
                    Log.d(TAG, "person = $person")
                    person = cursorPerson.nextPerson()
                }

                Log.d(TAG, "END")
            }
            catch (throwable: Throwable)
            {
                Log.e(TAG, "Issue !", throwable)
            }
        }).parallel
    }

    override fun onDestroy()
    {
        this.database.close()
        super.onDestroy()
    }
}
