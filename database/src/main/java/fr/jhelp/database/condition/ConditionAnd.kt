package fr.jhelp.database.condition

import fr.jhelp.database.Column

class ConditionAnd(private val condition1: Condition, private val condition2: Condition) : Condition
{
    override fun refresh()
    {
        this.condition1.refresh()
        this.condition2.refresh()
    }

    override fun valid(values: Map<Column, Any?>) =
        this.condition1.valid(values) && this.condition2.valid(values)
}

infix fun Condition.AND(condition: Condition): Condition = ConditionAnd(this, condition)