package com.lucas.anagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var model = ViewModelProvider(this).get(MainViewModel::class.java)
        showProgressCircle(false)
        //show a grid of 3
        my_recycler_view.layoutManager = GridLayoutManager(this, 3)
        GlobalScope.launch {
            DownloadListOfString(model)
        }
        charTextbox.setText(model.RandomString)
        randomButton.setOnClickListener{
            model.RandomString = model.generateChars()
            charTextbox.setText(model.RandomString)
        }
        generateButton.setOnClickListener{
            if(!charTextbox.text.isNullOrEmpty()){
                var word = charTextbox.text.toString() //golden rule of android is thou shalt not do ui operations on a seperate thread
                GlobalScope.launch(Dispatchers.Main) {
                    showProgressCircle(true)
                    val asyncTask = async(Dispatchers.Default){
                        model.GetAnagrams(word)
                    }
                    asyncTask.await()
                    my_recycler_view.adapter = RecyclerAdapter(model.AnagramsList, this@MainActivity)
                    //only stop progress circle after bind to feel more seamless.
                    showProgressCircle(false)
                }
            }
        }

    }
    private inline fun showProgressCircle(bool: Boolean){
        //hide and show certain ui elements when progress circle is active
        my_recycler_view.isVisible = !bool
        progress_circular.isVisible = bool
        displaytext.isVisible = bool
        generateButton.isVisible = !bool
        randomButton.isVisible = !bool
    }
    suspend fun DownloadListOfString(model : MainViewModel){
        //launch coroutine from main to update progress circle
        GlobalScope.launch(Dispatchers.Main){
            //show progress circle when executing async task
            showProgressCircle(true)
            //launch with IO Dispatcher as it is more suitable for network and I/O related tasks
            val asynctask = async(Dispatchers.IO){
                model.GetFile(10)
            }
            //asynchronously wait for task
            asynctask.await()
            //do not show progress circle after task has finished
            showProgressCircle(false)
        }

    }

}
