package com.example.gmttest.managers

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.example.gmttest.databinding.ActivityMainBinding
import com.example.gmttest.models.Point
import com.example.gmttest.utils.getOppositeAverageColor
import com.example.gmttest.utils.hideKeyboard
import com.example.gmttest.utils.myDrawPath
import com.example.gmttest.utils.myDrawTextOnPath
import com.example.gmttest.utils.saveToGallery
import com.example.gmttest.utils.showKeyboard
import com.example.gmttest.utils.smooth
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog

class TextBrush(private val context: AppCompatActivity, private val binding: ActivityMainBinding) {

    private var painting = false
        set(value) {
            field = value
            onPainting(value)
        }
    private var typing = false
    private var path: MutableList<Point> = mutableListOf()
    private var myColor: Int = Color.WHITE
    private var mySize: Float = 50f
    private var bitmapStack: ArrayDeque<Bitmap> = ArrayDeque()
    private var paintingBitmap: Bitmap? = null
    private var typingBitmap: Bitmap? = null
    private var colorPicker: ColorPickerDialog? = null
    private var cursorThread: Thread? = null
    private var cursorRunnable: Runnable? = null
    private var input: SeekBar? = null
    private var textDialog: AlertDialog? = null

    init {
        colorPicker = ColorPickerDialog()
            .withColor(myColor)
            .withAlphaEnabled(false)
            .withListener { _, newColor ->
                myColor = newColor
                binding.buttonBottomLeft.setBackgroundColor(myColor)
                onPaintEditingBitmap()
            }

        input = SeekBar(context).apply {
            max = 200
            min = 20
            progress = mySize.toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    mySize = input?.progress.toString().toFloat()
                    textDialog?.setMessage("Choose the text size, current is ${mySize.toInt()}")
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
        }

        textDialog = AlertDialog.Builder(context)
            .setTitle("Text Size")
            .setMessage("Choose the text size, current is ${mySize.toInt()}")
            .setView(input)
            .setPositiveButton("OK") { _, _ -> onPaintEditingBitmap() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()

        cursorRunnable = Runnable {
            try {
                var addCursor = false
                while(!Thread.currentThread().isInterrupted) {
                    addCursor = !addCursor
                    onPaintEditingBitmap(addCursor)
                    Thread.sleep(500) }
            }
            catch (_: Exception) {}
        }
    }

    fun onFirstLoad() {
        val bitmap = binding.imageView.drawToBitmap()
        if (bitmapStack.isEmpty()) bitmapStack.add(bitmap)
        val color = bitmap.getOppositeAverageColor()
        binding.buttonTopLeft.setColorFilter(color)
        binding.buttonTopRight.setColorFilter(color)
        binding.buttonBottomLeft.setColorFilter(color)
        binding.buttonBottomRight.setColorFilter(color)
    }

    fun onKeyboardHidden() {
        if (typing) {
            if (cursorThread?.isInterrupted != true) {
                cursorThread?.interrupt()
                cursorThread = null
            }
            onPaintEditingBitmap()
        }
    }

    fun onKeyboardShowed() {
        if (typing) {
            if (cursorThread?.isAlive != true) {
                cursorThread = Thread(cursorRunnable)
                cursorThread?.start()
            }
            onPaintEditingBitmap(true)
        }
    }

    fun onLoadImage(bitmap: Bitmap) {
        val color = bitmap.getOppositeAverageColor()
        binding.buttonTopLeft.setColorFilter(color)
        binding.buttonTopRight.setColorFilter(color)
        binding.buttonBottomLeft.setColorFilter(color)
        binding.buttonBottomRight.setColorFilter(color)

        bitmapStack.add(bitmap)
        binding.imageView.setImageBitmap(bitmapStack.last())
        typing = false
        painting = false
        binding.editText.setText("")
        path.clear()
        paintingBitmap = null
        typingBitmap = null
    }

    private fun onPainting(value: Boolean) {
        if (value) {
            binding.buttonTopLeft.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            binding.buttonTopRight.setImageResource(android.R.drawable.ic_menu_send)
            binding.buttonBottomRight.setImageResource(android.R.drawable.ic_menu_upload)
            binding.buttonBottomLeft.setImageResource(android.R.drawable.ic_menu_view)
            binding.buttonBottomLeft.setBackgroundColor(myColor)
        } else {
            binding.buttonTopLeft.setImageResource(android.R.drawable.ic_menu_revert)
            binding.buttonTopRight.setImageResource(android.R.drawable.ic_menu_edit)
            binding.buttonBottomRight.setImageResource(android.R.drawable.ic_menu_save)
            binding.buttonBottomLeft.setImageResource(android.R.drawable.ic_menu_gallery)
            binding.buttonBottomLeft.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun onRevertClick() {
        if (bitmapStack.count() > 1) {
            bitmapStack.removeLast()
            binding.imageView.setImageBitmap(bitmapStack.last())
        }
    }

    private fun onToggleEditClick() {
        painting = !painting

        typing = false
        binding.editText.setText("")
        path.clear()
        paintingBitmap = null
        typingBitmap = null

        binding.imageView.setImageBitmap(bitmapStack.last())
        context.hideKeyboard()
    }

    private fun onGalleryClick() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        @Suppress("DEPRECATION")
        context.startActivityForResult(photoPickerIntent, 1)
    }

    private fun onConfirmClick() {
        if (binding.editText.text.toString().isNotBlank())
        {
            binding.imageView.requestFocus()
            val bitmap = bitmapStack.last().copy(bitmapStack.last().config, true)
            val canvas = Canvas(bitmap)
            canvas.myDrawTextOnPath(binding.editText.text.toString() + " ", path.toTypedArray(), Paint().apply {
                style = Paint.Style.FILL
                color = myColor
                textSize = mySize
            })
            bitmapStack.add(bitmap)
        }

        binding.imageView.setImageBitmap(bitmapStack.last())
        typing = false
        painting = false
        binding.editText.setText("")
        path.clear()
        paintingBitmap = null
        typingBitmap = null
        context.hideKeyboard()
    }

    private fun onSaveClick() {
        val saved = bitmapStack.last().saveToGallery(context, "Test.jpg")
        Toast.makeText(context, "Image saved: $saved", Toast.LENGTH_LONG).show()
    }

    private fun onTextSizeClick() {
        textDialog?.show()
    }

    private fun onColorClick() {
        colorPicker?.show(context.supportFragmentManager, "colorPicker")
    }

    fun onTopLeftClick() {
        if (painting) onToggleEditClick()
        else onRevertClick()
    }

    fun onTopRightClick() {
        if (painting) onConfirmClick()
        else onToggleEditClick()
    }

    fun onBottomLeftClick() {
        if (painting) onColorClick()
        else onGalleryClick()
    }

    fun onBottomRightClick() {
        if (painting) onTextSizeClick()
        else onSaveClick()
    }

    fun onTouch(event: MotionEvent) : Boolean {
        if (painting && !typing)
        {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> addToPath(Point(event.x, event.y))
                MotionEvent.ACTION_MOVE -> addToPath(Point(event.x, event.y))
                MotionEvent.ACTION_UP -> {
                    addToPath(Point(event.x, event.y))
                    typing = true
                    typingBitmap = paintingBitmap?.copy(paintingBitmap?.config!!, true)
                    binding.editText.requestFocus()
                    context.showKeyboard()
                }
            }
        } else if (painting && typing) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    binding.editText.requestFocus()
                    context.showKeyboard()
                }
            }
        }
        return true
    }

    private fun addToPath(point: Point) {
        if(!path.contains(point)) path.add(point)
        path = path.smooth()
        paintingBitmap = bitmapStack.last().copy(bitmapStack.last().config, true)
        onPaintEditingBitmap()
    }

    fun onTextChanged() {
        if (typing) {
            typingBitmap = paintingBitmap?.copy(paintingBitmap?.config!!, true)
            onPaintEditingBitmap()
        }
    }

    private fun onPaintEditingBitmap(addCursor: Boolean = false) {
        if(paintingBitmap != null) {
            paintingBitmap = bitmapStack.last().copy(bitmapStack.last().config, true)
            if (!typing || cursorThread?.isAlive == true) {
                val pathCanvas = Canvas(paintingBitmap!!)
                pathCanvas.myDrawPath(path.toTypedArray(), Paint().apply {
                    style = Paint.Style.FILL
                    color = myColor
                    strokeWidth = 5f
                })
                binding.imageView.setImageBitmap(paintingBitmap)
            }
            if (typingBitmap != null) {
                typingBitmap = paintingBitmap?.copy(paintingBitmap?.config!!, true)
                val typingCanvas = Canvas(typingBitmap!!)
                typingCanvas.myDrawTextOnPath(binding.editText.text.toString() + if (addCursor) "|" else " ", path.toTypedArray(), Paint().apply {
                    style = Paint.Style.FILL
                    color = myColor
                    textSize = mySize
                })
                binding.imageView.setImageBitmap(typingBitmap)
            }
        }
    }
}