package com.example.st10298850_prog7313_p2_lp.utils

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.example.st10298850_prog7313_p2_lp.R

object ImagePreviewUtil {

    fun showImagePreviewDialog(context: Context, imagePath: String) {
        Log.d("ImagePreviewUtil", "Showing image preview dialog for path: $imagePath")
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_popup)

        val imageView: ImageView = dialog.findViewById(R.id.popupImageView)
        Log.d("ImagePreviewUtil", "Loading image into ImageView")
        val bitmap = BitmapFactory.decodeFile(imagePath)
        if (bitmap != null) {
            Log.d("ImagePreviewUtil", "Bitmap loaded successfully, size: ${bitmap.width}x${bitmap.height}")
            imageView.setImageBitmap(bitmap)
        } else {
            Log.e("ImagePreviewUtil", "Failed to load bitmap from path: $imagePath")
        }

        dialog.show()
        Log.d("ImagePreviewUtil", "Dialog shown")
    }

    fun showImagePreviewAlert(context: Context, imagePath: String) {
        val builder = AlertDialog.Builder(context)
        val imageView = ImageView(context)
        val bitmap = BitmapFactory.decodeFile(imagePath)
        imageView.setImageBitmap(bitmap)

        builder.setView(imageView)
        builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}