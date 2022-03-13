package ru.cities.game.util

/** Getting a valid last letter of the city */
fun getLastChar(city: String): Char {
    var lastChar: Char = city.last()
    while (lastChar == 'ё' || lastChar == 'ы' || lastChar == 'ь')
        lastChar = city[city.lastIndex - 1]
    return lastChar.uppercaseChar()
}
