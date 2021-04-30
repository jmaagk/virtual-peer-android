package me.maagk.johannes.virtualpeer.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import me.maagk.johannes.virtualpeer.R
import me.maagk.johannes.virtualpeer.Utils
import me.maagk.johannes.virtualpeer.VirtualPeerApp
import me.maagk.johannes.virtualpeer.chat.Message
import me.maagk.johannes.virtualpeer.exercise.PomodoroExercise
import me.maagk.johannes.virtualpeer.fragment.StartFragment
import me.maagk.johannes.virtualpeer.fragment.chat.ChatFragment
import me.maagk.johannes.virtualpeer.fragment.exercise.AddLearningContentFragment
import me.maagk.johannes.virtualpeer.fragment.settings.ProfileFragment
import me.maagk.johannes.virtualpeer.fragment.stats.StatsFragment

class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var startFragment: StartFragment
    private lateinit var chatFragment: ChatFragment
    private lateinit var statsFragment: StatsFragment

    private lateinit var profileFragment: ProfileFragment

    private val Intent.rateExercise
        get() = hasExtra("rateExercise")

    private val Intent.exerciseClass
        get() = getSerializableExtra("rateExercise")

    private var exerciseRatingStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // making sure the app follows the system's theme (light / dark)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // setting the layout
        setContentView(R.layout.activity_main)

        // finding the Toolbar and setting it
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // creating the hamburger icon for the navigation drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { item -> onNavigationItemSelected(item, true) }

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item -> onNavigationItemSelected(item, false) }

        // the two states for the items in the bottom bar
        val states = Array(2) {
            IntArray(1)
        }
        states[0][0] = android.R.attr.state_checked
        states[1][0] = android.R.attr.state_enabled

        // the two colors the icons will have depending on the state
        val textColors = IntArray(2)
        textColors[0] = Utils.getColor(this, R.color.colorBottomNavigationActiveItem)
        textColors[1] = Utils.getColor(this, R.color.colorBottomNavigationInactiveItem)

        bottomNavigationView.itemIconTintList = ColorStateList(states, textColors)

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

        if(intent.rateExercise)
            initRateExercise()
    }

    private fun onNavigationItemSelected(item: MenuItem, navDrawer: Boolean): Boolean {
        if(navDrawer && navigationView.checkedItem?.itemId == item.itemId)
            return false

        if(!navDrawer && bottomNavigationView.selectedItemId == item.itemId)
            return false

        // TODO: reuse fragment instances; add them to the back stack?
        val fragment: Fragment = when(item.itemId) {
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
            R.id.navDrawerMyProfile -> {
                if(!::profileFragment.isInitialized)
                    profileFragment = supportFragmentManager.findFragmentByTag(ProfileFragment.TAG) as ProfileFragment? ?: ProfileFragment()
                profileFragment
            }
            else -> {
                if(!::startFragment.isInitialized)
                    startFragment = supportFragmentManager.findFragmentByTag(StartFragment.TAG) as StartFragment? ?: StartFragment()
                startFragment
            }
        }

        val tag = when(item.itemId) {
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
        if(manager.backStackEntryCount == 0)
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
            is ChatFragment, is AddLearningContentFragment -> R.id.navChat
            is StatsFragment -> R.id.navStats
            else -> R.id.navStart
        }

        if(navDrawer)
            navigationView.setCheckedItem(itemToSelect)
        else
            bottomNavigationView.selectedItemId = itemToSelect

        if(intent.rateExercise && getTopFragment() is ChatFragment)
            rateExercise()
    }

    fun removeOnMessageSentListener(onMessageSentListener: ChatFragment.OnMessageSentListener) {
        chatFragment.removeOnMessageSentListener(onMessageSentListener)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if(intent == null)
            return

        this.intent = intent

        if(intent.rateExercise)
            initRateExercise()
    }

    private fun initRateExercise() {
        if(::chatFragment.isInitialized)
            rateExercise()
        else
            chatFragment = ChatFragment()

        // switching to the chat in case it's not visible yet; also important for getting a context from it
        if(getTopFragment() !is ChatFragment) {
            val chatItem = bottomNavigationView.menu.findItem(R.id.navChat)
            onNavigationItemSelected(chatItem, false)
            bottomNavigationView.selectedItemId = chatItem.itemId
        }
    }

    private fun rateExercise() {
        if(exerciseRatingStarted)
            return

        exerciseRatingStarted = true

        when(intent.exerciseClass) {
            PomodoroExercise::class.java -> {
                val exercise = PomodoroExercise(chatFragment)
                exercise.rate()

                // canceling the notification because it would still be visible otherwise
                NotificationManagerCompat.from(this).cancel(VirtualPeerApp.NOTIFICATION_ID_POMODORO_FINISH)
            }
        }
    }

}