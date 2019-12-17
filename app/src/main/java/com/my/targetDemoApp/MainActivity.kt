package com.my.targetDemoApp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mainActivityAdapter: ItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        mainActivityAdapter = ItemsAdapter()
        main_recycler.adapter = mainActivityAdapter
        main_recycler.layoutManager = LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false)
        val callback = TouchCallback { mainActivityAdapter.deleteItem(it) }
        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(main_recycler)

        val types = ArrayList<ItemsAdapter.ListItem>()
        types.add(ItemsAdapter.ListItem({ goNative() }, getString(R.string.native_ads),
                getString(R.string.native_ads_desc)))
        val size = types.size
        callback.protectedTypesSize = size
        mainActivityAdapter.setItems(types)
    }

    private fun goNative(slot: Int? = null) {
        val intent = Intent(this, NativeActivity::class.java)
        if (slot != null) {
            intent.putExtra(NativeActivity.KEY_SLOT, slot)
        }
        startActivity(intent)
    }

    class TouchCallback(private val swipeListener: (position: Int) -> Unit) :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                    ItemTouchHelper.LEFT) {
        var protectedTypesSize: Int = 0

        override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder,
                            p2: RecyclerView.ViewHolder): Boolean {
            return true
        }

        override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
            swipeListener.invoke(p0.adapterPosition)
        }

        override fun getSwipeDirs(recyclerView: RecyclerView,
                                  viewHolder: RecyclerView.ViewHolder): Int {
            return if (viewHolder.adapterPosition < protectedTypesSize) {
                0
            }
            else {
                super.getSwipeDirs(recyclerView, viewHolder)
            }
        }
    }
}
