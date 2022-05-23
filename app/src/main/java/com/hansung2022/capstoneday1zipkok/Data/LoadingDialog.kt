package com.hansung2022.capstoneday1zipkok.Data

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.hansung2022.capstoneday1zipkok.R

class LoadingDialog(val mActivity : Activity){

    private lateinit var isdialog:AlertDialog

    fun startLoading(){
        val infalter = mActivity.layoutInflater
        val dialogView = infalter.inflate(R.layout.dialog_loading,null)

        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isdialog = builder.create()
        isdialog.show()
    }
    fun isDismiss(){
        isdialog.dismiss()
    }


}
