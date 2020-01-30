package com.lucas.anagram

import android.content.res.Configuration
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
        val RA = RecyclerAdapter(model.AnagramsList, this@MainActivity)
        //show a grid of 2(or 3 when landscape) and bind recycler adapter
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            my_recycler_view.layoutManager = GridLayoutManager(this, 3)
        }else{
            my_recycler_view.layoutManager = GridLayoutManager(this, 2)
        }
        my_recycler_view.adapter = RA
        if(model.WordsList.size == 0) {
            GlobalScope.launch {
                DownloadListOfString(model)
            }
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
                    //only stop progress circle after bind to feel more seamless.
                    RA.notifyDataSetChanged()
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
