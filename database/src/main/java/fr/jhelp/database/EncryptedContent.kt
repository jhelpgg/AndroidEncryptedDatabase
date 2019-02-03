package fr.jhelp.database

import android.os.Parcelable
import java.util.GregorianCalendar

class EncryptedContent(val table: Table)
{
    private val content = HashMap<Column, Any>()
    internal val contentEntries get() = this.content.entries

    private fun check(column: Column, columnType: ColumnType)
    {
        if (column !in this.table)
        {
            throw IllegalArgumentException("$column not inside the table $table")
        }

        if (column.type != columnType)
        {
            throw IllegalArgumentException("Can't put a $columnType in column $column")
        }
    }

    operator fun set(column: Column, value: Int)
    {
        this.check(column, INTEGER)
        this.content[column] = value
    }

    operator fun set(column: Column, value: Long)
    {
        this.check(column, LONG)
        this.content[column] = value
    }

    operator fun set(column: Column, value: Float)
    {
        this.check(column, FLOAT)
        this.content[column] = value
    }

    operator fun set(column: Column, value: Double)
    {
        this.check(column, DOUBLE)
        this.content[column] = value
    }

    operator fun set(column: Column, value: GregorianCalendar)
    {
        this.check(column, DATE)
        this.content[column] = value.timeInMillis
    }

    operator fun set(column: Column, value: String)
    {
        this.check(column, TEXT)
        this.content[column] = value
    }

    operator fun <P : Parcelable> set(column: Column, value: P)
    {
        if (column !in this.table)
        {
            throw IllegalArgumentException("$column not inside the table $table")
        }

        val clazz = value.javaClass
        val type = column.type as? PARCELABLE<P>
                   ?: throw IllegalArgumentException("Can't put a parcelable of ${clazz.name} in column $column")

        if (type.clazz != clazz)
        {
            throw IllegalArgumentException("Can't put a parcelable of ${clazz.name} in column $column")
        }

        this.content[column] = value
    }
}