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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import me.maagk.johannes.virtualpeer.chat.Message
import me.maagk.johannes.virtualpeer.fragment.StartFragment
import me.maagk.johannes.virtualpeer.fragment.chat.ChatFragment
import me.maagk.johannes.virtualpeer.fragment.settings.SettingsFragment
import me.maagk.johannes.virtualpeer.fragment.stats.StatsFragment
import me.maagk.johannes.virtualpeer.fragment.survey.SurveyFragment

class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var startFragment: StartFragment
    private lateinit var chatFragment: ChatFragment
    private lateinit var statsFragment: StatsFragment

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
        navigationView.setNavigationItemSelectedListener { item -> onNavigationItemSelected(item, true) }

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item -> onNavigationItemSelected(item, false) }

        // showing either the start fragment or the one that was previously on top
        if(savedInstanceState == null) {
            startFragment = StartFragment()
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, startFragment!!, StartFragment.TAG).commit()
            bottomNavigationView.selectedItemId = R.id.navStart
        } else {
            // TODO: retrieve the fragment on top for later use
            // val topFragment = getTopFragment()
        }

        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    private fun onNavigationItemSelected(item: MenuItem, navDrawer: Boolean): Boolean {
        if(navDrawer && navigationView.checkedItem?.itemId == item.itemId)
            return false

        if(!navDrawer && bottomNavigationView.selectedItemId == item.itemId)
            return false

        // TODO: reuse fragment instances; add them to the back stack?
        val fragment: Fragment = when(item.itemId) {
            R.id.navDrawerSettings -> supportFragmentManager.findFragmentByTag(SettingsFragment.TAG) ?: SettingsFragment()
            R.id.navDrawerSurvey -> supportFragmentManager.findFragmentByTag(SurveyFragment.TAG) ?: SurveyFragment()
            R.id.navChat -> {
                if(!::chatFragment.isInitialized)
                    chatFragment = supportFragmentManager.findFragmentByTag(ChatFragment.TAG) as ChatFragment? ?: ChatFragment()
                chatFragment
            }
            R.id.navStats -> {
                if(!::statsFragment.isInitialized)
                    statsFragment = supportFragmentManager.findFragmentByTag(StatsFragment.TAG) as StatsFragment? ?: StatsFragment()
                statsFragment
            }
            else -> {
                if(!::startFragment.isInitialized)
                    startFragment = supportFragmentManager.findFragmentByTag(StartFragment.TAG) as StartFragment? ?: StartFragment()
                startFragment
            }
        }

        val tag = when(item.itemId) {
            R.id.navDrawerSettings -> SettingsFragment.TAG
            R.id.navDrawerSurvey -> SurveyFragment.TAG
            R.id.navChat -> ChatFragment.TAG
            R.id.navStats -> StatsFragment.TAG
            else -> StartFragment.TAG
        }

        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment, tag).addToBackStack(tag).commit()
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getTopFragment(): Fragment? {
        val manager = supportFragmentManager
        if (manager.backStackEntryCount == 0)
            return manager.findFragmentByTag(StartFragment.TAG)

        val backStackStateTag = manager.getBackStackEntryAt(manager.backStackEntryCount - 1).name
        return manager.findFragmentByTag(backStackStateTag)
    }

    fun queueMessage(message: Message) {
        if(!::chatFragment.isInitialized)
            chatFragment = ChatFragment()

        if(!::startFragment.isInitialized)
            startFragment = StartFragment()

        chatFragment.addOnMessageSentListener(startFragment)
        chatFragment.queueMessage(message)

        if(getTopFragment() !is ChatFragment) {
            val chatItem = bottomNavigationView.menu.findItem(R.id.navChat)
            onNavigationItemSelected(chatItem, false)
            bottomNavigationView.selectedItemId = chatItem.itemId
        }
    }

    override fun onBackStackChanged() {
        // TODO: is there a better way to achieve this behavior?
        var navDrawer = false
        val itemToSelect = when(getTopFragment()) {
            is SettingsFragment -> {
                navDrawer = true
                R.id.navDrawerSettings
            }
            is SurveyFragment -> {
                navDrawer = true
                R.id.navDrawerSurvey
            }
            is ChatFragment -> R.id.navChat
            is StatsFragment -> R.id.navStats
            else -> R.id.navStart
        }

        if(navDrawer)
            navigationView.setCheckedItem(itemToSelect)
        else
            bottomNavigationView.selectedItemId = itemToSelect
    }

    fun removeOnMessageSentListener(onMessageSentListener: ChatFragment.OnMessageSentListener) {
        chatFragment.removeOnMessageSentListener(onMessageSentListener)
    }

}