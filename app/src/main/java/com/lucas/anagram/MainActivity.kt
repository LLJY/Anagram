package com.lucas.anagram

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        setContentView(R.layout.activity_main)
        var model = ViewModelProvider(this).get(MainViewModel::class.java)
        showProgressCircle(false, "")
        var RA = RecyclerAdapter(model.AnagramsList, this@MainActivity)
        //show a grid of 2(or 3 when landscape) and bind recycler adapter
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            my_recycler_view.layoutManager = GridLayoutManager(this, 3)
        }else{
            my_recycler_view.layoutManager = GridLayoutManager(this, 2)
        }
        if(model.AnagramsList.size != 0 && model.ElapsedTime != 0L){
            displaytext.isVisible=true
            displaytext.text = "Elapsed Time(ms): ${model.ElapsedTime}"
        }
        my_recycler_view.adapter = RA
        //if the wordlist has not been downloaded, download it
        if(model.WordsList.size == 0) {
            //launch coroutine from main to update progress circle
            GlobalScope.launch(Dispatchers.Main){
                //show progress circle when executing async task
                showProgressCircle(true, "Downloading Dictionary...")
                //launch with IO Dispatcher as it is more suitable for network and I/O related tasks
                val asynctask = async(Dispatchers.IO){
                    model.GetFile(10)
                }
                //asynchronously wait for task
                if(asynctask.await()){
                    //do not show progress circle after task has finished
                    showProgressCircle(false, "")
                }else{
                    showProgressCircle(true, "An Error Has Occurred, Please Check Your Internet Connection")
                    progress_circular.isVisible = false
                    Toast.makeText(this@MainActivity, "An Error Has Occurred!!", Toast.LENGTH_LONG).show()
                }
            }
        }
        charTextbox.setText(model.RandomString)
        randomButton.setOnClickListener{
            //generate characters, and then set text to the textbox
            model.generateChars()
            charTextbox.setText(model.RandomString)
        }
        generateButton.setOnClickListener{
            if(!charTextbox.text.isNullOrEmpty()){
                var word = charTextbox.text.toString() //golden rule of android is thou shalt not do ui operations on a seperate thread
                GlobalScope.launch(Dispatchers.Main) {
                    showProgressCircle(true, "Finding Anagrams...")
                    val asyncTask = async(Dispatchers.Default){
                        model.GetAnagrams(word)
                    }
                    asyncTask.await()
                    //only stop progress circle after bind to feel more seamless.
                    RA = RecyclerAdapter(model.AnagramsList, this@MainActivity)
                    my_recycler_view.adapter = RA
                    showProgressCircle(false, "")
                    displaytext.isVisible = true
                    displaytext.text = "Elapsed Time(ms): ${model.ElapsedTime}"
                }
            }
        }

    }
    private inline fun showProgressCircle(bool: Boolean, message: String){
        //hide and show certain ui elements when progress circle is active
        my_recycler_view.isVisible = !bool
        progress_circular.isVisible = bool
        displaytext.isVisible = bool
        generateButton.isVisible = !bool
        randomButton.isVisible = !bool
        if(bool) {
            displaytext.text = message
        }
    }
}
