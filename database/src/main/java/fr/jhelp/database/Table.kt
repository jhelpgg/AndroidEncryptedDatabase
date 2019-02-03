package fr.jhelp.database

import fr.jhelp.utilities.EnumerationIterator
import java.lang.IllegalArgumentException

class Table(val name: String) : Iterable<Column>, Comparable<Table>
{
    private val columns = ArrayList<Column>()
    var created = false
        private set
    val size = this.columns.size

    internal fun created()
    {
        this.created = true
    }

    internal fun columns() = this.columns.toTypedArray()

    operator fun plusAssign(column: Column)
    {
        if (this.created)
        {
            throw IllegalStateException("Can't add column after table is created")
        }

        if(column.type == PRIMARY_KEY && this.columns.any { it.type == PRIMARY_KEY })
        {
            throw IllegalArgumentException("Table ${this.name} have already a primary key")
        }

        if(this.columns.any { it.name == column.name })
        {
            throw IllegalArgumentException("Table ${this.name} have already a column named ${column.name}")
        }

        this.columns += column
    }

    override fun iterator() = EnumerationIterator(this.columns.iterator())

    operator fun get(index: Int) = this.columns[index]

    operator fun contains(column: Column) = column in this.columns

    override fun toString()="Table ${this.name} [${this.columns}]"

    override operator fun compareTo(other:Table) = this.name.compareTo(other.name)

    override fun equals(other: Any?): Boolean
    {
        if(other === this) {return true}

        if(other == null || other !is Table) {return false}

        return this.name == other.name
    }

    override fun hashCode() = this.name.hashCode()
}