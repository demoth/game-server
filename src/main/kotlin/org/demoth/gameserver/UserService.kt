package org.demoth.gameserver

import org.demoth.gameserver.model.User
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import java.util.ArrayList
import java.util.HashMap

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
open class UserService {
    private val currentUsers = HashMap<String, User>()

    fun login(user: String, pass: String): User? {
        return if ("demoth" == user) User() else null
    }

    fun register(user: User, id: String): List<String> {
        currentUsers[id] = user
        val chars = ArrayList<String>()
        chars.add("Totemy [Templar lvl 12]")
        chars.add("ArkWi4ka [Witch lvl 2]")
        return chars
    }
}
