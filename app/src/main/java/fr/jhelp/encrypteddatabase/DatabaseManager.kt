package fr.jhelp.encrypteddatabase

import android.content.Context
import fr.jhelp.database.Column
import fr.jhelp.database.DatabaseEncrypted
import fr.jhelp.database.EncryptedContent
import fr.jhelp.database.INTEGER
import fr.jhelp.database.PRIMARY_KEY
import fr.jhelp.database.TEXT
import fr.jhelp.database.Table
import fr.jhelp.database.condition.EQUALS
import fr.jhelp.database.request.Select

val columnID = Column("ID", PRIMARY_KEY)
val columnName = Column("Name", TEXT)
val columnAge = Column("Age", INTEGER)
val tablePerson by lazy {
    val table = Table("Person")
    table += columnID
    table += columnName
    table += columnAge
    table
}

class DatabaseManager(context: Context)
{
    private val database = DatabaseEncrypted(context, "JHelp")

    init
    {
        this.database.createTable(tablePerson)
    }

    fun close()
    {
        this.database.close()
    }

    fun addPerson(person: Person)
    {
        val encryptedContent = EncryptedContent(tablePerson)
        encryptedContent[columnName] = person.name
        encryptedContent[columnAge] = person.age
        this.database.insert(encryptedContent)
    }

    fun getAllPerson(): CursorPerson
    {
        val encryptedCursor = this.database.select(Select(tablePerson, arrayOf(columnName, columnAge)))
        return CursorPerson(encryptedCursor)
    }

    fun getPerson(age: Int): CursorPerson
    {
        val select = Select(tablePerson, arrayOf(columnName, columnAge)) WHERE (columnAge EQUALS 1)
        return CursorPerson(this.database.select(select))
    }
}