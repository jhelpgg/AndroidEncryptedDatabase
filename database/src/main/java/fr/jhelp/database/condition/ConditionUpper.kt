package fr.jhelp.database.condition

import android.os.Parcelable
import fr.jhelp.database.Column
import fr.jhelp.database.DATE
import fr.jhelp.database.DOUBLE
import fr.jhelp.database.FLOAT
import fr.jhelp.database.INTEGER
import fr.jhelp.database.LONG
import fr.jhelp.database.PARCELABLE
import fr.jhelp.database.PRIMARY_KEY
import fr.jhelp.database.TEXT
import java.util.GregorianCalendar

class ConditionUpper<C : Comparable<C>> internal constructor(private val column: Column, private val maximum: C) :
    Condition
{
    override fun valid(values: Map<Column, Any?>): Boolean
    {
        val columnValue = values[this.column]

        return when (this.column.type)
        {
            PRIMARY_KEY      -> (columnValue as Long) > (this.maximum as Long)
            INTEGER          -> (columnValue as Int) > (this.maximum as Int)
            LONG             -> (columnValue as Long) > (this.maximum as Long)
            FLOAT            -> (columnValue as Float) > (this.maximum as Float)
            DOUBLE           -> (columnValue as Double) > (this.maximum as Double)
            DATE             -> (columnValue as GregorianCalendar).timeInMillis > (this.maximum as GregorianCalendar).timeInMillis
            TEXT             -> (columnValue as String) > (this.maximum as String)
            is PARCELABLE<*> -> (columnValue as C) > this.maximum
        }
    }
}

infix fun Column.UPPER_PRIMARY(id: Long): Condition
{
    if (this.type != PRIMARY_KEY)
    {
        throw IllegalArgumentException("$this not a primary key")
    }

    return ConditionUpper(this, id)
}

infix fun Column.UPPER(value: Int): Condition
{
    if (this.type != INTEGER)
    {
        throw IllegalArgumentException("$this not a integer")
    }

    return ConditionUpper(this, value)
}

infix fun Column.UPPER(value: Long): Condition
{
    if (this.type != LONG)
    {
        throw IllegalArgumentException("$this not a long")
    }

    return ConditionUpper(this, value)
}

infix fun Column.UPPER(value: Float): Condition
{
    if (this.type != FLOAT)
    {
        throw IllegalArgumentException("$this not a float")
    }

    return ConditionUpper(this, value)
}

infix fun Column.UPPER(value: Double): Condition
{
    if (this.type != DOUBLE)
    {
        throw IllegalArgumentException("$this not a double")
    }

    return ConditionUpper(this, value)
}

infix fun Column.UPPER(value: GregorianCalendar): Condition
{
    if (this.type != DATE)
    {
        throw IllegalArgumentException("$this not a date")
    }

    return ConditionUpper(this, value)
}

infix fun Column.UPPER(value: String): Condition
{
    if (this.type != TEXT)
    {
        throw IllegalArgumentException("$this not a text")
    }

    return ConditionUpper(this, value)
}

infix fun <PC> Column.UPPER(value: PC): Condition where PC : Parcelable, PC : Comparable<PC>
{
    if (this.type !is PARCELABLE<*>)
    {
        throw IllegalArgumentException("$this not a parcelable")
    }

    if (this.type.clazz != value.javaClass)
    {
        throw IllegalArgumentException("$this not a parcelable of ${value.javaClass}")
    }

    return ConditionUpper(this, value)
}