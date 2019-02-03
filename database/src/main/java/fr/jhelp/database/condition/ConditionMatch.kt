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

class ConditionMatch<T> internal constructor(private val column: Column,private val values:Array<T>) : Condition
{
    override fun valid(values: Map<Column, Any?>): Boolean
    {
        val columnValue = values[this.column]

        return when (this.column.type)
        {
            PRIMARY_KEY      -> (columnValue as Long) in (this.values as Array<Long>)
            INTEGER          -> (columnValue as Int) in (this.values as Array<Int>)
            LONG             -> (columnValue as Long) in (this.values as Array<Long>)
            FLOAT            -> (columnValue as Float) in (this.values as Array<Float>)
            DOUBLE           -> (columnValue as Double) in (this.values as Array<Double>)
            DATE             -> ((columnValue as GregorianCalendar).timeInMillis) in  (this.values as Array<GregorianCalendar>).map { it.timeInMillis }
            TEXT             -> (columnValue as String) in (this.values as Array<String>)
            is PARCELABLE<*> -> columnValue in this.values
        }
    }
}

infix fun Column.MATCH_PRIMARY(ids: Array<Long>): Condition
{
    if (this.type != PRIMARY_KEY)
    {
        throw IllegalArgumentException("$this not a primary key")
    }

    return ConditionMatch(this, ids)
}

infix fun Column.MATCH(values: Array<Int>): Condition
{
    if (this.type != INTEGER)
    {
        throw IllegalArgumentException("$this not a integer")
    }

    return ConditionMatch(this, values)
}

infix fun Column.MATCH(values: Array<Long>): Condition
{
    if (this.type != LONG)
    {
        throw IllegalArgumentException("$this not a long")
    }

    return ConditionMatch(this, values)
}

infix fun Column.MATCH(values: Array<Float>): Condition
{
    if (this.type != FLOAT)
    {
        throw IllegalArgumentException("$this not a float")
    }

    return ConditionMatch(this, values)
}

infix fun Column.MATCH(values: Array<Double>): Condition
{
    if (this.type != DOUBLE)
    {
        throw IllegalArgumentException("$this not a double")
    }

    return ConditionMatch(this, values)
}

infix fun Column.MATCH(values: Array<GregorianCalendar>): Condition
{
    if (this.type != DATE)
    {
        throw IllegalArgumentException("$this not a date")
    }

    return ConditionMatch(this, values)
}

infix fun Column.MATCH(values: Array<String>): Condition
{
    if (this.type != TEXT)
    {
        throw IllegalArgumentException("$this not a text")
    }

    return ConditionMatch(this, values)
}

infix fun <P : Parcelable> Column.MATCH(values: Array<P>): Condition
{
    if (this.type !is PARCELABLE<*>)
    {
        throw IllegalArgumentException("$this not a parcelable")
    }

    if (this.type.clazz != values.javaClass.componentType)
    {
        throw IllegalArgumentException("$this not a parcelable of ${values.javaClass.componentType}")
    }

    return ConditionMatch(this, values)
}
