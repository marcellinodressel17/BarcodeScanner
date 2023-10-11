package com.ridwanharts.qrcodebarcodescanner

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_scan_result.view.*
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.select

class HomeActivity : AppCompatActivity(), RecylerViewClickListener {

    private var adapter: ResultAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        scan_now.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        showList()

    }

    private fun getListScanResult(): ArrayList<ResultScan>{
        var listResult: List<ResultScan>? = null

        database.use {
            val result = select(ResultScan.TABLE_RESULT)
            listResult = result.parseList(classParser<ResultScan>())
        }

        return ArrayList(listResult!!)
    }

    private fun showList(){
        val list = getListScanResult()
        if (list.isEmpty()){

            linear_empty.visibility = View.VISIBLE

        }else{
            if (linear_empty.visibility == View.VISIBLE){
                linear_empty.visibility = View.INVISIBLE
            }
            adapter = ResultAdapter(this, list, this)
            rv_list_result.adapter = adapter
            rv_list_result.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            rv_list_result.addItemDecoration(
                DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
            )

        }
    }

    override fun onResume() {
        super.onResume()
        showList()
    }

    override fun onRecylerViewItemClick(view: View, result: ResultScan, position: Int) {

        view.rootView.setOnClickListener {
            if (result.title == "LINK"){
                showDialogMenu(result.result)
            }
        }

        view.iv_popmenu.setOnClickListener {

            val popupMenu = PopupMenu(this, view.iv_copy)
            popupMenu.inflate(R.menu.menu_action)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.delete -> {
                        view.context.database.use{
                            delete(ResultScan.TABLE_RESULT,
                                "${ResultScan.ID} = {id}",
                                "id" to result.id!!.toInt()
                            )
                        }
                        adapter?.updateList(position)

                        if (adapter?.itemCount == 0){
                            linear_empty.visibility = View.VISIBLE
                        }
                        Toast.makeText(this, "Hapus ${adapter?.itemCount}", Toast.LENGTH_SHORT).show()
                    }
                    /*R.id.edit -> {
                        Toast.makeText(this, "Edit", Toast.LENGTH_SHORT).show()
                    }*/
                }
                true
            }

        }

        view.iv_copy.setOnClickListener {

            val copyToClipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val copyText = ClipData.newPlainText("", result.result)
            copyToClipboard.setPrimaryClip(copyText)

            Toast.makeText(this, "Text Berhasil Disalin", Toast.LENGTH_SHORT).show()

        }
    }

    private fun showDialogMenu(link: String){

        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val mDialog: MaterialDialog = MaterialDialog.Builder(this)
            .setTitle("Buka Link")
            .setMessage(link)
            .setCancelable(true)
            .setPositiveButton("Buka", R.drawable.ic_open_web) { dialogInterface, which ->
                try {
                    startActivity(intent)
                    dialogInterface.dismiss()
                } catch (e: ActivityNotFoundException) {
                    dialogInterface.dismiss()
                }
            }
            .setNegativeButton("Batal", R.drawable.ic_cancel) { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            .build()

        mDialog.show()
    }
}