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

class ConditionEquals internal constructor(private val column: Column, private val value: Any) : Condition
{
    override fun valid(values: Map<Column, Any?>): Boolean
    {
        val columnValue = values[this.column]

        return when (this.column.type)
        {
            PRIMARY_KEY      -> (columnValue as Long) == (this.value as Long)
            INTEGER          -> (columnValue as Int) == (this.value as Int)
            LONG             -> (columnValue as Long) == (this.value as Long)
            FLOAT            -> (columnValue as Float) == (this.value as Float)
            DOUBLE           -> (columnValue as Double) == (this.value as Double)
            DATE             -> ((columnValue as GregorianCalendar).timeInMillis) == ((this.value as GregorianCalendar).timeInMillis)
            TEXT             -> (columnValue as String) == (this.value as String)
            is PARCELABLE<*> -> columnValue == this.value
        }
    }
}

infix fun Column.EQUALS_PRIMARY(id: Long): Condition
{
    if (this.type != PRIMARY_KEY)
    {
        throw IllegalArgumentException("$this not a primary key")
    }

    return ConditionEquals(this, id)
}

infix fun Column.EQUALS(value: Int): Condition
{
    if (this.type != INTEGER)
    {
        throw IllegalArgumentException("$this not a integer")
    }

    return ConditionEquals(this, value)
}

infix fun Column.EQUALS(value: Long): Condition
{
    if (this.type != LONG)
    {
        throw IllegalArgumentException("$this not a long")
    }

    return ConditionEquals(this, value)
}

infix fun Column.EQUALS(value: Float): Condition
{
    if (this.type != FLOAT)
    {
        throw IllegalArgumentException("$this not a float")
    }

    return ConditionEquals(this, value)
}

infix fun Column.EQUALS(value: Double): Condition
{
    if (this.type != DOUBLE)
    {
        throw IllegalArgumentException("$this not a double")
    }

    return ConditionEquals(this, value)
}

infix fun Column.EQUALS(value: GregorianCalendar): Condition
{
    if (this.type != DATE)
    {
        throw IllegalArgumentException("$this not a date")
    }

    return ConditionEquals(this, value)
}

infix fun Column.EQUALS(value: String): Condition
{
    if (this.type != TEXT)
    {
        throw IllegalArgumentException("$this not a text")
    }

    return ConditionEquals(this, value)
}

infix fun <P : Parcelable> Column.EQUALS(value: P): Condition
{
    if (this.type !is PARCELABLE<*>)
    {
        throw IllegalArgumentException("$this not a parcelable")
    }

    if (this.type.clazz != value.javaClass)
    {
        throw IllegalArgumentException("$this not a parcelable of ${value.javaClass}")
    }

    return ConditionEquals(this, value)
}
