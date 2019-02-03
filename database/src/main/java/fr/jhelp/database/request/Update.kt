package fr.jhelp.database.request

import fr.jhelp.database.EncryptedContent
import fr.jhelp.database.condition.Condition
import fr.jhelp.database.condition.NoCondition

class Update(val encryptedContent: EncryptedContent)
{
    var where: Condition = NoCondition

    infix fun WHERE(where: Condition) : Update
    {
        this.where = where
        return this
    }
}