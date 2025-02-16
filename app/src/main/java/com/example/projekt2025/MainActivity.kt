package com.example.projekt2025

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toogle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)

        drawerLayout.addDrawerListener(toogle)
        toogle.syncState()

        if(savedInstanceState==null){
            replaceFragment(IntroFragment())
            navigationView.setCheckedItem(R.id.nav_intro)
        }

    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.nav_intro->replaceFragment(IntroFragment())
            R.id.nav_pose->replaceFragment(PoseFragment())
            R.id.nav_detect->replaceFragment(DetectFragment())
            R.id.nav_emotion->replaceFragment(EmotionFragment())
            R.id.nav_dashboard->replaceFragment(DashboardFragment())

        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true

    }

    private fun replaceFragment(fragment: Fragment){
        val transaction: FragmentTransaction =supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction .commit()
    }


    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else {
            onBackPressedDispatcher.onBackPressed()
        }
    }


}