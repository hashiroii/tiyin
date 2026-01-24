package kz.hashiroii.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home : TiyinDestination

@Serializable
object Analytics : TiyinDestination

@Serializable
object Groups : TiyinDestination

@Serializable
object Profile : TiyinDestination

@Serializable
object Settings : TiyinDestination

interface TiyinDestination
