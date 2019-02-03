package fr.jhelp.database.condition

import android.os.Parcelable
import fr.jhelp.database.Column
import java.util.GregorianCalendar

infix fun Column.LOWER_EQUAL_PRIMARY(id: Long) = (this LOWER_PRIMARY id) OR (this EQUALS_PRIMARY id)
infix fun Column.LOWER_EQUAL(value: Int) = (this LOWER value) OR (this EQUALS value)
infix fun Column.LOWER_EQUAL(value: Long) = (this LOWER value) OR (this EQUALS value)
infix fun Column.LOWER_EQUAL(value: Float) = (this LOWER value) OR (this EQUALS value)
infix fun Column.LOWER_EQUAL(value: Double) = (this LOWER value) OR (this EQUALS value)
infix fun Column.LOWER_EQUAL(value: GregorianCalendar) = (this LOWER value) OR (this EQUALS value)
infix fun Column.LOWER_EQUAL(value: String) = (this LOWER value) OR (this EQUALS value)
infix fun <PC> Column.LOWER_EQUAL(value: PC): Condition where PC : Parcelable, PC : Comparable<PC> =
    (this LOWER value) OR (this EQUALS value)

infix fun Column.UPPER_EQUAL_PRIMARY(id: Long) = (this UPPER_PRIMARY id) OR (this EQUALS_PRIMARY id)
infix fun Column.UPPER_EQUAL(value: Int) = (this UPPER value) OR (this EQUALS value)
infix fun Column.UPPER_EQUAL(value: Long) = (this UPPER value) OR (this EQUALS value)
infix fun Column.UPPER_EQUAL(value: Float) = (this UPPER value) OR (this EQUALS value)
infix fun Column.UPPER_EQUAL(value: Double) = (this UPPER value) OR (this EQUALS value)
infix fun Column.UPPER_EQUAL(value: GregorianCalendar) = (this UPPER value) OR (this EQUALS value)
infix fun Column.UPPER_EQUAL(value: String) = (this UPPER value) OR (this EQUALS value)
infix fun <PC> Column.UPPER_EQUAL(value: PC): Condition where PC : Parcelable, PC : Comparable<PC> =
    (this UPPER value) OR (this EQUALS value)

fun Column.BETWEEN_PRIMARY(minimum: Long, maximum: Long) =
    (this UPPER_EQUAL_PRIMARY minimum) AND (this LOWER_EQUAL_PRIMARY maximum)

fun Column.BETWEEN(minimum: Int, maximum: Int) = (this UPPER_EQUAL minimum) AND (this LOWER_EQUAL maximum)
fun Column.BETWEEN(minimum: Long, maximum: Long) = (this UPPER_EQUAL minimum) AND (this LOWER_EQUAL maximum)
fun Column.BETWEEN(minimum: Float, maximum: Float) = (this UPPER_EQUAL minimum) AND (this LOWER_EQUAL maximum)
fun Column.BETWEEN(minimum: Double, maximum: Double) = (this UPPER_EQUAL minimum) AND (this LOWER_EQUAL maximum)
fun Column.BETWEEN(minimum: GregorianCalendar, maximum: GregorianCalendar) =
    (this UPPER_EQUAL minimum) AND (this LOWER_EQUAL maximum)

fun Column.BETWEEN(minimum: String, maximum:String) = (this UPPER_EQUAL minimum) AND (this LOWER_EQUAL maximum)
fun <PC> Column.BETWEEN(minimum: PC, maximum: PC): Condition where PC : Parcelable, PC : Comparable<PC> =
    (this UPPER_EQUAL minimum) AND (this LOWER_EQUAL maximum)

