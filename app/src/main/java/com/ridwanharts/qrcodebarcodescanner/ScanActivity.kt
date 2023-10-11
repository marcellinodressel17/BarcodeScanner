package com.ridwanharts.qrcodebarcodescanner

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_scan.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.jetbrains.anko.db.insert
import java.text.SimpleDateFormat
import java.util.*

class ScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var mScannerView: ZXingScannerView? = null
    private var mSelectedIndices: ArrayList<Int>? = null
    private var mFlash = false
    private var mAutoFocus = false
    private var mCameraId = -1

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_scan)

        init(state)
        checkPermissions()
        buttonControl()

    }

    private fun init(state: Bundle?) {
        if (state != null) {
            mFlash = state.getBoolean(FLASH_STATE, false)
            mAutoFocus = state.getBoolean(AUTO_FOCUS_STATE, true)
            mSelectedIndices = state.getIntegerArrayList(SELECTED_FORMATS)
            mCameraId = state.getInt(CAMERA_ID, -1)
        }else{
            try {
                mFlash = false;
                mAutoFocus = true;
                mSelectedIndices = null;
                mCameraId = -1;
                mScannerView = barcode
            }catch (e: RuntimeException){
                Log.e("TAG", "$e")
            }
        }
    }

    private fun buttonControl(){

        flash_camera.setOnClickListener {
            val flashCamera = findViewById<ImageButton>(R.id.flash_camera)
            if (mFlash){
                mFlash = false
                flashCamera.setImageDrawable(resources.getDrawable(R.drawable.ic_flash_off))
                mScannerView?.flash = mFlash
            }else{
                mFlash = true
                flashCamera.setImageDrawable(resources.getDrawable(R.drawable.ic_flash))
                mScannerView?.flash = mFlash
            }
        }

        change_camera.setOnClickListener {
            val drawable = findViewById<ImageButton>(R.id.change_camera)
            if (mCameraId == -1){
                mCameraId = 1
                drawable.setImageDrawable(resources.getDrawable(R.drawable.ic_change_camera_rot))
                mScannerView?.startCamera(mCameraId)
            }else{
                mCameraId = -1
                drawable.setImageDrawable(resources.getDrawable(R.drawable.ic_change_camera))
                mScannerView?.startCamera(mCameraId)
            }
        }

        auto_focus.setOnClickListener {
            val auto = findViewById<ImageButton>(R.id.auto_focus)
            if (mAutoFocus){
                mAutoFocus = false
                auto.setImageDrawable(resources.getDrawable(R.drawable.ic_focus_off))
                mScannerView?.setAutoFocus(mAutoFocus)
            }else{
                mAutoFocus = true
                auto.setImageDrawable(resources.getDrawable(R.drawable.ic_auto_focus))
                mScannerView?.setAutoFocus(mAutoFocus)
            }
        }
    }

    private fun checkPermissions() {
        if (allPermissionsGranted()) {

            setupFormats()
            mScannerView!!.setAspectTolerance(0.5f);
            mScannerView!!.setResultHandler(this)

        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

                mScannerView!!.setAspectTolerance(0.5f);
                setupFormats()
                mScannerView!!.setResultHandler(this)

            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                //finish()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {

        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun handleResult(rawResult: Result?) {
        try {
            val notification =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {

            showMessageDialog("Error = $e", "")
        }

        showMessageDialog(rawResult!!.text, rawResult.barcodeFormat.toString())

    }

    private fun showMessageDialog(message: String?, format: String) {

        var title = ""
        val uri = Uri.parse(message)
        val formatDate = SimpleDateFormat("hh:mm dd/M/yyyy")
        val currentDate = formatDate.format(Date())

        //check if uri is valid URL or not
        if (Patterns.WEB_URL.matcher(uri.toString()).matches()){
            title = "LINK"
        }else{
            title = "TEXT"
            //uri = Uri.parse("http://$uri")
            //URLUtil.isValidUrl(url)
        }


        val intent = Intent(Intent.ACTION_VIEW, uri)

        mScannerView?.stopCameraPreview()

        database.use{
            insert(ResultScan.TABLE_RESULT,
                ResultScan.TITLE to title,
                ResultScan.RESULT to uri.toString(),
                ResultScan.DATE to currentDate.toString()

            )
        }

        val mDialog: MaterialDialog = MaterialDialog.Builder(this)
            .setTitle("Hasil Scan")
            .setMessage(message!!)
            .setCancelable(false)
            .setPositiveButton("Buka", R.drawable.ic_open_web) { dialogInterface, which ->
                try {
                    startActivity(intent)
                    mScannerView?.stopCameraPreview()
                } catch (e: ActivityNotFoundException) {

                    mScannerView!!.resumeCameraPreview(this)
                }
            }
            .setNegativeButton("Ulang", R.drawable.ic_focus_off) { dialogInterface, which ->
                dialogInterface?.dismiss()
                mScannerView!!.resumeCameraPreview(this)
            }
            .build()

        mDialog.show()
    }

    private fun setupFormats() {
        val formats: MutableList<BarcodeFormat> =
            ArrayList()
        if (mSelectedIndices == null || mSelectedIndices!!.isEmpty()) {
            mSelectedIndices = ArrayList<Int>()
            for (i in ZXingScannerView.ALL_FORMATS.indices) {
                mSelectedIndices!!.add(i)
            }
        }
        for (index in mSelectedIndices!!) {
            formats.add(ZXingScannerView.ALL_FORMATS[index])
        }
        if (mScannerView != null) {
            mScannerView!!.setFormats(formats)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(FLASH_STATE, mFlash)
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus)
        outState.putIntegerArrayList(SELECTED_FORMATS, mSelectedIndices)
        outState.putInt(CAMERA_ID, mCameraId)
    }

    override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this)
        mScannerView!!.startCamera(mCameraId)
        mScannerView!!.flash = mFlash
        mScannerView!!.setAutoFocus(mAutoFocus)
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val FLASH_STATE = "FLASH_STATE"
        private const val AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE"
        private const val SELECTED_FORMATS = "SELECTED_FORMATS"
        private const val CAMERA_ID = "CAMERA_ID"
    }

}