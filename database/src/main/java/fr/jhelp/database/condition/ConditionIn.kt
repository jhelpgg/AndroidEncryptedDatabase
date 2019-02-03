package fr.jhelp.database.condition

import fr.jhelp.database.Column
import fr.jhelp.database.DATE
import fr.jhelp.database.DOUBLE
import fr.jhelp.database.DatabaseEncrypted
import fr.jhelp.database.FLOAT
import fr.jhelp.database.INTEGER
import fr.jhelp.database.LONG
import fr.jhelp.database.PARCELABLE
import fr.jhelp.database.PRIMARY_KEY
import fr.jhelp.database.TEXT
import fr.jhelp.database.request.Select
import java.util.GregorianCalendar

class ConditionIn(
    private val column: Column,
    private val select: Select,
    private val databaseEncrypted: DatabaseEncrypted) : Condition
{
    private val values = ArrayList<Any?>()

    override fun refresh()
    {
        this.values.clear()
        val type = this.column.type
        val encryptedCursor = this.databaseEncrypted.select(this.select)

        while (encryptedCursor.moveToNext())
        {
            this.values +=
                    when (type)
                    {
                        PRIMARY_KEY      -> encryptedCursor.getPrimaryKey(0)
                        INTEGER          -> encryptedCursor.getInt(0)
                        LONG             -> encryptedCursor.getLong(0)
                        FLOAT            -> encryptedCursor.getFloat(0)
                        DOUBLE           -> encryptedCursor.getDouble(0)
                        DATE             -> encryptedCursor.getDate(0)
                        TEXT             -> encryptedCursor.getText(0)
                        is PARCELABLE<*> -> encryptedCursor.getParcelable(0, type.clazz)
                    }
        }

        encryptedCursor.close()
    }

    override fun valid(values: Map<Column, Any?>): Boolean
    {
        val columnValue = values[this.column]

        return when (this.column.type)
        {
            PRIMARY_KEY      -> (columnValue as Long) in this.values
            INTEGER          -> (columnValue as Int) in this.values
            LONG             -> (columnValue as Long) in this.values
            FLOAT            -> (columnValue as Float) in this.values
            DOUBLE           -> (columnValue as Double) in this.values
            DATE             -> ((columnValue as GregorianCalendar).timeInMillis) in this.values.map { (it as GregorianCalendar).timeInMillis }
            TEXT             -> (columnValue as String) in this.values
            is PARCELABLE<*> -> columnValue in this.values
        }
    }
}

fun Column.IN(select: Select, databaseEncrypted: DatabaseEncrypted): Condition =
    ConditionIn(this, select, databaseEncrypted)