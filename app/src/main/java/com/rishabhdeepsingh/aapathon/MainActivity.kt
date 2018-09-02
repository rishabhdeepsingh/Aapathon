package com.rishabhdeepsingh.aapathon

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback, MyRecyclerViewAdapter.ItemClickListener, OnMapReadyCallback {
    private var mapFragment: SupportMapFragment? = null
    private val REQCODE: Int = 9001
    private lateinit var profileSection: LinearLayout
    private lateinit var signOutButton: Button
    private lateinit var googleSignInButton: SignInButton
    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var profilePic: ImageView
    private lateinit var googleApiClient: GoogleApiClient

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.google_signin -> signIn()
            R.id.btn_signout -> signOut()
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onItemClick(view: View, position: Int) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allPermissions()
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        recyclerViewSetup()
        ifNotAskPermission(Manifest.permission.CAMERA)
        ifNotAskPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ifNotAskPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        ifNotAskPermission(Manifest.permission.INTERNET)
        ifNotAskPermission(Manifest.permission.CALL_PHONE)
        ifNotAskPermission(Manifest.permission.SEND_SMS)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("MissingPermission", "WrongConstant")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                //Check if Camera Permissions are not Given ask and then Start the Camera
                val intent = Intent("android.media.action.IMAGE_CAPTURE")
                startActivity(intent)
                Toast.makeText(this, "Camera", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_tab_layout -> {
                setContentView(R.layout.activity_tab)
                val tabLayout = findViewById<TabLayout>(R.id.simpleTabLayout)
                val firstTab = tabLayout.newTab()
                firstTab.text = "First"
                tabLayout.addTab(firstTab)
                val secondTab = tabLayout.newTab()
                secondTab.text = "Second"
                tabLayout.addTab(secondTab)
                val thirdTab = tabLayout.newTab()
                thirdTab.text = "Third"
                tabLayout.addTab(thirdTab)
                tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabUnselected(p0: TabLayout.Tab?) {
                    }

                    override fun onTabReselected(p0: TabLayout.Tab?) {
                    }

                    override fun onTabSelected(tab: TabLayout.Tab) {
                        var fragment: Fragment = FirstTabFragment()
                        when (tab.position) {
                            0 -> fragment = FirstTabFragment()
                            1 -> fragment = SecondTabFragment()
                            2 -> fragment = ThirdTabFragment()
                        }
                        val fragmentManager = supportFragmentManager
                        val fragmentTransaction = fragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.simpleFrameLayout, fragment)
                        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        fragmentTransaction.commit()
                    }
                })
            }
            R.id.nav_google_signin -> {
                googleSignIn()
                Toast.makeText(this, "Google Sign in Page", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_call_sms -> {
                callAndSmsHandler()
                Toast.makeText(this, "Call/ SMS", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_maps -> {
                setContentView(R.layout.activity_map)
                mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
                mapFragment?.getMapAsync(this)
                Toast.makeText(this, "Google Maps", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_send -> {
                setContentView(R.layout.activity_notification)
                val notificationTitle = findViewById<EditText>(R.id.notification_title)
                val notificationText = findViewById<EditText>(R.id.notification_text)
                val notificationTime = findViewById<EditText>(R.id.notification_time)
                val notificationButton = findViewById<Button>(R.id.notification_button)
                val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationButton.setOnClickListener {
                    notificationBuilder.setAutoCancel(true)
                            .setWhen(SystemClock.elapsedRealtime())
                            .setSmallIcon(R.drawable.notification_icon_background)
                            .setTicker("Hearty365")
                            .setContentTitle(notificationTitle.text)
                            .setContentText(notificationText.text)
                    val intent = Intent(this, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    notificationBuilder.setContentIntent(pendingIntent)
                    Handler().postDelayed({
                        notificationManager.notify(1, notificationBuilder.build())
                        notificationText.text.clear()
                        notificationTitle.text.clear()
                        notificationTime.text.clear()
                    }, notificationTime.text.toString().toLong() * 1000)
                    Toast.makeText(this, "Notification has been set", Toast.LENGTH_SHORT).show()
                }
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)

        return true
    }

    @SuppressLint("MissingPermission")
    fun callAndSmsHandler() {
        setContentView(R.layout.activity_call)
        val callButton = findViewById<Button>(R.id.btn_call)
        val smsButton = findViewById<Button>(R.id.btn_sms)
        val phoneNumber = findViewById<EditText>(R.id.phone_number)
        phoneNumber.clearFocus()
        phoneNumber.requestFocus()
        val smsText = findViewById<EditText>(R.id.sms_text)
        smsText.clearFocus()
        smsText.requestFocus()
        ifNotAskPermission(Manifest.permission.CALL_PHONE)
        ifNotAskPermission(Manifest.permission.SEND_SMS)
        callButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:${phoneNumber.text}")
            ifNotAskPermission(Manifest.permission.CALL_PHONE)
            startActivity(intent)
        }
        smsButton.setOnClickListener {
            if (phoneNumber.text.length != 10 || smsText.text.isEmpty()) {
                Toast.makeText(this, "Please enter a Valid Phone Number or SMS Text", Toast.LENGTH_LONG).show()
            } else {
                SmsManager.getDefault().sendTextMessage(phoneNumber.text.toString(), null, smsText.text.toString(), null, null)
                Toast.makeText(this, "Message Sent using Default Sim", Toast.LENGTH_SHORT).show()
                smsText.text.clear()
                phoneNumber.text.clear()
            }
        }
    }

    private fun ifNotAskPermission(permission: String) {
        if (!checkPermission(permission)) {
            askPermission(permission, 1)
        }
    }

    private fun allPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 1)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_NETWORK_STATE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1)
    }

    private fun askPermission(permissionString: String, code: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permissionString), code)
    }

    private fun checkPermission(permission: String): Boolean {
        val temp = ContextCompat.checkSelfPermission(this, permission)
        return temp == PackageManager.PERMISSION_GRANTED
    }

    private fun recyclerViewSetup() {
        //Recycler View
        // added list of all IIT's to the Recycler View
        val list = mutableListOf<String>()
        list.add("IIT Kharagpur")
        list.add("IIT Bombay")
        list.add("IIT Kanpur")
        list.add("IIT Madras")
        list.add("IIT Delhi")
        list.add("IIT Guwahati")
        list.add("IIT Roorkee")
        list.add("IIT Ropar")
        list.add("IIT Bhubaneswar")
        list.add("IIT Gandhinagar")
        list.add("IIT Hyderabad")
        list.add("IIT Jodhpur")
        list.add("IIT Patna")
        list.add("IIT Indore")
        list.add("IIT Mandi")
        list.add("IIT Tirupati")
        list.add("IIT Dhanbad")
        list.add("IIT Bhilai")
        list.add("IIT Goa")
        list.add("IIT Jammu")
        list.add("IIT Dharwad")

        val recyclerView = findViewById<RecyclerView>(R.id.main_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = MyRecyclerViewAdapter(this, list)
        adapter.setClickListener(this)
        recyclerView.adapter = adapter
        //Recycler View Finished
    }


    //Google Maps
    override fun onMapReady(googleMap: GoogleMap?) {
        val sydney = LatLng(22.318174, 87.298793)
        googleMap?.addMarker(MarkerOptions().position(sydney).title("Hey its Me"))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }


    // Handle Google Login
    private fun googleSignIn() {
        setContentView(R.layout.activity_signin)
        profileSection = findViewById(R.id.profile)
        signOutButton = findViewById(R.id.btn_signout)
        googleSignInButton = findViewById(R.id.google_signin)
        name = findViewById(R.id.name)
        email = findViewById(R.id.email)
        profilePic = findViewById(R.id.profile_pic)
        googleSignInButton.setOnClickListener(this)
        signOutButton.setOnClickListener(this)
        profileSection.visibility = View.GONE
        val signInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        googleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, signInOption).build()
    }

    private fun signIn() {
        val intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(intent, REQCODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQCODE) {
            val googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleResult(googleSignInResult)
        }
    }

    private fun signOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback {
            updateUI(false)
        }
    }

    private fun handleResult(result: GoogleSignInResult) {
        if (result.isSuccess) {
            val account = result.signInAccount
            val nameResult = account?.displayName
            val emailResult = account?.email
            val imageUrlResult = account?.photoUrl.toString()
            name.text = nameResult
            email.text = emailResult
            Glide.with(this).load(imageUrlResult).into(profilePic)
            updateUI(true)
        } else {
            updateUI(false)
        }
    }

    private fun updateUI(isLogin: Boolean) {
        if (isLogin) {
            profileSection.visibility = View.VISIBLE
            googleSignInButton.visibility = View.GONE
        } else {
            profileSection.visibility = View.GONE
            googleSignInButton.visibility = View.VISIBLE
        }
    }
}
