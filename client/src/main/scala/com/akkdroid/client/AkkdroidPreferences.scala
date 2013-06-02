package com.akkdroid.client

import android.preference.PreferenceActivity
import android.os.Bundle
import android.view.Menu

class AkkdroidPreferences extends PreferenceActivity {
  override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)
      addPreferencesFromResource(R.xml.preferences)
  }

  override def onCreateOptionsMenu(menu: Menu) : Boolean = {
    menu.add(Menu.NONE, 0, 0, "Show current settings")
    super.onCreateOptionsMenu(menu)
  }

}
