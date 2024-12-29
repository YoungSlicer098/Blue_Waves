package com.dld.bluewaves

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dld.bluewaves.databinding.ActivityAdminUserEditBinding

class AdminUserEditActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAdminUserEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAdminUserEditBinding.inflate(layoutInflater)
        setContentView(mBinding.root)



    }
}