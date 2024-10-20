package com.example.bluewaves

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.bluewaves.ui.theme.BlueWavesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        enableEdgeToEdge();
        setContentView(R.layout.activity_main);

        val hamburgMenuBtn: ImageView = findViewById(R.id.hamburgMenu_btn)
        val navLogo: ImageView = findViewById(R.id.navLogo)
        val navTitle: TextView = findViewById(R.id.navTitle)

        hamburgMenuBtn.setOnClickListener {
            Toast.makeText(this, "You clicked on the Hamburger Menu Icon", Toast.LENGTH_SHORT).show()
        }
    }
}