package com.demo.cameraappnew

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import com.demo.cameraappnew.camhelper.FILE_PATH_KEY

class LandingActivity : AppCompatActivity() {

    private lateinit var container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        container = findViewById(R.id.main_fragment_container)
    }


}
