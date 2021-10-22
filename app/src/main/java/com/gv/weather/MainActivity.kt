package com.gv.weather

import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import com.gv.weather.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val navController
            by lazy { (supportFragmentManager.findFragmentById(R.id.navHostFragment)
                    as NavHostFragment).navController }

    private val b by lazy { ActivityMainBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        b.rootLayout.systemUiVisibility =
            SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        ViewCompat.setOnApplyWindowInsetsListener(b.rootLayout) { view, insets ->
            b.rootLayout.updatePadding(top = 0)
            insets
        }

        /*val appBarConfiguration =
            AppBarConfiguration(navController.graph)*/
        //setupActionBarWithNavController(navController, appBarConfiguration)
        //b.bottomNav.setupWithNavController(navController)
    }

}