package fr.jhelp.database

import android.os.Parcelable

sealed class ColumnType

object PRIMARY_KEY : ColumnType()
object INTEGER : ColumnType()
object LONG : ColumnType()
object FLOAT : ColumnType()
object DOUBLE : ColumnType()
object DATE : ColumnType()
object TEXT : ColumnType()

data class PARCELABLE<P : Parcelable>(val clazz: Class<P>) : ColumnType()
{
    override fun toString() = "Parcelable:${clazz.name}"
}