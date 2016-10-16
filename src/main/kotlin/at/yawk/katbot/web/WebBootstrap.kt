/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package at.yawk.katbot.web

import at.yawk.katbot.Config
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.handlers.PathHandler
import io.undertow.server.handlers.resource.ClassPathResourceManager
import io.undertow.server.handlers.resource.ResourceHandler
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.ws.rs.Path
import javax.ws.rs.core.Application
import javax.ws.rs.ext.Provider

/**
 * @author yawkat
 */
private val log = LoggerFactory.getLogger(WebBootstrap::class.java)

@Singleton
class WebBootstrap @Inject internal constructor(
        private val config: Config,
        private val securityContainerRequestFilter: SecurityContainerRequestFilter,
        private val objectMapper: ObjectMapper
) : WebProvider {
    private val classLoader = WebBootstrap::class.java.classLoader

    private val resources = HashSet<Any>()
    private val prefixes = mutableMapOf<String, HttpHandler>(
            "/" to ResourceHandler(ClassPathResourceManager(classLoader, "at/yawk/katbot/web/static")),
            "/webjars" to ResourceHandler(ClassPathResourceManager(classLoader, "META-INF/resources/webjars"))
    )
    private var started = false

    fun start() {
        val application = object : Application() {
            override fun getClasses() = setOf(ErrorHandlers.ShiroExceptionMapper::class.java)

            override fun getSingletons(): Set<Any> {
                return resources + securityContainerRequestFilter +
                        @Provider object : JacksonJsonProvider() {
                            init {
                                setMapper(objectMapper)
                            }
                        }
            }
        }

        val server = UndertowJaxrsServer()

        val rootField = server.javaClass.getDeclaredField("root")
        rootField.isAccessible = true
        val root = rootField.get(server) as PathHandler
        for ((prefix, handler) in prefixes) {
            root.addPrefixPath(prefix, handler)
        }

        server.deploy(application, "/api")
                .start(Undertow.builder().addHttpListener(config.web.port, config.web.bindHost))
    }

    override fun addResource(resource: Any) {
        if (started) throw IllegalStateException()
        if (!resource.javaClass.isAnnotationPresent(Path::class.java)) {
            log.warn("Missing @Path annotation on ${resource.javaClass.name}")
        }
        resources.add(resource)
    }

    override fun addRootHandler(prefix: String, httpHandler: HttpHandler) {
        prefixes.put(prefix, httpHandler)
    }
}