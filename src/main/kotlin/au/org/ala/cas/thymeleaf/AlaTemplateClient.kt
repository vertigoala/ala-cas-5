package au.org.ala.cas.thymeleaf

import au.org.ala.cas.SkinProperties
import au.org.ala.utils.logger
import com.github.benmanes.caffeine.cache.Caffeine
import org.apereo.cas.web.support.WebUtils
import org.springframework.webflow.execution.RequestContextHolder
import java.io.Reader
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest

class AlaTemplateClient(val skinConfig: SkinProperties, val cookieName: String) {

    companion object {
        const val LOGGED_IN_CLASS = "logged-in"
        const val LOGGED_OUT_CLASS = "not-logged-in"

        val log = logger<AlaHeaderFooterTagProcessor>()
    }

    val uri = URI(skinConfig.headerFooterUrl)
    val cache = Caffeine.newBuilder().expireAfterWrite(skinConfig.cacheDuration, TimeUnit.MILLISECONDS).build(this::loadTemplate)

    fun loadTemplate(template: String) = uri.resolve("./$template.html").toURL().openStream().reader().use(Reader::readText)

    fun load(name: String, request: HttpServletRequest?, fluidLayout: Boolean = false): String? {
        val cached = try {
            cache[name]
        } catch (e: Exception) {
            log.error("Couldn't load {}", name, e)
            null
        }

        if (cached == null || cached.isBlank()) {
            return null
        }
        var content = cached.replace("::headerFooterServer::", skinConfig.headerFooterUrl)
                .replace("::centralServer::", skinConfig.baseUrl)
                .replace("::searchServer::", skinConfig.bieBaseUrl)
                .replace("::searchPath::", skinConfig.bieSearchPath)
                .replace("::authStatusClass::", if (isLoggedIn(request)) LOGGED_IN_CLASS else LOGGED_OUT_CLASS)
        if (fluidLayout) {
            content = content.replace("class=\"container\"", "class=\"container-fluid\"")
        }
        if (content.contains("::loginLogoutListItem::")) {
            // only do the work if it is needed
            content = content.replace("::loginLogoutListItem::", buildLoginoutLink(request))
        }
        return content
    }

    fun isLoggedIn(request: HttpServletRequest?): Boolean {
        val ctx = RequestContextHolder.getRequestContext()
        return if (ctx != null) {
            // TODO pass in the request context
            WebUtils.getCredential(ctx) != null
//            (request.cookies?.any { it.name ==  cookieName } ?: false) || request.userPrincipal != null
        } else { false }
    }

    /**
     * Builds the login or logout link based on current login status.
     * @param attrs any specified params to override defaults
     * @return
     */
    fun buildLoginoutLink(request: HttpServletRequest?): String {
//        val requestUri = removeContext(grailServerURL) + request.forwardURI
//        val logoutUrl = attrs.logoutUrl ?: grailServerURL + "/session/logout"
//        val logoutReturnToUrl = attrs.logoutReturnToUrl ?: requestUri
//                def casLogoutUrl = attrs.casLogoutUrl ?: casLogoutUrl
//
//                // TODO should this be attrs.logoutReturnToUrl?
//                if (!attrs.loginReturnToUrl && request.queryString) {
//                    logoutReturnToUrl += "?" + URLEncoder.encode(request.queryString, "UTF-8")
//                }

        return if (isLoggedIn(request)) {
            val casLogoutUrl = request?.servletContext?.contextPath + "/logout"
            "<a href=\"$casLogoutUrl\">Logout</a>"
        } else {
            // currently logged out
            val casLoginUrl = request?.servletContext?.contextPath + "/login"
            "<a href=\"$casLoginUrl\">Log in</a>"
        }
    }
}