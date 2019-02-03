package fr.jhelp.database

import android.content.ContentValues
import android.content.Context
import android.os.Parcelable
import fr.jhelp.cryptographic.encrypt
import fr.jhelp.cryptographic.encryptName
import fr.jhelp.cryptographic.encryptParcelable
import fr.jhelp.database.request.Select
import java.lang.ref.WeakReference
import java.util.TreeSet

class DatabaseEncrypted(context: Context, databaseName: String)
{
    private val context = WeakReference(context.applicationContext)
    private val database = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null)
    private val tables = TreeSet<Table>()

    fun createTable(table: Table)
    {
        if (!this.tables.add(table))
        {
            throw IllegalArgumentException(("Table $table already created"))
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
                    //   else             -> throw IllegalArgumentException("Not managed type ${column.type}")
                }
            }

            this.database.insert(tableName, null, contentValues)
        }
    }

    fun select(select: Select): EncryptedCursor
    {
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
}