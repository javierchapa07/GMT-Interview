package com.example.gmttest

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Window
import android.view.WindowInsets
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.gmttest.databinding.ActivityMainBinding
import com.example.gmttest.managers.TextBrush
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var textBrush: TextBrush

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ActivityMainBinding.inflate(layoutInflater)
        textBrush = TextBrush(this, binding)
        setContentView(binding.root)
        window?.insetsController?.hide(WindowInsets.Type.statusBars())
        binding.imageView.setOnTouchListener { _, event -> textBrush.onTouch(event) }
        binding.buttonTopLeft.setOnClickListener { textBrush.onTopLeftClick() }
        binding.buttonTopRight.setOnClickListener { textBrush.onTopRightClick() }
        binding.buttonBottomLeft.setOnClickListener { textBrush.onBottomLeftClick() }
        binding.buttonBottomRight.setOnClickListener { textBrush.onBottomRightClick() }
        binding.editText.addTextChangedListener { textBrush.onTextChanged() }
        binding.imageView.setImageResource(R.drawable.test_image)
        binding.imageView.viewTreeObserver.addOnGlobalLayoutListener { textBrush.onFirstLoad() }
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)
            val keypadHeight = binding.root.height - rect.bottom
            if (keypadHeight < binding.root.height * 0.15) textBrush.onKeyboardHidden()
            else textBrush.onKeyboardShowed()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            try { textBrush.onLoadImage(BitmapFactory.decodeStream(contentResolver.openInputStream(data?.data!!))) }
            catch (e: FileNotFoundException) { Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show() }
        }
    }
}