package com.github.naixx.mo

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.SHOW_AS_ACTION_ALWAYS
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private lateinit var model: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val image = findViewById<ImageView>(R.id.image)

        model = ViewModelProviders.of(this).get(ViewModel::class.java)
        model.image.observe(this, Observer {
            image.setImageBitmap(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(Menu.NONE, 42, Menu.NONE, "Item name")?.apply {
            setIcon(R.drawable.ic_refresh)
            setShowAsAction(SHOW_AS_ACTION_ALWAYS)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        42   -> model.refresh()
        else -> null
    } != null || super.onOptionsItemSelected(item)
}
