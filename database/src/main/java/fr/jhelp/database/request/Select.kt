package fr.jhelp.database.request

import fr.jhelp.database.Column
import fr.jhelp.database.Table
import fr.jhelp.database.condition.Condition
import fr.jhelp.database.condition.NoCondition

class Select(val table: Table, val columns: Array<Column>)
{
    init
    {
        this.columns.firstOrNull { it !in this.table }
            ?.let { throw IllegalArgumentException("$it column not in table ${this.table}") }
    }

    var where: Condition = NoCondition

    infix fun WHERE(where: Condition): Select
    {
        this.where = where
        return this
    }
}
