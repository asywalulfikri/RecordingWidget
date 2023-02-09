package sound.recorder.widget.ui.activity

import android.content.ActivityNotFoundException
import android.content.ComponentCallbacks2
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import sound.recorder.widget.R
import sound.recorder.widget.adapter.VideoAdapter
import sound.recorder.widget.databinding.ActivityListVideoBinding
import sound.recorder.widget.model.Video
import sound.recorder.widget.model.VideoWrapper
import java.util.*

class ListVideoActivity : AppCompatActivity(), VideoAdapter.OnItemClickListener {
    private var firebaseFirestore: FirebaseFirestore? = null

    //private LoadingLayout mLoadingLayout;
    private var mAdapter: VideoAdapter? = null
    private var mVideoList = ArrayList<Video>()
    private var footerView: View? = null
    private var rlLoadMore: RelativeLayout? = null
    private var progressBar: ProgressBar? = null
    private var tvLoadMore: TextView? = null
    private var mPage = 1
    private var mLayoutManager: LinearLayoutManager? = null
    private lateinit var binding : ActivityListVideoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseFirestore = FirebaseFirestore.getInstance()
        mLayoutManager = LinearLayoutManager(this)

        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.setHasFixedSize(true)

        footer()
        load(false)
    }

    private fun footer() {
        footerView  = LayoutInflater.from(this@ListVideoActivity).inflate(R.layout.item_footer, binding.recyclerView, false)
        rlLoadMore  = footerView?.findViewById(R.id.rl_load_more)
        progressBar = footerView?.findViewById(R.id.pb_progress)
        tvLoadMore  = footerView?.findViewById(R.id.tv_loading)
        rlLoadMore?.setOnClickListener(View.OnClickListener { v: View? ->
            progressBar!!.visibility = View.VISIBLE
            tvLoadMore!!.text = "loading"
            load(true)
        })
        rlLoadMore?.setOnClickListener(View.OnClickListener {
            progressBar?.visibility = View.VISIBLE
            tvLoadMore?.text = "loading"
            mPage++
            load(true)
        })
    }

    private fun load(loadMore: Boolean) {
        firebaseFirestore!!.collection("videos")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val wrapper = VideoWrapper()
                    wrapper.list = ArrayList()
                    var rowList = 1
                    for (doc in task.result!!) {
                        if (rowList <= mPage * 10 && rowList > (mPage - 1) * 10) {
                            val video = Video()
                            video.datepublish = doc.getString("datepublish")
                            video.description = doc.getString("description")
                            video.thumbnail = doc.getString("thumbnail")
                            video.url = doc.getString("url")
                            video.title = doc.getString("title")
                            Log.d("title", video.url + "-")
                            wrapper.list!!.add(video)
                        }
                        rowList++
                    }
                    if (wrapper.list!!.size != 0) {
                        result(wrapper, loadMore)
                        mAdapter!!.notifyDataSetChanged()
                        if (task.result!!.size() == wrapper.list!!.size) {
                            mAdapter!!.removeFooter()
                        }
                    } else if (task.result!!.size() == 0) {
                        Toast.makeText(
                            this@ListVideoActivity,
                            "No data found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ListVideoActivity,
                        "Gagal mengambil data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun loadMoreView(wrapper: VideoWrapper) {
        progressBar?.visibility = View.GONE
        tvLoadMore?.text = "Next"
        mAdapter?.addItem(wrapper.list)
        mAdapter?.notifyDataSetChanged()
    }

    private fun result(wrapper: VideoWrapper?, loadMore: Boolean) {
        if (wrapper != null) {
            Log.e("gg2", "mm")
            if (wrapper.list!!.size == 0) {
                Log.e("gg3", "mm")
                Toast.makeText(this@ListVideoActivity, "No data found", Toast.LENGTH_SHORT).show()
            } else {
                if (loadMore) {
                    loadMoreView(wrapper)
                } else {
                    mVideoList = ArrayList()
                    updateList(wrapper)
                }
                for (i in wrapper.list!!.indices) {
                    mVideoList.add(wrapper.list!![i])
                    if (i < 8) {
                        footerView!!.visibility = View.GONE
                    }
                    if (i > 8) {
                        footerView!!.visibility = View.VISIBLE
                    }
                }
                if (loadMore) {
                    mPage += 1
                }
                showList()
            }
        } else {
            Log.e("gg4", "mm")
            Toast.makeText(this@ListVideoActivity, "No data found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateList(wrapper: VideoWrapper) {
        showList()
        //        mPage = 1 + mPage;
        rlLoadMore!!.visibility = View.VISIBLE
        progressBar!!.visibility = View.GONE
        tvLoadMore!!.text = "Next"
        mAdapter = VideoAdapter(this@ListVideoActivity)
        mAdapter?.data = wrapper.list
        binding.recyclerView.adapter = mAdapter
        mAdapter?.setClickListener { view, position ->
            val video = mVideoList[position]
            val appIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + video.url))
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + video.url)
            )
            try {
                startActivity(appIntent)
            } catch (ex: ActivityNotFoundException) {
                startActivity(webIntent)
            }
        }
        mAdapter?.setFooterView(footerView)
        mAdapter?.notifyDataSetChanged()
    }

    private fun showList() {
        binding.recyclerView.visibility = View.VISIBLE
    }

    override fun onItemClick(view: View, position: Int) {}
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        // Determine which lifecycle or system event was raised.
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE, ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW, ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
            }
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND, ComponentCallbacks2.TRIM_MEMORY_MODERATE, ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
            }
            else -> {
            }
        }
    }
}