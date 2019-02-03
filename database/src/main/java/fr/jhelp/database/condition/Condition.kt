package fr.jhelp.database.condition

import fr.jhelp.database.Column
import fr.jhelp.database.EncryptedCursor

interface Condition
{
    fun valid(values:Map<Column, Any?>) : Boolean
}

object NoCondition : Condition
{
    override fun valid(values:Map<Column, Any?>) = true
}