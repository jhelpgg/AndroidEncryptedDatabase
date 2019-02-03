package fr.jhelp.database.request

import fr.jhelp.database.Table
import fr.jhelp.database.condition.Condition
import fr.jhelp.database.condition.NoCondition

class Delete(val table:Table)
{
    var where: Condition = NoCondition

    infix fun WHERE(where: Condition) : Delete
    {
        this.where = where
        return this
    }
}