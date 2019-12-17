package com.my.targetDemoApp

import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.my.target.nativeads.NativeAd
import com.my.target.nativeads.banners.NativePromoBanner
import com.my.target.nativeads.factories.NativeViewsFactory
import com.my.target.nativeads.views.ChatListAdView
import com.my.target.nativeads.views.ContentStreamAdView
import com.my.target.nativeads.views.ContentWallAdView
import com.my.target.nativeads.views.MediaAdView
import com.my.target.nativeads.views.NewsFeedAdView

class NativeHelper(val parent: View) : NativeAd.NativeAdListener {

    companion object {
        private const val NATIVE_AD_COUNT = 5
    }

    private val nativeList = androidx.collection.SparseArrayCompat<NativeAd>()
    private var loaded = false
    var recyclerView: RecyclerView? = null
    private var afterLoad: (() -> Unit)? = null
    private var afterNoad: (() -> Unit)? = null
    private var bar: Snackbar? = null
    private var adCalls: Int = 0

    fun createRecycler(viewType: NativeViewType) {
        if (nativeList.size() == 0) {
            Snackbar.make(parent, "NativeAd is not loaded", Snackbar.LENGTH_SHORT)
                    .show()
            return
        }

        recyclerView = RecyclerView(parent.context)
        recyclerView?.layoutManager =
                LinearLayoutManager(parent.context, LinearLayoutManager.VERTICAL, false)
        recyclerView?.adapter = NativeAdapter(nativeList, viewType)
    }

    fun load(slot: Int, view: View, afterLoad: (() -> Unit)? = null,
             afterNoAd: (() -> Unit)? = null) {
        this.afterLoad = afterLoad
        this.afterNoad = afterNoAd
        loaded = false
        nativeList.clear()
        if (afterLoad == null) showLoading(view)
        repeat(NATIVE_AD_COUNT) {
            val nativeAd = NativeAd(slot, parent.context)
            nativeAd.listener = this
            nativeAd.load()
        }
    }

    private fun showLoading(parent: View) {
        bar = Snackbar.make(parent, "Loading", Snackbar.LENGTH_INDEFINITE)
        bar?.let {
            val contentLay = it.view.findViewById<TextView>(R.id.snackbar_text).parent as ViewGroup
            val item = ProgressBar(parent.context)
            contentLay.addView(item, 0)
            it.show()
        }
    }

    override fun onLoad(banner: NativePromoBanner, nativeAd: NativeAd) {
        adCalls++
        nativeList.append(nativeList.size() * 4 + 4, nativeAd)
        if (adCalls >= NATIVE_AD_COUNT) {
            bar?.dismiss()
            loaded = true
            Snackbar.make(parent, "NativeAd is loaded", Snackbar.LENGTH_SHORT)
                    .show()
            afterLoad?.invoke()
        }
    }

    override fun onClick(nativeAd: NativeAd) {
    }

    override fun onVideoPlay(nativeAd: NativeAd) {
    }

    override fun onVideoPause(nativeAd: NativeAd) {
    }

    override fun onNoAd(s: String, nativeAd: NativeAd) {
        adCalls++
        if (adCalls >= NATIVE_AD_COUNT) {
            if (nativeList.isEmpty) {
                bar?.dismiss()
                Snackbar.make(parent, "No Ad", Snackbar.LENGTH_SHORT)
                        .show()
                afterNoad?.invoke()
            }
        }
    }

    override fun onVideoComplete(nativeAd: NativeAd) {
    }

    override fun onShow(nativeAd: NativeAd) {
    }

    class NativeViewHolder(var adView: View?, view: View, var aspectView: TextView?) :
            RecyclerView.ViewHolder(view)

    class NativeAdapter(private var nativeList: androidx.collection.SparseArrayCompat<NativeAd>,
                        private var nativeViewType: NativeViewType) :
            RecyclerView.Adapter<NativeViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NativeViewHolder {
            val view = FrameLayout(parent.context)
            var adView: View? = null
            var aspectTextView: TextView? = null

            if (viewType == 1) {
                adView = when (nativeViewType) {
                    NativeViewType.CONTENT_WALL -> {
                        val contentWallView = NativeViewsFactory.getContentWallView(view.context)
                        contentWallView
                    }
                    NativeViewType.CHAT_LIST    -> {
                        val chatListView = NativeViewsFactory.getChatListView(view.context)
                        chatListView
                    }
                    NativeViewType.NEWS_FEED    -> {
                        val newsFeedView = NativeViewsFactory.getNewsFeedView(view.context)
                        newsFeedView
                    }
                    else                        -> {
                        val contentStreamView =
                                NativeViewsFactory.getContentStreamView(view.context)
                        contentStreamView
                    }
                }
                view.addView(adView)
                aspectTextView = TextView(parent.context)
                aspectTextView.setTextColor(Color.RED)
                aspectTextView.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f,
                        parent.context.resources.displayMetrics)
                view.addView(aspectTextView)
            }
            else {
                val textView = TextView(parent.context)
                textView.setText(R.string.lorem)
                view.addView(textView)
            }

            return NativeViewHolder(adView, view, aspectTextView)
        }

        override fun getItemCount(): Int {
            return NATIVE_AD_COUNT * 4
        }

        override fun getItemViewType(position: Int): Int {
            if (nativeList[position] != null) {
                return 1
            }
            return 0
        }

        override fun onBindViewHolder(holder: NativeViewHolder, position: Int) {
            val ad = nativeList[position] ?: return
            holder.adView.let {
                when (nativeViewType) {
                    NativeViewType.CONTENT_WALL -> {
                        (it as ContentWallAdView).setupView(ad.banner)
                    }
                    NativeViewType.CHAT_LIST    -> {
                        (it as ChatListAdView).setupView(ad.banner)
                        it.loadImages()
                    }
                    NativeViewType.NEWS_FEED    -> {
                        (it as NewsFeedAdView).setupView(ad.banner)
                        it.loadImages()
                    }
                    else                        -> {
                        (it as ContentStreamAdView).setupView(ad.banner)
                        it.loadImages()
                    }
                }
                ad.registerView(it)
                val findViewById = holder.adView?.findViewById<View>(R.id.nativeads_media_view)
                val mediaAdView = findViewById as? MediaAdView
                val text = "Aspect: ${mediaAdView?.mediaAspectRatio?.toString() ?: "No aspect"}"
                holder.aspectView?.text = text

            }
        }

        override fun onViewRecycled(holder: NativeViewHolder) {
            super.onViewRecycled(holder)
            val nativeAd: NativeAd? = nativeList.get(holder.adapterPosition)
            nativeAd?.unregisterView()
        }
    }

    enum class NativeViewType {
        CONTENT_STREAM,
        CONTENT_WALL,
        NEWS_FEED,
        CHAT_LIST
    }
}