package com.bera.telegram_drive.nav

sealed class Screen(protected val route: String, vararg params: String) {
    val fullRoute: String = if (params.isEmpty()) route else {
        val builder = StringBuilder(route)
        params.forEach { builder.append("/{${it}}") }
        builder.toString()
    }

    sealed class NoArgumentsScreen(route: String) : Screen(route) {
        operator fun invoke(): String = route
    }

    data object HomeScreen : NoArgumentsScreen("home")

    data object UsersScreen : NoArgumentsScreen("users")

    data object MessagesScreen : NoArgumentsScreen("messages")

    data object DetailsScreen : NoArgumentsScreen("details")

    data object UserDetailsScreen : Screen("user_details", "firstName", "lastName") {
        const val FIRST_NAME_KEY = "firstName"
        const val LAST_NAME_KEY = "lastName"

        operator fun invoke(fistName: String, lastName: String): String = route.appendParams(
            FIRST_NAME_KEY to fistName,
            LAST_NAME_KEY to lastName
        )
    }
}

internal fun String.appendParams(vararg params: Pair<String, Any?>): String {
    val builder = StringBuilder(this)

    params.forEach {
        it.second?.toString()?.let { arg ->
            builder.append("/$arg")
        }
    }

    return builder.toString()
}