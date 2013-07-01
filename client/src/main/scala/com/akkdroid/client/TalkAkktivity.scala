package com.akkdroid.client

import android.app.Activity
import android.view.{MenuItem, Menu}
import android.content.Intent
import android.os.{Handler, Bundle}
import java.{util => ju}
import android.widget.{ListView, TextView, Button, ArrayAdapter}
import com.akkdroid.client.TalkActor.{SetTalkListener, ForwardMessage, TalkMessage}
import android.preference.PreferenceManager
import akka.actor.{Props, ActorSelection, ActorRef}

class TalkAkktivity extends Activity {

  import Conversions._

  private var messageTextView: TextView = null
  private var sendButton: Button = null
  private var messagesList: ListView = null

  private var settingsMenu: MenuItem = null
  private var adapter: ArrayAdapter[String] = null

  private var recipient: ActorSelection = null
  private var localActor: ActorRef = null

  override def onCreateOptionsMenu(menu: Menu) : Boolean = {
    settingsMenu = menu.add(Menu.NONE, 0, 0, "Show current settings")
    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(menu : MenuItem) : Boolean = {
    if (menu equals settingsMenu) {
      startActivity(new Intent(getBaseContext, classOf[AkkdroidPreferences]))
      true
    } else false
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    loadTalkUI()
    startConversation()
  }

  override def onStart() {
    super.onStart()
  }

  override def onResume() {
    super.onResume()
  }

  override def onDestroy() {
    super.onDestroy()
  }

  private def getNick(): String = {
    val pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext)
    val newNick: String = pref.getString("pref_nick", getString(R.string.pref_nick_value))
    newNick
  }

  private def loadTalkUI() {
    setContentView(R.layout.talk)
    sendButton = findViewById(R.id.sendButton).asInstanceOf[Button]
    messageTextView = findViewById(R.id.messageText).asInstanceOf[TextView]
    messagesList = findViewById(R.id.messagesList).asInstanceOf[ListView]
    val items = new ju.ArrayList[String]

    adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, items)
    messagesList.setAdapter(adapter)

    sendButton.onClickAsync { _ =>
      messageTextView.setText("")
      Akktivity.talkActor ! ForwardMessage(recipient, getNick(), messageTextView.getText.toString)
    }
    if (localActor == null) {
      val handler = new Handler
      def newLocalActor = new HandlerDispatcherActor(handler, msg => {
        msg match {
          case TalkMessage(sender, text) => {
            adapter.add(s"<$sender> $text")
            adapter.notifyDataSetChanged()
          }
        }
      })
      localActor = Akktivity.system.actorOf(Props(newLocalActor))
      Akktivity.talkActor ! SetTalkListener(localActor)
    }
  }

  def startConversation() {
    val bundle = getIntent().getExtras()
    recipient = Akktivity.system.actorSelection(bundle.getString("remote-ref"))
    val user = bundle.getString("remote-nick")
    adapter.add(s"Started chat with $user")
    adapter.notifyDataSetChanged()
  }
}
