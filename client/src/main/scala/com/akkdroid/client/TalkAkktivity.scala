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
import android.util.Log
class TalkAkktivity extends Activity {

  import Conversions._

  private var messageTextView: TextView = null
  private var sendButton: Button = null
  private var settingsButton: Button = null
  private var messagesList: ListView = null

  private var settingsMenu: MenuItem = null
  private var adapter: ArrayAdapter[String] = null

  private var talkActor: ActorRef = null
  private var recipient: ActorSelection = null

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
    settingsButton = findViewById(R.id.settingsButton).asInstanceOf[Button]
    messagesList = findViewById(R.id.messagesList).asInstanceOf[ListView]
    val items = new ju.ArrayList[String]
    val bundle = getIntent().getExtras()

    val user = bundle.getString("remote-nick")
    //recipient = bundle.getSerializable("remote-ref").asInstanceOf[ActorSelection]
    //talkActor = bundle.getSerializable("local-ref").asInstanceOf[ActorRef]
    items.add(s"Started conversation with $user")
    adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, items)
    messagesList.setAdapter(adapter)

    recipient = Akktivity.system.actorSelection(bundle.getString("remote-ref"))

    val handler = new Handler
    def newLocalActor = new HandlerDispatcherActor(handler, msg => {
      msg match {
        case TalkMessage(sender, text) => {
          Log.e("TalkAkktivity", "got MSG!!!")
          adapter.add(s"<$sender> $text")
          adapter.notifyDataSetChanged()
        }
      }
    })
    val localActor = Akktivity.system.actorOf(Props(newLocalActor), name = "talk-update-handler")
    Akktivity.talkActor ! SetTalkListener(localActor)

    sendButton.onClickAsync { _ =>
      Log.i("TalkAkktivity", "sending message START")
      Akktivity.talkActor ! ForwardMessage(recipient, getNick(), messageTextView.getText.toString)
      Log.i("TalkAkktivity", "sending message END")
    }
  }
}
