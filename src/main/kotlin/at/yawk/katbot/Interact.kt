package at.yawk.katbot

import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent
import org.kitteh.irc.lib.net.engio.mbassy.listener.Handler
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * @author yawkat
 */
class Interact @Inject constructor(val ircProvider: IrcProvider, val config: Config) {
    companion object {
        private val COMMAND_PATTERN = Pattern.compile("~\\s*(\\w+)\\s*($NAME_PATTERN)\\s*")
    }

    fun start() {
        ircProvider.registerEventListener(this)
    }

    @Handler
    fun onPublicMessage(event: ChannelMessageEvent) {
        val matcher = COMMAND_PATTERN.matcher(event.message)
        if (matcher.matches()) {
            val command = matcher.group(1)
            val interactions = config.interactions[command.toLowerCase()]
            if (interactions != null) {
                val message = Template(randomChoice(interactions))
                        .set("bot", event.client.nick)
                        .set("target", matcher.group(2))
                        .finish()
                if (message.startsWith("ACTION:")) {
                    event.channel.sendCTCPMessage("ACTION " + message.substring("ACTION:".length))
                } else {
                    event.channel.sendMessage(message)
                }
            }
        }
    }
}