package com.interact.listen.acd

import com.interact.listen.Organization
import com.interact.listen.User
import com.interact.listen.fax.Fax
import com.interact.listen.fax.OutgoingFax
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

import javax.servlet.http.HttpServletResponse

//import grails.plugins.springsecurity.Secured
@Secured(['ROLE_KEY_API'])
class AcdApiController
{
    static allowedMethods = [
        updateAgent: 'PUT',
        getActiveCall: 'GET'
    ]

    def historyService;

    def updateAgent = {
        if(!params.id)
        {
            renderJsonResponse(HttpServletResponse.SC_BAD_REQUEST, [message: 'Missing required parameter [id]'])
            log.warn 'Missing required parameter [id]'
            return
        }

        if(!params.apiKey)
        {
            renderJsonResponse(HttpServletResponse.SC_BAD_REQUEST, [message: 'Missing required parameter [apiKey]'])
            log.warn 'Missing required parameter [apiKey]'
            return
        }

        if(!params.status)
        {
            renderJsonResponse(HttpServletResponse.SC_BAD_REQUEST, [message: 'Missing required parameter [status]'])
            log.warn 'Missing required parameter [status]'
            return
        }

        def agentId = params.get("id");
        String toggle = params.get("status");
        String apiKey = params.get("apiKey");

        log.debug("agentId: " + agentId);
        log.debug("toggle: " + toggle);
        log.debug("apiKey: " + apiKey);

        Organization org = Organization.findByApiKey(apiKey);
        String statusSet = "";

        User agent = User.findByIdAndOrganization(agentId, org);
        if(agent != null)
        {
            if(toggle != null && toggle.equalsIgnoreCase(AcdQueueStatus.Available.toString()))
            {
                agent.getAcdUserStatus().setAcdQueueStatus(AcdQueueStatus.Available);
                agent.getAcdUserStatus().setStatusModified(DateTime.now());
                statusSet = AcdQueueStatus.Available.toString();
            }
            else if(toggle != null && toggle.equalsIgnoreCase(AcdQueueStatus.Unavailable.toString()))
            {
                agent.getAcdUserStatus().setAcdQueueStatus(AcdQueueStatus.Unavailable);
                agent.getAcdUserStatus().setStatusModified(DateTime.now());
                statusSet = AcdQueueStatus.Unavailable.toString();
            }
            else
            {
                log.warn("Bad status value: " + toggle);
                renderJsonResponse(HttpServletResponse.SC_BAD_REQUEST, [message: "Bad status value: " + toggle])
            }

            if(agent.validate() && agent.save())
            {
                log.debug("Agent status updated.");
                historyService.acdApiSet(org, agent, statusSet);
                renderJsonResponse(HttpServletResponse.SC_OK, [
                        id: agent.id,
                        status: agent.getAcdUserStatus().getAcdQueueStatus().toString(),
                        statusModified: agent.getAcdUserStatus().getStatusModified()
                ])
            }
            else
            {
                log.error("Unable to save agent.");
                renderJsonResponse(HttpServletResponse.SC_BAD_REQUEST, [message: 'Unable to save agent'])
            }
        }
        else
        {
            log.warn("Bad agent value: " + agentId);
            renderJsonResponse(HttpServletResponse.SC_NOT_FOUND, [message: 'The agent requested could not be found'])
        }

        response.flushBuffer()
    }

    def getAgent = {
        if(!params.id)
        {
            renderJsonResponse(HttpServletResponse.SC_BAD_REQUEST, [message: 'Missing required parameter [id]'])
            log.warn 'Missing required parameter [id]'
            return
        }

        if(!params.apiKey)
        {
            renderJsonResponse(HttpServletResponse.SC_BAD_REQUEST, [message: 'Missing required parameter [apiKey]'])
            log.warn 'Missing required parameter [apiKey]'
            return
        }

        def agentId = params.get("id");
        String apiKey = params.get("apiKey");

        log.debug("agentId: " + agentId);
        log.debug("apiKey: " + apiKey);

        Organization org = Organization.findByApiKey(apiKey);

        User agent = User.findByIdAndOrganization(agentId, org);
        if(agent != null)
        {
            renderJsonResponse(HttpServletResponse.SC_OK, [
                    id: agent.id,
                    status: agent.getAcdUserStatus().getAcdQueueStatus().toString(),
                    statusModified: agent.getAcdUserStatus().getStatusModified()?.withZone(DateTimeZone.UTC)?.toString("yyyy-MM-dd HH:mm:ss")
            ])
        }
        else
        {
            log.warn "Agent not found."
            renderJsonResponse(HttpServletResponse.SC_NOT_FOUND, [message: 'The agent requested could not be found'])
            return;
        }

        response.flushBuffer()
    }

    def getActiveCall = {
        log.debug "getActiveCall request with params ${params}"

        if(!params.apiKey)
        {
            renderJsonResponse(HttpServletResponse.SC_BAD_REQUEST, [message: 'Missing required parameter [apiKey]'])
            log.warn 'Missing required parameter [apiKey]'
            return
        }

        if(!params.username)
        {
            renderJsonResponse(HttpServletResponse.SC_BAD_REQUEST, [message: 'Missing required parameter [username]'])
            log.warn 'Missing required parameter [username]'
            return
        }

        Organization org = Organization.findByApiKey(params.apiKey);

        if (!org) {
            renderJsonResponse(HttpServletResponse.SC_UNAUTHORIZED, [message: 'Bad Api Key'])
            log.warn "Organization for apiKey [${params.apiKey}] not found."
            return
        }

        User user = User.findByOrganizationAndUsername(org, params.username)

        if (!user) {
            renderJsonResponse(HttpServletResponse.SC_NOT_FOUND, [message: 'Username not found'])
            log.warn "Username [${params.username}] for org [${org.name}] not found."
            return
        }

        // Otherwise lets get the active acd calls
        def acdCall = AcdCall.findByUser(user)

        if (acdCall) {
            renderJsonResponse(HttpServletResponse.SC_OK, [
                    callId: acdCall.getCommonCallId(),
                    availability: user.getAcdUserStatus()?.getAcdQueueStatus()?.name(),
                    callStatus: "IN_PROGRESS",
                    callStartTime: acdCall.getInitTime()?.getMillis() / 1000
            ])
            return
        }

        log.warn "No Active ACD Calls found for user ${params.username}"
        renderJsonResponse(HttpServletResponse.SC_NOT_FOUND, [message: 'No active call was found for the username requested'])
    }

    private def renderJsonResponse(def statusCode, def jsonMap) {
        render(contentType: 'application/json', status: statusCode) {
            jsonMap
        }
    }
}
