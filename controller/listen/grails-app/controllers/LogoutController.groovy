import com.interact.listen.SingleOrganizationConfiguration
import com.interact.listen.stats.Stat
import grails.plugin.springsecurity.SpringSecurityUtils

class LogoutController {

    def historyService
    def springSecurityService
    def statWriterService

	def index = {
        historyService.loggedOut(springSecurityService.getCurrentUser())
        statWriterService.send(Stat.GUI_LOGOUT)

        def url = SpringSecurityUtils.securityConfig.logout.filterProcessesUrl
        if(session.organization && !SingleOrganizationConfiguration.exists()) {
            url += "?spring-security-redirect=/${session.organization.contextPath}"
        } else if(session.organizationContext == 'custodian') {
            url += "?spring-security-redirect=/custodian"
        }

        redirect(uri: url)
	}
}
