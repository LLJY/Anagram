package com.lucas.anagram

import android.provider.UserDictionary
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class MainViewModel : ViewModel() {
    var RandomString = ""
    var WordsList = ArrayList<String>()
    fun generateChars(): String {
        var alphabets = "QWERTYUIOPASDFGHJKLZXCVBNM"
        var returnstring = ""
        for (i in 1..10) {
            var rnd = (0..alphabets.length - 1).random()
            var character = alphabets.get(rnd)
            returnstring = "$returnstring$character"
            alphabets.drop(rnd)
        }
        return returnstring
    }
    //Get the dictionary full of words and put them into a list accessible via the view model
    fun GetFile(timeout: Int) {
        var returnlist = ArrayList<String>()
        val end = System.currentTimeMillis()+ timeout //add timeout to current time
        var success = false
        while(System.currentTimeMillis() < end || !success) { //loop until timeout is reached OR function was successfully executed
            val url = URL("https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt")
            try {
                val input = BufferedReader(InputStreamReader(url.openStream()))
                returnlist = ArrayList(input.lines().collect(Collectors.toList()).toMutableList())
                success = true
            } catch (e: IOException) {
                success = false
                Log.e("ERROR: IOException", "An error has occurred")
            }
        }
        WordsList = returnlist
    }
    fun GetAnagrams(word: String) {
        WordsList.forEach{

        }

    }
}