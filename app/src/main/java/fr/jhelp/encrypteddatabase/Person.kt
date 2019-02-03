package fr.jhelp.encrypteddatabase

import fr.jhelp.database.EncryptedCursor

data class Person(val name: String, val age: Int)

class CursorPerson(private val encryptedCursor: EncryptedCursor)
{
    val closed get() = this.encryptedCursor.closed

    fun close()
    {
        this.encryptedCursor.close()
    }

    fun nextPerson(): Person?
    {
        if (this.encryptedCursor.closed)
        {
            return null
        }

        if (!this.encryptedCursor.moveToNext())
        {
            this.encryptedCursor.close()
            return null
        }

        val name = this.encryptedCursor.getText(0) ?: ""
        val age = this.encryptedCursor.getInt(1)
        return Person(name, age)
    }
}
