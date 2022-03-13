package ru.cities.game.util

fun normalizeUserCity(text: String): String {
    var userCity: String = text.trim()
    while (userCity.isNotBlank() && (userCity.first() == ' ' || userCity.first() == '-'))
        userCity = userCity.trim().trim('-')
    return userCity
        .replace('ë', 'е')//the letters "ё" and "ë" are different
        .replace('ё', 'е')//the letters "ё" and "ë" are different
        .replaceFirstChar(Char::uppercase)
}
