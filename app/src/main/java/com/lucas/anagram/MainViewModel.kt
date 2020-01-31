package com.lucas.anagram

import android.util.Log
import androidx.lifecycle.ViewModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.math.BigInteger
import java.net.URL
import kotlin.collections.ArrayList

class MainViewModel : ViewModel() {
    var RandomString = ""
    var WordsList = ArrayList<WordData>()
    var AnagramsList = ArrayList<String>()
    fun generateChars() {
        val alphabets = "QWERTYUIOPASDFGHJKLZXCVBNM"
        var returnstring = ""
        for (i in 1..10) {
            var rnd = (0..alphabets.length - 1).random()
            var character = alphabets.get(rnd)
            returnstring = "$returnstring$character"
        }
        RandomString = returnstring
    }

    /** Get the dictionary full of words and calculate their total value based off the total value of
     * prime numbers and each alphabet is assigned it's own unique prime, i.e.(A=2, M=41). This ensures a unique number is always calculated for each possible anagram.
     * We will then insert this unique calculated value.
     */
    fun GetFile(timeout: Int): Boolean {
        var returnlist = ArrayList<WordData>()
        val end = System.currentTimeMillis()+ timeout //add timeout to current time
        while(System.currentTimeMillis() < end) { //loop until timeout is reached OR function was successfully executed
            val url = URL("https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt")
            try {
                val input = BufferedReader(InputStreamReader(url.openStream()))
                input.lines().forEach{
                    returnlist.add(WordData(it, GetStringValue(it)))
                }
                WordsList = returnlist
                return true
            } catch (e: IOException) {
                Log.e("ERROR: IOException", "An error has occurred")
            }
        }
        WordsList = returnlist
        return false
    }
    //calculate the value in an inline function instead as this long ass function will be used more than once
    inline fun GetStringValue(word: String): BigInteger{
        var value = 1.toBigInteger()
        word.forEach {
            when(it.toUpperCase()){
                'A' -> value *= 2.toBigInteger()
                'B' -> value *= 3.toBigInteger()
                'C' -> value *= 5.toBigInteger()
                'D' -> value *= 7.toBigInteger()
                'E' -> value *= 11.toBigInteger()
                'F' -> value *= 13.toBigInteger()
                'G' -> value *= 17.toBigInteger()
                'H' -> value *= 19.toBigInteger()
                'I' -> value *= 23.toBigInteger()
                'J' -> value *= 29.toBigInteger()
                'K' -> value *= 31.toBigInteger()
                'L' -> value *= 37.toBigInteger()
                'M' -> value *= 41.toBigInteger()
                'N' -> value *= 43.toBigInteger()
                'O' -> value *= 47.toBigInteger()
                'P' -> value *= 53.toBigInteger()
                'Q' -> value *= 59.toBigInteger()
                'R' -> value *= 61.toBigInteger()
                'S' -> value *= 67.toBigInteger()
                'T' -> value *= 71.toBigInteger()
                'U' -> value *= 73.toBigInteger()
                'V' -> value *= 79.toBigInteger()
                'W' -> value *= 83.toBigInteger()
                'X' -> value *= 89.toBigInteger()
                'Y' -> value *= 97.toBigInteger()
                'Z' -> value *= 101.toBigInteger()
            }
        }
        return value
    }

    /***
     * the purpose of the function should be self explanatory if you understood the documentation above.
     * If the word's value is equal to the anagram's value, they are both anagrams and we will add it to the list.
     * We will run this function asynchronously as it is rather heavy due to having to loop through thousands of words im the provided list.
     */
    fun GetAnagrams(word: String) {
        //clear the list before starting
        AnagramsList.clear()
        var wd = WordData(word, GetStringValue(word))
        WordsList.forEach{
            //DO NOT bother calculating any string that is larger
            //if divisible by the word, it means it contains the same prime numbers(prime factorisation), hence is an anagram
            if(it.WordValue <= wd.WordValue && (wd.WordValue % it.WordValue) == 0.toBigInteger() ){
                AnagramsList.add(it.Word)
            }
        }
    }

}
data class WordData(var Word: String, var WordValue: BigInteger)