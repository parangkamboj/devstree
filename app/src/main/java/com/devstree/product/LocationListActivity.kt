package com.devstree.product

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.devstree.product.adapter.AllLocationAdapter
import com.devstree.product.databinding.ActivityLocationListBinding
import com.devstree.product.databinding.SortBottomSheetBinding
import com.devstree.product.model.AddressSuggestModel
import com.devstree.product.model.PathData
import com.devstree.product.model.TransferPathList
import com.devstree.product.roomdb.AppDataBase
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationListBinding
    private lateinit var appDb: AppDataBase
    private lateinit var allLocationAdapter: AllLocationAdapter
    private var listModel: List<AddressSuggestModel>? = null
    private var transferData: TransferPathList? = null
    private lateinit var sortBottomSheetBinding: SortBottomSheetBinding
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Reload or refresh data here
            setLayoutAllLocationList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_location_list)

        binding = DataBindingUtil.setContentView(
            this@LocationListActivity,
            R.layout.activity_location_list
        )

        appDb = AppDataBase.getDatabase(this)

        setLayoutAllLocationList()

        clickEvents()
    }

    private fun getAllDataFromList() {
        listModel = null
        lifecycleScope.launch {
            val users = withContext(Dispatchers.IO) {
                appDb.Dao().getAllUsers()
            }
            allLocationAdapter.setList(users)
            listModel = users
            Log.d("Parang", "getAllDataFromList: ${users}")

        }
    }

    private fun setLayoutAllLocationList() {
        val linearLayout = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.locationRv.layoutManager = linearLayout
        allLocationAdapter =
            AllLocationAdapter(
                this,
                object : AllLocationAdapter.OnClickListener {
                    override fun onDelete(id: Int) {
                        openConfirmationDeleteDialog(id)
                    }

                    override fun onUpdate(data: AddressSuggestModel) {
                        if (listModel != null){
                            if (listModel?.size!! > 0){
                                var transferList = ArrayList<PathData>()
                                transferList.clear()
                                transferData = null
                                transferList.add(
                                    PathData(
                                        lat = listModel!![0].latitude,
                                        long = listModel!![0].longitude
                                    )
                                )
                                transferData = TransferPathList(list = transferList)
                            }
                        }
                        val intent = Intent(this@LocationListActivity, MainActivity::class.java).apply {
                            putExtra("id", data.id)
                            putExtra("DistanceData", transferData)
                        }
                        launcher.launch(intent)
                    }

                })
        binding.locationRv.adapter = allLocationAdapter

        getAllDataFromList()
    }

    private fun openConfirmationDeleteDialog(userId: Int) {
        val builder1 = AlertDialog.Builder(this)
        builder1.setMessage("Are you sure you want to delete?")
        builder1.setCancelable(true)

        builder1.setPositiveButton(
            "Yes"
        ) { dialog, id ->
            lifecycleScope.launch {
                appDb.Dao().deleteUserById(userId)

            }
            getAllDataFromList()
            dialog.cancel()
        }

        builder1.setNegativeButton(
            "No"
        ) { dialog, id -> dialog.cancel() }

        val alert11 = builder1.create()
        alert11.show()
    }

    private fun clickEvents() {
        binding.apply {
            btnAddPoi.setOnClickListener {
                val intent = Intent(this@LocationListActivity, MainActivity::class.java).apply {
                    if (listModel != null){
                        if (listModel?.size!! > 0){
                            var transferList = ArrayList<PathData>()
                            transferList.clear()
                            transferData = null
                            transferList.add(
                                PathData(
                                    lat = listModel!![0].latitude,
                                    long = listModel!![0].longitude
                                )
                            )
                            transferData = TransferPathList(list = transferList)
                            putExtra("DistanceData", transferData)
                        }
                    }
                }
                launcher.launch(intent)
            }
            btnNavigate.setOnClickListener {
                if (listModel != null) {
                    if (listModel?.size!! > 1) {
                        var transferList = ArrayList<PathData>()
                        transferList.clear()
                        transferData = null
                        for (i in 0..listModel?.size!! - 1) {
                            transferList.add(
                                PathData(
                                    name = listModel!![i].placeName,
                                    lat = listModel!![i].latitude,
                                    long = listModel!![i].longitude
                                )
                            )
                        }
                        transferData = TransferPathList(list = transferList)
                        val intent = Intent(this@LocationListActivity, MainActivity::class.java).apply {
                            putExtra("Data", transferData)
                        }
                        launcher.launch(intent)

                    } else {
                        Toast.makeText(
                            this@LocationListActivity,
                            "Please add atleast two Place",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            tvSort.setOnClickListener {
                openSortBS()
            }

        }
    }

    private fun openSortBS() {
        sortBottomSheetBinding =
            SortBottomSheetBinding.inflate(layoutInflater, null)

        bottomSheetDialog =
            BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(sortBottomSheetBinding.root)

        sortBottomSheetBinding.apply {
            btnSortApply.setOnClickListener {
                val selectedId = radioGroupSort.checkedRadioButtonId
                listModel = when (selectedId) {
                    R.id.radioAsc -> listModel
                        ?.sortedWith(compareBy<AddressSuggestModel?>(
                            { it?.distance == null },
                            { it?.distance }
                        ))
                    R.id.radioDesc -> listModel
                        ?.sortedWith(compareBy<AddressSuggestModel?>(
                            { it?.distance == null },
                        ).reversed())

                    else -> listModel
                }
                allLocationAdapter.setList(listModel)
                bottomSheetDialog.dismiss()
            }
        }


        bottomSheetDialog.show()
    }
}