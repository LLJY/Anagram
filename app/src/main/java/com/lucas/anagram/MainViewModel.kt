package com.lucas.anagram

import android.util.Log
import androidx.lifecycle.ViewModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
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
            alphabets.drop(rnd)
        }
        RandomString = returnstring
    }

    /** Get the dictionary full of words and calculate their total value based off the total value of
     * prime numbers and each alphabet is assigned it's own unique prime, i.e.(A=2, M=41). This ensures a unique number is always calculated for each possible anagram.
     * We will then insert this unique calculated value.
     */
    fun GetFile(timeout: Int) {
        var returnlist = ArrayList<WordData>()
        val end = System.currentTimeMillis()+ timeout //add timeout to current time
        var success = false
        while(System.currentTimeMillis() < end || !success) { //loop until timeout is reached OR function was successfully executed
            val url = URL("https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt")
            try {
                val input = BufferedReader(InputStreamReader(url.openStream()))
                input.lines().forEach{
                    returnlist.add(WordData(it, GetStringValue(it)))
                }
                success = true
            } catch (e: IOException) {
                success = false
                Log.e("ERROR: IOException", "An error has occurred")
            }
        }
        WordsList = returnlist
    }
    //calculate the value in an inline function instead as this long ass function will be used more than once
    inline fun GetStringValue(word: String): Long{
        var value = 1L
        word.forEach {
            when(it.toUpperCase()){
                'A' -> value *= 2
                'B' -> value *= 3
                'C' -> value *= 5
                'D' -> value *= 7
                'E' -> value *= 11
                'F' -> value *= 13
                'G' -> value *= 17
                'H' -> value *= 19
                'I' -> value *= 23
                'J' -> value *= 29
                'K' -> value *= 31
                'L' -> value *= 37
                'M' -> value *= 41
                'N' -> value *= 43
                'O' -> value *= 47
                'P' -> value *= 53
                'Q' -> value *= 59
                'R' -> value *= 61
                'S' -> value *= 67
                'T' -> value *= 71
                'U' -> value *= 73
                'V' -> value *= 79
                'W' -> value *= 83
                'X' -> value *= 89
                'Y' -> value *= 97
                'Z' -> value *= 101
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
            if(it.Word.length == wd.Word.length && wd.WordValue == it.WordValue){
                AnagramsList.add(it.Word)
            }
        }
    }

}
data class WordData(var Word: String, var WordValue: Long)