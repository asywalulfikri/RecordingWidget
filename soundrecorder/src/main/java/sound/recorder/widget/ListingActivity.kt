package sound.recorder.widget

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import sound.recorder.widget.db.AppDatabase
import sound.recorder.widget.db.AudioRecord
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sound.recorder.widget.databinding.ActivityListingBinding


internal class ListingActivity : AppCompatActivity(), Adapter.OnItemClickListener {
    private lateinit var adapter : Adapter
    private lateinit var audioRecords : List<AudioRecord>
    private lateinit var db : AppDatabase
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var menu : Menu
    private var allSelected = false
    private var nbSelected = 0
    private lateinit var binding: ActivityListingBinding
    private val colorDisabled = "#CFCFCF"
    private val colorText = "#2B2B2B"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        audioRecords = emptyList()
        adapter = Adapter(audioRecords, this)

        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "audioRecords")
            //.fallbackToDestructiveMigration()
            .build()

        fetchAll()

        binding.searchInput.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                var query = p0.toString()
                searchDatabase("%$query%")
            }

        })

        binding.btnSelectAll.setOnClickListener {
            allSelected = !allSelected
            Log.d("ListingTag", allSelected.toString())
            audioRecords.forEach {
                it.isChecked = allSelected
            }

            nbSelected = if (allSelected) audioRecords.size else 0
            updateBottomSheet()

            adapter.notifyDataSetChanged()
        }

        binding.btnClose.setOnClickListener {
            closeEditor()
        }

        binding.btnDelete.setOnClickListener {
            closeEditor()
            var toDelete : List<AudioRecord> = audioRecords.filter { it.isChecked }
            audioRecords = audioRecords.filter { !it.isChecked }
            GlobalScope.launch {
                db.audioRecordDAO().delete(toDelete)
                if(audioRecords.isEmpty())
                    fetchAll()
                else
                    adapter.setData(audioRecords)
            }
        }

        binding.btnRename.setOnClickListener {
            Toast.makeText(this, "rename clicked", Toast.LENGTH_SHORT).show()
        }

    }

    private fun closeEditor(){
        allSelected = false
        adapter.setEditMode(false)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // hide back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        // show relative layout
        binding.editorBar.visibility = View.GONE
        nbSelected = 0
    }

    private fun fetchAll(){
        GlobalScope.launch {
            audioRecords = db.audioRecordDAO().getAll()
            adapter.setData(audioRecords)
        }
    }

    private fun searchDatabase(query: String){
        GlobalScope.launch {
            audioRecords = db.audioRecordDAO().searchDatabase(query)
            runOnUiThread{
                adapter.setData(audioRecords)
            }
        }
    }

    private fun updateBottomSheet(){
        when(nbSelected){
            0 -> {
                binding.btnRename.isClickable = false
                binding.btnRename.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_edit_disabled, theme)
                binding.tvRename.setTextColor(Color.parseColor(colorDisabled))
                binding.btnDelete.isClickable = false
                binding.btnDelete.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_delete_disabled2, theme)
                binding.tvDelete.setTextColor(Color.parseColor(colorDisabled))

            }
            1 -> {
                binding.btnRename.isClickable = true
                binding.btnRename.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_edit, theme)
                binding.tvRename.setTextColor(Color.parseColor(colorText))

                binding.btnDelete.isClickable = true
                binding.btnDelete.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, theme)
                binding.tvDelete.setTextColor(Color.parseColor(colorText))

            }
            else -> {
                binding.btnRename.isClickable = false
                binding.btnRename.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_edit_disabled, theme)
                binding.tvRename.setTextColor(Color.parseColor(colorDisabled))

                binding. btnDelete.isClickable = true
                binding.btnDelete.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, theme)
                binding.tvDelete.setTextColor(Color.parseColor(colorText))

            }
        }
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this, PlayerActivity::class.java)
        val audioRecord = audioRecords[position]

        if(adapter.isEditMode()){
            Log.d("ITEMCHANGE", audioRecord.isChecked.toString())
            audioRecord.isChecked = !audioRecord.isChecked
            adapter.notifyItemChanged(position)

            nbSelected = if (audioRecord.isChecked) nbSelected+1 else nbSelected-1
            updateBottomSheet()

        }else{
            intent.putExtra("filepath", audioRecord.filePath)
            intent.putExtra("filename", audioRecord.filename)
            startActivity(intent)
        }

    }

    override fun onItemLongClick(position: Int) {
        adapter.setEditMode(true)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val audioRecord = audioRecords[position]

        audioRecord.isChecked = !audioRecord.isChecked

        nbSelected = if (audioRecord.isChecked) nbSelected+1 else nbSelected-1
        updateBottomSheet()

        // hide back button
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        // show relative layout
        binding.editorBar.visibility = View.VISIBLE


    }

}