package com.ridwanharts.qrcodebarcodescanner

data class ResultScan(
    val id: Long?,
    val title: String,
    val result: String,
    val date: String
){
    companion object{
        const val TABLE_RESULT: String = "table_result"
        const val ID: String = "id_"
        const val TITLE: String = "title"
        const val RESULT: String = "result"
        const val DATE: String = "date"
    }


}