package fr.jhelp.database

import android.content.ContentValues
import android.content.Context
import android.os.Parcelable
import fr.jhelp.cryptographic.encrypt
import fr.jhelp.cryptographic.encryptName
import fr.jhelp.cryptographic.encryptParcelable
import fr.jhelp.database.request.Delete
import fr.jhelp.database.request.Select
import fr.jhelp.database.request.Update
import java.lang.ref.WeakReference
import java.util.TreeSet

class DatabaseEncrypted(context: Context, databaseName: String)
{
    private val context = WeakReference(context.applicationContext)
    private val database = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null)
    private val tables = TreeSet<Table>()

    fun tables() = this.tables.toTypedArray()

    fun createTable(table: Table)
    {
        if (!this.tables.add(table))
        {
            throw IllegalArgumentException("Table $table already created")
        }

        this.context.get()?.let { context ->
            val request = StringBuilder()
            request.append("CREATE TABLE IF NOT EXISTS ")
            request.append(encryptName(context, table.name))
            request.append(" (")
            var notFirst = false

            for (column in table)
            {
                if (notFirst)
                {
                    request.append(", ")
                }

                notFirst = true
                request.append(encryptName(context, column.name))

                if (column.type == PRIMARY_KEY)
                {
                    request.append(" INTEGER PRIMARY KEY ASC ON CONFLICT FAIL AUTOINCREMENT")
                }
                else
                {
                    request.append(" TEXT")
                }
            }

            request.append(")")
            this.database.execSQL(request.toString())
        }

        table.created()
    }

    fun close()
    {
        this.database.close()
    }

    fun insert(encryptedContent: EncryptedContent)
    {
        if (encryptedContent.table !in this.tables)
        {
            throw IllegalArgumentException("Unknown table : ${encryptedContent.table}")
        }

        this.context.get()?.let { context ->
            val tableName = encryptName(context, encryptedContent.table.name)
            val contentValues = ContentValues()

            for ((column, value) in encryptedContent.contentEntries)
            {
                val columnName = encryptName(context, column.name)

                when (column.type)
                {
                    PRIMARY_KEY      -> contentValues.put(columnName, value as Long)
                    INTEGER          -> contentValues.put(columnName, encrypt(context, value.toString()))
                    LONG             -> contentValues.put(columnName, encrypt(context, value.toString()))
                    FLOAT            -> contentValues.put(columnName, encrypt(context, value.toString()))
                    DOUBLE           -> contentValues.put(columnName, encrypt(context, value.toString()))
                    DATE             -> contentValues.put(columnName, encrypt(context, value.toString()))
                    TEXT             -> contentValues.put(columnName, encrypt(context, value.toString()))
                    is PARCELABLE<*> -> contentValues.put(columnName, encryptParcelable(context, value as Parcelable))
                }
            }

            this.database.insert(tableName, null, contentValues)
        }
    }

    fun select(select: Select): EncryptedCursor
    {
        if (select.table !in this.tables)
        {
            throw IllegalArgumentException("Unknown table : ${select.table}")
        }

        select.where.refresh()
        val table = select.table
        val columns = select.columns
        columns.firstOrNull { it !in table }?.let { throw IllegalArgumentException("$it column not in table $table") }

        return this.context.get()?.let { context ->
            val tableName = encryptName(context, table.name)
            val allColumns = table.columns()
            val columnsName = Array(allColumns.size) { encryptName(context, allColumns[it].name) }
            val cursor = this.database.query(tableName, columnsName, null, null, null, null, null)
            EncryptedCursor(cursor, this.context, allColumns, columns, select.where)
        } ?: throw IllegalStateException("No context !")
    }

    fun update(update: Update): Int
    {
        if (update.encryptedContent.table !in this.tables)
        {
            throw IllegalArgumentException("Unknown table : ${update.encryptedContent.table}")
        }

        var count = 0

        this.context.get()?.let { context ->
            val encryptedContent = update.encryptedContent
            val contentValues = ContentValues()

            for ((column, value) in encryptedContent.contentEntries)
            {
                val columnName = encryptName(context, column.name)

                when (column.type)
                {
                    PRIMARY_KEY      -> contentValues.put(columnName, value as Long)
                    INTEGER          -> contentValues.put(columnName, encrypt(context, value.toString()))
                    LONG             -> contentValues.put(columnName, encrypt(context, value.toString()))
                    FLOAT            -> contentValues.put(columnName, encrypt(context, value.toString()))
                    DOUBLE           -> contentValues.put(columnName, encrypt(context, value.toString()))
                    DATE             -> contentValues.put(columnName, encrypt(context, value.toString()))
                    TEXT             -> contentValues.put(columnName, encrypt(context, value.toString()))
                    is PARCELABLE<*> -> contentValues.put(columnName, encryptParcelable(context, value as Parcelable))
                }
            }

            val tableName = encryptName(context, encryptedContent.table.name)
            val columns = encryptedContent.table.columns()
            val encryptedCursor = this.select(Select(encryptedContent.table, columns) WHERE update.where)

            while (encryptedCursor.moveToNext())
            {
                val cursor = encryptedCursor.cursor
                val conditionValues = arrayOfNulls<String>(columns.size)
                val condition = StringBuilder()
                condition.append(encryptName(context, columns[0].name))
                condition.append("=?")

                conditionValues[0] = when
                {
                    columns[0].type == PRIMARY_KEY -> cursor.getLong(0).toString()
                    cursor.isNull(0)               -> null
                    else                           -> cursor.getString(0)
                }

                for (index in 1 until columns.size)
                {
                    condition.append(" AND ")
                    condition.append(encryptName(context, columns[index].name))
                    condition.append("=?")

                    conditionValues[index] = when
                    {
                        columns[index].type == PRIMARY_KEY -> cursor.getLong(index).toString()
                        cursor.isNull(index)               -> null
                        else                               -> cursor.getString(index)
                    }
                }

                count += this.database.update(tableName, contentValues, condition.toString(), conditionValues)
            }
        }

        return count
    }

    fun delete(delete: Delete): Int
    {
        if (delete.table !in this.tables)
        {
            throw IllegalArgumentException("Unknown table : ${delete.table}")
        }

        var count = 0

        this.context.get()?.let { context ->
            val tableName = encryptName(context, delete.table.name)
            val columns = delete.table.columns()
            val encryptedCursor = this.select(Select(delete.table, columns) WHERE delete.where)

            while (encryptedCursor.moveToNext())
            {
                val cursor = encryptedCursor.cursor
                val conditionValues = arrayOfNulls<String>(columns.size)
                val condition = StringBuilder()
                condition.append(encryptName(context, columns[0].name))
                condition.append("=?")

                conditionValues[0] = when
                {
                    columns[0].type == PRIMARY_KEY -> cursor.getLong(0).toString()
                    cursor.isNull(0)               -> null
                    else                           -> cursor.getString(0)
                }

                for (index in 1 until columns.size)
                {
                    condition.append(" AND ")
                    condition.append(encryptName(context, columns[index].name))
                    condition.append("=?")

                    conditionValues[index] = when
                    {
                        columns[index].type == PRIMARY_KEY -> cursor.getLong(index).toString()
                        cursor.isNull(index)               -> null
                        else                               -> cursor.getString(index)
                    }
                }

                count += this.database.delete(tableName, condition.toString(), conditionValues)
            }
        }

        return count
    }

    fun drop(table: Table)
    {
        this.context.get()?.let { context ->
            if (table in this.tables)
            {
                val tableName = encryptName(context, table.name)
                this.database.execSQL("DROP TABLE $tableName")
                this.tables.remove(table)
            }
        }
    }


}