package com.ridwanharts.qrcodebarcodescanner

import android.view.View

interface RecylerViewClickListener {

    fun onRecylerViewItemClick(view: View, result: ResultScan, position: Int)

}