package com.dld.bluewaves

import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.dld.bluewaves.databinding.ActivityImageBinding
import com.dld.bluewaves.view.ImagePagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

@Suppress("DEPRECATION")
class ImageActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityImageBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.closeBtn.setOnClickListener {
            onBackPressed()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.fade_in_static, R.anim.fade_out_down)
            }
        })

        val imgList = intent.getStringArrayListExtra("imgList")!!
        val annId = intent.getStringExtra("annId")!!
        val initialPosition = intent.getIntExtra("initialPosition", 0)
        val indicatorContainer: LinearLayout = findViewById(R.id.indicatorContainer)
        val numPages = imgList.size // Replace with the number of images/pages

        mBinding.viewPager.adapter = ImagePagerAdapter(imgList, annId)

        // Set the initial position
        mBinding.viewPager.setCurrentItem(initialPosition, false)

        // Dynamically create indicators
        for (i in 0 until numPages) {
            val indicator = FloatingActionButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(25.dpToPx(), 25.dpToPx()).apply {
                    setMargins(8.dpToPx(), 0, 8.dpToPx(), 0)
                }
                size = FloatingActionButton.SIZE_MINI
                isClickable = false
                setImageResource(android.R.color.transparent)
                backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context as AppCompatActivity, R.color.unpressed_color)
                )
            }
            indicatorContainer.addView(indicator)
        }

        // Highlight the first indicator
        highlightIndicator(initialPosition)

        // Listen for page changes
        mBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                highlightIndicator(position)
            }
        })
    }

    // Highlight the active indicator
    private fun highlightIndicator(position: Int) {
        for (i in 0 until mBinding.indicatorContainer.childCount) {
            val indicator = mBinding.indicatorContainer.getChildAt(i) as FloatingActionButton
            if (i == position) {
                indicator.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.pressed_color)) // Active color
            } else {
                indicator.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.unpressed_color)) // Inactive color
            }
        }
    }

    // Extension function for dp to px conversion
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Restrict touch events to this activity
        return if (ev != null && isTouchInsideView(ev)) {
            super.dispatchTouchEvent(ev)
        } else {
            false
        }
    }

    private fun isTouchInsideView(event: MotionEvent): Boolean {
        val viewBounds = Rect()
        findViewById<View>(R.id.image_view)?.getHitRect(viewBounds)
        return viewBounds.contains(event.x.toInt(), event.y.toInt())
    }

}