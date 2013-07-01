package com.akkdroid.client

import android.preference.PreferenceActivity
import android.os.Bundle
import android.view.Menu

class AkkdroidPreferences extends PreferenceActivity {
  override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)
      addPreferencesFromResource(R.xml.preferences)
  }
}
