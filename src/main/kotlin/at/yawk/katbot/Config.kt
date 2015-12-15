package at.yawk.katbot

import java.net.URI

/**
 * @author yawkat
 */
data class Config(
        val nick: String,
        val server: Config.Server,
        val feeds: Map<URI, RssFeedListener.FeedConfiguration>,
        val forums: Map<URI, ForumListener.ForumConfiguration>,
        val paste: at.yawk.paste.client.Config
) {
    data class Server(
            val host: String,
            val port: Int,
            val secure: Boolean,
            val password: String
    )
}
