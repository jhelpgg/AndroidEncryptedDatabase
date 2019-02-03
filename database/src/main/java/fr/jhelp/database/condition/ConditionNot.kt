package fr.jhelp.database.condition

import fr.jhelp.database.Column

class ConditionNot(private val condition:Condition) : Condition
{
    override fun refresh()
    {
        this.condition.refresh()
    }

    override fun valid(values: Map<Column, Any?>) = !this.condition.valid(values)
}

val Condition.NOT :Condition get() = ConditionNot(this)