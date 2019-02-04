package fr.jhelp.database

import android.content.Context
import android.database.Cursor
import android.os.Parcelable
import fr.jhelp.cryptographic.decrypt
import fr.jhelp.cryptographic.decryptParcelable
import fr.jhelp.database.condition.Condition
import java.lang.ref.WeakReference
import java.util.GregorianCalendar

class EncryptedCursor internal constructor(
    internal val cursor: Cursor,
    private val context: WeakReference<Context>,
    private val realColumns: Array<Column>,
    private val visibleColumns: Array<Column>,
    private val where: Condition)
{
    private val values = HashMap<Column, Any?>()

    val numberColumns = this.visibleColumns.size
    fun getColumn(index: Int) = this.visibleColumns[index]
    val count get() = this.cursor.count
    val position get() = this.cursor.position

    fun moveToNext(): Boolean
    {
        while (this.cursor.moveToNext())
        {
            this.collectValues()

            if (this.where.valid(this.values))
            {
                return true
            }
        }

        this.cursor.close()
        return false
    }

    fun close() = this.cursor.close()
    val closed get() = this.cursor.isClosed

    private fun decrypted(column: Int) =
        when
        {
            this.cursor.isNull(column) -> null
            else                       -> this.context.get()?.let { decrypt(it, this.cursor.getString(column)) }
        }

    private fun collectValues()
    {
        this.values.clear()

        (0 until this.realColumns.size).forEach { col ->
            val column = this.realColumns[col]
            when (column.type)
            {
                PRIMARY_KEY      -> this.values[column] = this.cursor.getLong(col)
                is PARCELABLE<*> ->
                {
                    if (this.cursor.isNull(col))
                    {
                        this.values[column] = null
                    }
                    else
                    {
                        this.values[column] = this.context.get()?.let { context ->
                            val encrypted = this.cursor.getString(col)
                            decryptParcelable(context, column.type.clazz, encrypted)
                        }
                    }
                }
                else             ->
                {
                    val decrypted = decrypted(col)
                    this.values[column] =
                        when (column.type)
                        {
                            INTEGER -> decrypted?.toInt() ?: 0
                            LONG    -> decrypted?.toLong() ?: 0L
                            FLOAT   -> decrypted?.toFloat() ?: 0f
                            DOUBLE  -> decrypted?.toDouble() ?: 0.0
                            DATE    -> decrypted?.let { va ->
                                val time = va.toLong()
                                val date = GregorianCalendar()
                                date.timeInMillis = time
                                date
                            }
                            TEXT    -> decrypted
                            else    -> RuntimeException("Should not goes here with : ${column.type}")
                        }
                }
            }
        }
    }

    fun getPrimaryKey(column: Int): Long
    {
        val col = this.visibleColumns[column]

        if (col.type != PRIMARY_KEY)
        {
            throw IllegalArgumentException("Column at $column : $col not a primary key")
        }

        return this.values[col] as Long
    }


    fun getInt(column: Int): Int
    {
        val col = this.visibleColumns[column]

        if (col.type != INTEGER)
        {
            throw IllegalArgumentException("Column at $column : $col not an integer")
        }

        return this.values[col] as Int
    }

    fun getLong(column: Int): Long
    {
        val col = this.visibleColumns[column]

        if (col.type != LONG)
        {
            throw IllegalArgumentException("Column at $column : $col not a long")
        }

        return this.values[col] as Long
    }

    fun getFloat(column: Int): Float
    {
        val col = this.visibleColumns[column]

        if (col.type != FLOAT)
        {
            throw IllegalArgumentException("Column at $column : $col not a float")
        }

        return this.values[col] as Float
    }

    fun getDouble(column: Int): Double
    {
        val col = this.visibleColumns[column]

        if (col.type != DOUBLE)
        {
            throw IllegalArgumentException("Column at $column : $col not a double")
        }

        return this.values[col] as Double
    }

    fun getDate(column: Int): GregorianCalendar?
    {
        val col = this.visibleColumns[column]

        if (col.type != DATE)
        {
            throw IllegalArgumentException("Column at $column : $col not a date")
        }

        return this.values[col] as GregorianCalendar?
    }

    fun getText(column: Int): String?
    {
        val col = this.visibleColumns[column]

        if (col.type != TEXT)
        {
            throw IllegalArgumentException("Column at $column : $col not a text")
        }

        return this.values[col] as String?
    }

    fun <P : Parcelable> getParcelable(column: Int, clazz: Class<P>): P?
    {
        val col = this.visibleColumns[column]

        if (col.type !is PARCELABLE<*>)
        {
            throw IllegalArgumentException("Column at $column : $col not a parcelable")
        }

        if (col.type.clazz != clazz)
        {
            throw IllegalArgumentException("Column at $column : $col not a parcelable of ${clazz.name}")
        }

        return this.values[col] as P?
    }
}