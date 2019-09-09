package fr.jhelp.database.condition

import fr.jhelp.database.Column
import fr.jhelp.database.TEXT
import java.util.regex.Pattern

class ConditionRegex internal constructor(private val column: Column, private val pattern: Pattern) : Condition
{
    override fun valid(values: Map<Column, Any?>): Boolean
    {
        val columnValue = values[this.column] as String?
        return columnValue?.let { this.pattern.matcher(it).matches() } ?: false
    }
}

infix fun Column.REGEX(pattern: Pattern): Condition
{
    if (this.type != TEXT)
    {
        throw IllegalArgumentException("$this not a text")
    }

    return ConditionRegex(this, pattern)
}

infix fun Column.REGEX(regex: String) = this REGEX Pattern.compile(regex)

class ConditionEqualsIgnoreCase internal constructor(private val column: Column, private val value: String) :
    Condition
{
    override fun valid(values: Map<Column, Any?>): Boolean
    {
        val columnValue = values[this.column] as String?
        return columnValue?.let { this.value.equals(columnValue, true) } ?: false
    }
}

infix fun Column.EQUALS_IGNORE_CASE(value: String): Condition
{
    if (this.type != TEXT)
    {
        throw IllegalArgumentException("$this not a text")
    }

    return ConditionEqualsIgnoreCase(this, value)
}

class ConditionUpperIgnoreCase internal constructor(private val column: Column, private val value: String) : Condition
{
    override fun valid(values: Map<Column, Any?>): Boolean
    {
        val columnValue = values[this.column] as String?
        return columnValue?.let { this.value.compareTo(columnValue, true) > 0 } ?: false
    }
}

infix fun Column.UPPER_IGNORE_CASE(value: String): Condition
{
    if (this.type != TEXT)
    {
        throw IllegalArgumentException("$this not a text")
    }

    return ConditionUpperIgnoreCase(this, value)
}

class ConditionLowerIgnoreCase internal constructor(private val column: Column, private val value: String) : Condition
{
    override fun valid(values: Map<Column, Any?>): Boolean
    {
        val columnValue = values[this.column] as String?
        return columnValue?.let { this.value.compareTo(columnValue, true) < 0 } ?: false
    }
}

infix fun Column.LOWER_IGNORE_CASE(value: String): Condition
{
    if (this.type != TEXT)
    {
        throw IllegalArgumentException("$this not a text")
    }

    return ConditionLowerIgnoreCase(this, value)
}

infix fun Column.UPPER_EQUALS_IGNORE_CASE(value: String) =
    (this UPPER_IGNORE_CASE value) OR (this EQUALS_IGNORE_CASE value)

infix fun Column.LOWER_EQUALS_IGNORE_CASE(value: String) =
    (this LOWER_IGNORE_CASE value) OR (this EQUALS_IGNORE_CASE value)