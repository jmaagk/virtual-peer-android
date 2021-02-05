package me.maagk.johannes.virtualpeer

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import me.maagk.johannes.virtualpeer.fragment.StartFragment
import me.maagk.johannes.virtualpeer.fragment.chat.ChatFragment
import me.maagk.johannes.virtualpeer.fragment.settings.SettingsFragment
import me.maagk.johannes.virtualpeer.fragment.survey.SurveyFragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // making sure the app follows the system's theme (light / dark)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // setting the layout
        setContentView(R.layout.activity_main)

        // finding the Toolbar and setting it
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // checking if this is the first time the app is launched
        // if this is the case, the current time will be saved for later use
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var firstLaunchTime = sharedPreferences.getLong(getString(R.string.pref_first_launch_time), -1)
        if(firstLaunchTime == -1L) {
            firstLaunchTime = System.currentTimeMillis()
            sharedPreferences.edit(commit = true) {
                putLong(getString(R.string.pref_first_launch_time), firstLaunchTime)
            }
        }

        // creating the hamburger icon for the navigation drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)

        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val manager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

            val calendar = Calendar.getInstance()
            val to = calendar.timeInMillis
            calendar.add(Calendar.YEAR, -1)
            val from = calendar.timeInMillis

            val stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, from, to)
            for(stat in stats) {
                if(stat.packageName == "me.maagk.johannes.customsoundboard") {
                    Utils.log("packageName=${stat.packageName}")
                }
            }
            Utils.log("size=${stats.size}")
        }*/

        // showing either the start fragment or the one that was previously on top
        if(savedInstanceState == null) {
            val startFragment = StartFragment()
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, startFragment, StartFragment.TAG).commit()
            navigationView.setCheckedItem(R.id.navDrawerStart)
        } else {
            // TODO: retrieve the fragment on top
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        if(navigationView.checkedItem?.itemId == item.itemId)
            return false

        // TODO: reuse fragment instances; add them to the back stack?
        return when(item.itemId) {
            R.id.navDrawerStart -> {
                val startFragment = supportFragmentManager.findFragmentByTag(StartFragment.TAG) ?: StartFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, startFragment, StartFragment.TAG).commit()
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }

            R.id.navDrawerSettings -> {
                val settingsFragment = supportFragmentManager.findFragmentByTag(SettingsFragment.TAG) ?: SettingsFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, settingsFragment, SettingsFragment.TAG).commit()
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }

            R.id.navDrawerSurvey -> {
                val surveyFragment = SurveyFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, surveyFragment, null).commit()
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }

            R.id.navDrawerChat -> {
                val chatFragment = ChatFragment()
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, chatFragment, null).commit()
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }

            else -> false
        }
    }

}