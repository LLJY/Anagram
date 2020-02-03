package com.lucas.anagram

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import kotlin.collections.ArrayList
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class MainViewModel : ViewModel() {
    var RandomString = ""
    var WordsList = ArrayList<WordData>()
    var AnagramsList = ArrayList<String>()
    var ElapsedTime = 0L
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
    suspend fun GetFile(timeout: Int): Boolean {
        var returnlist = ArrayList<WordData>()
        val url = URL("https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt")
        try {
            val input = BufferedReader(InputStreamReader(url.openStream()))
            var list = ArrayList<String>()
            input.lines().forEach{
                list.add(it)
            }
            val nproc = Runtime.getRuntime().availableProcessors()
            //split list into nproc x 2 number of lists
            val parallellist = ArrayList(list.chunked(list.size/(nproc*2)))
            var asynclist = ArrayList<Deferred<List<WordData>>>()
            parallellist.forEach{
                asynclist.add(viewModelScope.async {
                    GenerateWordsList(ArrayList(it))
                })
            }
            asynclist.forEach {
                returnlist.addAll(it.await())
            }
            WordsList = returnlist
            return true
        } catch (e: IOException) {
            Log.e("ERROR: IOException", "An error has occurred")
        }
        WordsList = returnlist
        return false
    }
    fun GenerateWordsList(wordsList: ArrayList<String>) : ArrayList<WordData>{
        var returnlist = ArrayList<WordData>()
        wordsList.forEach {
            returnlist.add(WordData(it, GetStringValue(it)))
        }
        return returnlist
    }
    //calculate the value in an inline function instead as this long ass function will be used more than once
    inline fun GetStringValue(word: String): Long{
        var value = 1L
        var prev = 0L
        for(it in word){
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
            if(value >= prev){
                prev = value
            }else{
                /*The only reason than value will not be more or equal to  he previous value is if there is an overflow situation as some words may exceed the 64bit Integer limit.
                * set the value to -1 and we will deal with this case separately with a different fallback method in GetAnagrams()
                 */
                value = -1
                break
            }
        }
        return value
    }

    /***
     * the purpose of the function should be self explanatory if you understood the documentation above.
     * If the word's value is equal to the anagram's value, they are both anagrams and we will add it to the list.
     * We will run this function asynchronously as it is rather heavy due to having to loop through thousands of words im the provided list.
     */
    suspend fun GetAnagrams(word: String) {
        val wd = WordData(word, GetStringValue(word))
        ElapsedTime = 0
        //clear the list before starting
        AnagramsList.clear()
        val time = measureTimeMillis {
            //if wordvalue is -1, it means it will fall back to manual string comparison, which will benefit from the extra threads.
            if(wd.WordValue == -1L) {
                val nproc = Runtime.getRuntime().availableProcessors()
                //split list into nproc x 2 number of lists
                val parallellist = ArrayList(WordsList.chunked(WordsList.size / (nproc * 2)))
                //We create this list to await it later on.
                var asynclist = ArrayList<Deferred<List<String>>>()
                parallellist.forEach {
                    //spawn tasks to find anagrams in every list, should speed things up slightly
                    asynclist.add(viewModelScope.async {
                        findAnagrams(wd, it)
                    })
                }
                asynclist.forEach {
                    AnagramsList.addAll(it.await())
                }
                //Normal Arithmetic is actually slower with coroutines, just do it normally.
            }else{
                AnagramsList = findAnagrams(wd, WordsList)
            }

            AnagramsList.sortBy { it.length }
        }
        ElapsedTime = time

    }
    fun findAnagrams(wd: WordData, wordsList: List<WordData>): ArrayList<String>{
        var returnlist = ArrayList<String>()
        wordsList.forEach{
            //DO NOT bother calculating any string that is larger
            //if divisible by the word, it means it contains the same prime numbers(prime factorisation), hence is an anagram
            if(it.WordValue > -1 && wd.WordValue > -1) {
                if (it.WordValue <= wd.WordValue && (wd.WordValue % it.WordValue) == 0L) {
                    returnlist.add(it.Word)
                }
            }else{
                /*Fallback to brute forcing by sorting the strings and running .contains().
                * This circumvents the need for BigInteger as we now have two methods to match the string, the prime method is faster than string operations, however
                * The string operation is still much faster than using BigInteger, which is rather expensive. So take it as a compromise.
                 */
                if(it.Word.length <= wd.Word.length && wd.Word.toCharArray().sorted().containsAll(it.Word.toUpperCase().toCharArray().sorted())){
                    returnlist.add(it.Word)
                }
            }
        }
        return returnlist
    }

}
data class WordData(var Word: String, var WordValue: Long)