package com.interact.listen.acd

import com.interact.listen.DirectInwardDialNumber
import com.interact.listen.Organization
import com.interact.listen.User
import com.interact.listen.acd.AcdCall
import com.interact.listen.acd.AcdUserStatus
import com.interact.listen.acd.Skill
import com.interact.listen.User
import com.interact.listen.PhoneNumber
import com.interact.listen.history.*
import grails.converters.JSON
import org.joda.time.DateTime
import com.interact.listen.util.FileTypeDetector
import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.LocalDateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.context.MessageSource

import javax.servlet.AsyncContext
import javax.servlet.http.HttpServletRequest

@Secured(['ROLE_ACD_USER'])
class AcdController
{
    static allowedMethods = [
      index: 'GET',
      status: 'GET',
      toggleStatus: 'POST',
      updateNumber: 'POST',
      callQueue: 'GET',
      acdCallHistory: 'GET',
      pollQueue: 'GET',
      pollHistory: 'GET',
      pollStatus: 'GET',
      exportHistoryToCsv: 'GET',
      jsframe: 'GET'
    ]

    def promptFileService
    def historyService
    def spotCommunicationService
    def acdService
    def springSecurityService
    MessageSource messageSource

    private static final String storageLocation = "acd";

    def index = {
        redirect(action: 'status')
    }

    def jsframe = {
      render(view: 'jsframe')
    }

    def callQueue = {
      if (!springSecurityService.currentUser)
      {
          //Redirect to login
          redirect(controller: 'login', action: 'auth');
      }

      def user = springSecurityService.currentUser

      params.sort = params.sort ?: 'enqueueTime'
      params.order = params.order ?: 'asc'
      params.max = params.max ?: 10
      params.offset = params.offset ?: 0

      def calls = []
      def allCalls = []
      def callTotal = 0

      if ( user.hasRole('ROLE_ORGANIZATION_ADMIN') || user.hasRole('ROLE_QUEUE_USER') )
      { // get all calls
        calls = AcdCall.createCriteria().list() {
            skill
            {
                eq('organization', user.organization);
            }
            order(params.sort, params.order)
            maxResults(params.max.toInteger())
            firstResult(params.offset.toInteger())
        }
        callTotal = AcdCall.createCriteria().list() {
            skill
            {
                eq('organization', user.organization);
            }
        }.size();
      }
      // else { // Get calls for the current user
        // def skills = []
        // UserSkill.findAllByUser(user)?.each() { userSkill ->
          // skills.add(userSkill.skill)
        // }

        // if they have no skills there shouldn't be any waiting calls
        // if ( !skills.isEmpty() ) {
          // calls = AcdCall.createCriteria().list() {
              // order(params.sort, params.order)
              // maxResults(params.max.toInteger())
              // firstResult(params.offset.toInteger())
              // 'in'("skill",skills)
          // }
          // allCalls = AcdCall.createCriteria().list() {
            // 'in'("skill",skills)
          // }
        // }
        // callTotal = allCalls.size()
      // }

      def model = [
              calls: calls,
              callTotal: callTotal,
              sort: params.sort,
              order: params.order,
              max: params.max,
              offset: params.offset
      ]

      log.debug "Rending view [callQueue] with model [${model}]"
      render(view: 'callQueue', model: model)
    }

    def pollQueue = {
      def user = springSecurityService.currentUser

      params.sort = params.sort ?: 'enqueueTime'
      params.order = params.order ?: 'asc'
      params.max = params.max ?: 10
      params.offset = params.offset ?: 0

      def json = [:]

      List<AcdCall> calls = []

      if ( user.hasRole('ROLE_ORGANIZATION_ADMIN') || user.hasRole('ROLE_QUEUE_USER') ) { // get all calls
        calls = AcdCall.createCriteria().list {
            skill
            {
                eq('organization', user.organization);
            }
            order(params.sort, params.order)
            maxResults(params.max.toInteger())
            firstResult(params.offset.toInteger())
        }
      }

      json.callTotal = AcdCall.createCriteria().list {
          skill
          {
              eq('organization', user.organization);
          }
      }.size();

      // else { // get calls for current users skillset
        // def skills = []
        // UserSkill.findAllByUser(user).each() { userSkill ->
          // skills.add(userSkill.skill)
        // }
        // calls = AcdCall.createCriteria().list {
          // order(params.sort, params.order)
          // maxResults(params.max.toInteger())
          // firstResult(params.offset.toInteger())
          // 'in'("skill",skills)
        // }
      // }

      def callJson = []
      for (AcdCall call : calls)
      {
          def c = [:]
          c.id = call.id
          c.ani = call.ani
          c.onHold = call.onHold
          c.sessionId = call.sessionId
          c.skill = call.skill.description
          c.callStatus = call.callStatus.viewable()
          c.enqueueTime = call.enqueueTime
          c.lastModified = call.lastModified
          c.user = ""
          if (call.user != null)
          {
              c.user = call.user.realName
          }
          callJson.add(c)
      }

      json.calls = callJson

      render(contentType: 'application/json') {
          json
      }
    }

    def pollHistory = {
      def user = springSecurityService.currentUser

      def organization = session.organization
      if (!organization){
          log.error("Failed to evaluate organization from [${session.organization}]")
          redirect(action: 'acdCallHistory')
          return
      }
      params.sort = params.sort ?: 'enqueueTime'
      params.order = params.order ?: 'desc'
      params.max = params.max ?: 10
      params.offset = params.offset ?: 0
      LocalDate today = new LocalDate();

      def json = [:]

      List<AcdCallHistory> acdCalls = []

      def acdCallHistory
      if ( user.hasRole('ROLE_ORGANIZATION_ADMIN') )
      {
          // get all call history
          log.info("Find acdCallHistory for organization [${organization}][${organization.id}]");
          acdCalls = AcdCallHistory.createCriteria().list {
              order(params.sort, params.order)
              maxResults(params.max.toInteger())
              firstResult(params.offset.toInteger())
              skill {
                  eq('organization', organization)
              }
              or {
                  ge("agentCallStart", today.toDateTimeAtStartOfDay())
                  ge("enqueueTime", today.toDateTimeAtStartOfDay())
              }

          }

      }
      else
      {
          // get call history for current user
          log.info("Find acdCallHistory for organization [${organization}] and user [${user}]");
          acdCallHistory = AcdCallHistory.createCriteria().list {
              order(params.sort, params.order)
              maxResults(params.max.toInteger())
              firstResult(params.offset.toInteger())
              skill {
                  eq('organization', organization)
              }
              eq("user",user)
              or {
                  ge("agentCallStart", today.toDateTimeAtStartOfDay())
                  ge("enqueueTime", today.toDateTimeAtStartOfDay())
              }
          }
      }

      if (acdCallHistory) {
          log.debug("We've found [${acdCallHistory.size()}] acd records")
      }

      def callJson = []
      for (AcdCallHistory call : acdCalls)
      {
          def c = [:]
          c.id = call.id
          c.ani = call.ani
          c.user = ""
          if (call.user != null)
          {
              c.user = call.user.realName
          }
          c.skill = call.skill.description
          c.start = call.agentCallStart
          c.end = call.agentCallEnd
          c.callStatus = call.callStatus.viewable()
          c.enqueueTime = call.enqueueTime
          c.dequeueTime = call.dequeueTime
          callJson.add(c)
      }

      json.acdCalls = callJson

      if (log.isInfoEnabled())
      {
          log.info("Rendering call queue as json [${json.toString()}]");
      }

      render(contentType: 'application/json') {
          json
      }
    }

    def pollStatus = {

        HttpServletRequest theRequest = request;
        boolean isAjax = theRequest.xhr;

        if(!isAjax)
        {
            redirect(action: 'index');
        }

      def user = springSecurityService.currentUser

      params.queueSort = params.queueSort ?: 'enqueueTime'
      params.queueOrder = params.queueOrder ?: 'asc'
      params.queueMax = params.queueMax ?: 5

      def json = [:]

      // get agent call queue
      def skills = []
      UserSkill.findAllByUser(user)?.each() { userSkill ->
        skills.add(userSkill.skill)
      }

      def calls = []
      // Should only have waiting calls if one or more skills is assigned
      if ( !skills.isEmpty() ) {
        calls = AcdCall.createCriteria().list {
          order(params.queueSort,params.queueOrder)
          maxResults(params.queueMax.toInteger())
          // firstResult(params.offset.toInteger())
          eq("callStatus",AcdCallStatus.WAITING)
          'in'("skill",skills)
        }
      }

      def callJson = []
      for(AcdCall call : calls) {
        def c = [:]
        c.id = call.id
        c.ani = call.ani
        c.onHold = call.onHold
        c.sessionId = call.sessionId
        c.skill = call.skill.description
        c.callStatus = call.callStatus.viewable()
        c.enqueueTime = call.enqueueTime
        c.lastModified = call.lastModified
        c.user = ""
        if ( call.user != null ) {
          c.user = call.user.realName
        }
        callJson.add(c)
      }

      json.calls = callJson

      //Get Agent ACD Status
      json.userStatus = user.acdUserStatus.AcdQueueStatus.toString();
      json.userStatusTitle = message(code: 'page.acd.status.button.' + json.userStatus);

      render(contentType: 'application/json') {
        json
      }
    }

    def status = {
      if (!springSecurityService.currentUser)
      {
        //Redirect to login
        redirect(controller: 'login', action: 'auth');
      }

      def user = springSecurityService.currentUser

      def organization = session.organization
      if (!organization){
          log.error("Failed to evaluate organization from [${session.organization}]")
          redirect(action: 'acdCallHistory')
          return
      }

      // Get Agent Status Details
      def acdUserStatus = AcdUserStatus.findByOwner(user)
      if (!acdUserStatus) {
          log.debug "user does not currently have an acd user status [${acdUserStatus}]"
          acdUserStatus = new AcdUserStatus()
          acdUserStatus.owner = user
          acdUserStatus.acdQueueStatus = AcdQueueStatus.Unavailable
          /* Create user acd status entry, as they should have had one already */
          if (acdUserStatus.validate() && acdUserStatus.save(failOnError: true, flush: true)) {
              log.error "Created acd user status for this user [${acdUserStatus.owner}]"
          } else {
              log.error "Could not create Acd Status Entry for new user."
          }
      }

      def status = acdUserStatus?.acdQueueStatus?.toString()
      def statusDisabled = acdUserStatus?.acdQueueStatus?.isDisabled()
      def contactNumber = acdUserStatus?.contactNumber
      def phoneNumbers = []

      // Get List of Agent's Available Contact Numbers
      List<PhoneNumber> phoneNumberList = PhoneNumber.findAllByOwner(user);

      for(PhoneNumber number : phoneNumberList)
      {
          if(number instanceof DirectInwardDialNumber)
          {
              if(log.isDebugEnabled())
              {
                log.debug("Not adding Direct Dial Number");
              }
          }
          else
          {
               if(log.isDebugEnabled())
               {
                    log.debug("Adding number");
               }
                phoneNumbers.add(number)
          }
      }

      // Get Agent's Skillset
      def userSkills = UserSkill.findAllByUser(user)

      // Get Current Call
      def currentCalls = AcdCall.findAllByUser(user)
      def currentCallTotal = AcdCall.countByUser(user)

      // Query for waiting calls associated with agent's skillset
      params.queueSort = params.queueSort ?: 'enqueueTime'
      params.queueOrder = params.queueOrder ?: 'asc'
      params.queueMax = params.queueMax ?: 5
      params.queueOffset = params.queueOffset ?: 0

      def skills = []
      userSkills?.each() { userSkill ->
        skills.add(userSkill.skill)
      }

      def calls = []
      // Should only have waiting calls if one or more skills is assigned
      if ( !skills.isEmpty() ) {
        calls = AcdCall.createCriteria().list() {
          order(params.queueSort,params.queueOrder)
          maxResults(params.queueMax.toInteger())
          firstResult(params.queueOffset.toInteger())
          eq("callStatus",AcdCallStatus.WAITING)
          'in'("skill",skills)
        }
      }

      if (log.isDebugEnabled())
      {
          log.debug "User status contact number [${contactNumber}]"
      }

      def allCalls = []
      // Should only have waiting calls if one or more skills is assigned
      if ( !skills.isEmpty() ) {
        allCalls = AcdCall.createCriteria().list() {
          eq("callStatus",AcdCallStatus.WAITING)
          'in'("skill",skills)
        }
      }
      def callTotal = allCalls.size()

      // Get Agent's Call History
      params.historySort = params.historySort ?: 'enqueueTime'
      params.historyOrder = params.historyOrder ?: 'desc'

      LocalDate today = new LocalDate()

      def acdCallHistory
      if ( user.hasRole('ROLE_ORGANIZATION_ADMIN') ) { // get all call history
          log.debug("Find acdCallHistory for organization [${organization}][${organization.id}]")
          acdCallHistory = AcdCallHistory.createCriteria().list {
              order(params.historySort, params.historyOrder)
              skill {
                  eq('organization', organization)
              }
              or {
                  ge("agentCallStart", today.toDateTimeAtStartOfDay())
                  ge("enqueueTime", today.toDateTimeAtStartOfDay())
              }
          }
      } else { // get call history for current user
          log.debug("Find acdCallHistory for organization [${organization}] and user [${user}]")
          acdCallHistory = AcdCallHistory.createCriteria().list {
              order(params.historySort, params.historyOrder)
              skill {
                  eq('organization', organization)
              }
              eq("user",user)
              or {
                  ge("agentCallStart", today.toDateTimeAtStartOfDay())
                  ge("enqueueTime", today.toDateTimeAtStartOfDay())
              }
          }
      }

      def historyTotal = 0
      if (acdCallHistory) {
          historyTotal = acdCallHistory.size()
      }
      log.debug("We've found [${historyTotal}] acd records")


      def model = [
        status: status,
        statusDisabled: statusDisabled,
        phoneNumbers: phoneNumbers,
        contactNumber: contactNumber,
        acdCallHistory: acdCallHistory,
        callTotal: callTotal,
        historyTotal: historyTotal,
        calls: calls,
        userSkills: userSkills,
        currentCalls: currentCalls,
        currentCallTotal: currentCallTotal
      ]

      if (log.isDebugEnabled()) {
          log.debug "Rendering view [status] with model [${model}]"
      }

      render(view: 'status', model: model)
    }

    def toggleStatus =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.toggleStatus: params[${params}]"
            }

            def acdUserStatus = AcdUserStatus.findByOwner(springSecurityService.currentUser)
            if (!acdUserStatus)
            {
                log.error "Failed to find acd user status, maybe not serious"
                flash.errorMessage = message(code: 'page.acd.status.statusChange.failure.message', args: [params?.toggle_status])
                redirect(action: 'status')
                return
            }

            //Don't let them go available if contact number is null
            if(acdUserStatus.AcdQueueStatus == AcdQueueStatus.Unavailable &&
                acdUserStatus.contactNumber == null)
            {
                flash.errorMessage = message(code: 'page.acd.status.statusChange.noNumber.failure.message');
            }
            else
            {
                acdUserStatus.toggleStatus()
                acdUserStatus.statusModified = DateTime.now()

                if (log.isDebugEnabled())
                {
                    log.debug "Updating user [${acdUserStatus.owner.username}] to status [${acdUserStatus.acdQueueStatus}]"
                }

                if (acdUserStatus.acdQueueStatus && acdUserStatus.validate() &&
                        acdUserStatus.save(failOnError: true, flush: true))
                {
                    historyService.toggleACDStatus(acdUserStatus)
                    flash.successMessage = message(code: 'page.acd.status.statusChange.successful.message', args: [acdUserStatus.acdQueueStatus])
                }
                else
                {
                    log.error "Could not update user acd status."
                    flash.errorMessage = message(code: 'page.acd.status.statusChange.failure.message', args: [params?.toggle_status])
                }
            }

            redirect(action: 'status')
            return
        }


    def toggleAgentStatus =
    {
        if (log.isDebugEnabled())
        {
            log.debug "AcdController.toggleAgentStatus: params[${params}]"
        }

        def agent = User.get(params.agentId);
        def acdUserStatus = AcdUserStatus.findByOwner(agent);
        if (!acdUserStatus)
        {
            log.error "Failed to find acd user status, maybe not serious"
            flash.errorMessage = message(code: 'page.acd.status.statusChange.failure.message', args: [params?.toggle_status])
            redirect(action: 'status')
            return
        }

        //Don't let them go available if contact number is null
        if(acdUserStatus.AcdQueueStatus == AcdQueueStatus.Unavailable &&
                acdUserStatus.contactNumber == null)
        {
            flash.errorMessage = message(code: 'page.acd.status.statusChange.noNumber.failure.message');
        }
        else
        {
            acdUserStatus.toggleStatus()
            acdUserStatus.statusModified = DateTime.now()

            if (log.isDebugEnabled())
            {
                log.debug "Updating user [${acdUserStatus.owner.username}] to status [${acdUserStatus.acdQueueStatus}]"
            }

            if (acdUserStatus.acdQueueStatus && acdUserStatus.validate() &&
                    acdUserStatus.save(failOnError: true, flush: true))
            {
                historyService.toggleACDStatus(acdUserStatus)
                flash.successMessage = message(code: 'page.acd.status.statusChange.successful.message', args: [acdUserStatus.acdQueueStatus])
            }
            else
            {
                log.error "Could not update user acd status."
                flash.errorMessage = message(code: 'page.acd.status.statusChange.failure.message', args: [params?.toggle_status])
            }
        }

        redirect(action: 'agentStatus')
        return
    }

    def updateNumber =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.updateNumber: params[${params}]"
            }
            def acdUserStatus = AcdUserStatus.findByOwner(springSecurityService.currentUser)
            if (!acdUserStatus)
            {
                log.error "Failed to find acd user status, maybe not serious"
                flash.errorMessage =
                    message(code: 'page.acd.status.statusChange.failure.message', args: [params?.toggle_status])
                redirect(action: 'status')
                return
            }

            if(params.contactNumber != null && !params.contactNumber.isEmpty())
            {
                PhoneNumber theNumber = PhoneNumber.get(params.contactNumber);

                acdUserStatus.setContactNumber(theNumber);
                if (acdUserStatus.validate() && acdUserStatus.save(failOnError: true, flush: true))
                {
                    historyService.updatedACDContactNumber(acdUserStatus)
                    flash.successMessage =
                        message(code: 'page.acd.status.statusNumber.successful.message',
                                args: [acdUserStatus.contactNumber.number])
                }
                else
                {
                    log.error "Failed to find phone number [${params?.contactNumber}]"
                    flash.errorMessage =
                        message(code: 'page.acd.status.statusNumber.failure.message', args: [params?.contactNumber])
                }
            }

            redirect(action: 'status')
        }

    def uploadPrompt =
        {
            def file = request.getFile('uploadFile')
            if (!file)
            {
                render('Please select a file to upload')
                return
            }

            def detector = new FileTypeDetector()
            def detectedType = detector.detectContentType(file.inputStream, file.originalFilename)
            if (detectedType != 'audio/x-wav')
            {
                render('File must be a wav file')
                return
            }

            def user = springSecurityService.currentUser
            promptFileService.save(storageLocation, file, user.organization.id)

            render('Success')
        }

    def currentCall =
        {
            def call = AcdCall.findAllByUser(springSecurityService.currentUser)
            render(view: 'currentCall', model: [calls: call])
        }

    def polledCalls =
        {
            List<AcdCall> calls = AcdCall.findAllByUser(springSecurityService.currentUser)

            HttpServletRequest theRequest = request;
            boolean isAjax = theRequest.xhr;

            if(!isAjax)
            {
                redirect(action: 'index');
            }

            def json = [:]

            def callJson = [];

            for (AcdCall thisCall : calls)
            {
                def c = [:]
                c.id = thisCall.id;
                c.ani = thisCall.ani;
                c.onHold = thisCall.onHold;
                c.sessionId = thisCall.sessionId;
                c.skill = thisCall.skill.description;
                callJson.add(c);
            }

            json.calls = callJson;

            render(contentType: 'application/json') {
                json
            }
        }

    def callerOffHold =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.callerOffHold: params[${params}]"
            }

            boolean success = false;

            AcdCall thisCall = AcdCall.get(params.id);

            if (thisCall)
            {
                try
                {
                    spotCommunicationService.sendAcdOffHoldEvent(thisCall.sessionId);
                    success = true;
                }
                catch (Exception e)
                {
                    log.error("Exception sending off hold event: " + e);
                }
            }

            if (success)
            {
                thisCall.onHold = false;
                thisCall.save(flush: true);
                flash.successMessage = message(code: 'page.acd.call.offHold.success.message');
            }
            else
            {
                flash.errorMessage = message(code: 'page.acd.call.offHold.fail.message');
            }

            render(contentType: 'application/json')
                    {
                        thisCall
                    }
        }

    def callerOnHold =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.callerOnHold: params[${params}]"
            }

            boolean success = false;
            AcdCall thisCall = AcdCall.get(params.id);

            if (thisCall)
            {
                try
                {
                    spotCommunicationService.sendAcdOnHoldEvent(thisCall.sessionId);
                    success = true;
                }
                catch (Exception e)
                {
                    log.error("Exception sending on hold event: " + e);
                }
            }

            if (success)
            {
                thisCall.onHold = true;
                thisCall.save(flush: true);
                flash.successMessage = message(code: 'page.acd.call.hold.success.message');
            }
            else
            {
                flash.errorMessage = message(code: 'page.acd.call.hold.fail.message');
            }

            render(contentType: 'application/json')
                    {
                        thisCall
                    }
        }

    def transferCaller =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.transferCaller: params[${params}]"
            }

            boolean success = false;

            AcdCall thisCall
            if (params.id) {
                thisCall = AcdCall.get(params.id);
                if (thisCall)
                    log.debug("Found acd call by id")
            } else if (params.sessionId) {
                thisCall = AcdCall.findBySessionId(params.sessionId)
                if (thisCall)
                    log.debug("Found acd call by session id")
            }

            if(Integer.parseInt(params.userId) < 0)
            {
                //This is a transfer to a queue
                //Skill ID is the negation of the userid
                int theSkillId = Integer.parseInt(params.userId) * -1;
                Skill theSkill = Skill.get(theSkillId);
                if(theSkill != null && log.isDebugEnabled())
                {
                    log.debug("Transferring call to queue[" + theSkill.skillname + "]");
                }

                success = acdService.transferCallToQueue(thisCall, theSkill);
            }
            else
            {
                User transferTo = User.get(params.userId);

                if (thisCall && transferTo)
                {
                    try
                    {
                        Boolean sendIvrRequest = true
                        if (params?.noIvr == 'true')
                            sendIvrRequest = false
                        acdService.transferCall(thisCall, transferTo, sendIvrRequest);
                        success = true;
                    }
                    catch (Exception e)
                    {
                        log.error("Exception sending transfer event to agent[" + transferTo.realName + "] : " + e);
                    }
                }
            }

            def json = [:]
            json.success = success.toString();

            render(contentType: 'application/json')
                    {
                        json
                    }
        }

    def availableTransferAgents =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.availableTransferAgents: params[${params}]"
            }

            def user = springSecurityService.currentUser;

            def users;

            AcdCall thisCall = AcdCall.get(params.id);

            if (thisCall)
            {
                users = acdService.getAcdAgentList(user.organization);
            }

            def json = [:]

            def userJson = [];

            //First add the other queues
            List<Skill> skills = Skill.createCriteria().list(sort: "skillname")
            {
                eq('organization', user.organization)
            };

            for(Skill thisSkill : skills)
            {
                def c = [:];
                c.id = -1 * thisSkill.id;
                c.realName = thisSkill.description + " Queue";
                c.type = 'queue';
                userJson.add(c);
            }

            for (User thisUser : users)
            {
                if(thisUser.enabled)
                {
                    log.debug "User [${thisUser.realName}] is enabled and put on transfer list"
                    def c = [:]
                    c.id = thisUser.id;
                    c.realName = thisUser.realName;
                    c.type = 'agent';
                    userJson.add(c);
                }
                else
                {
                    log.debug "User [${thisUser.realName}] is disabled and excluded from transfer list"
                }

            }

            json.users = userJson;

            render(contentType: 'application/json')
                    {
                        json
                    }
        }

    def disconnectCaller =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.disconnectCaller: params[${params}]"
            }

            boolean success = false;

            AcdCall thisCall = AcdCall.get(params.id);

            if (thisCall)
            {
                try
                {
                    acdService.acdCallVoicemail(thisCall);
                    success = true;
                }
                catch (Exception e)
                {
                    log.error("Exception sending voicemail event: " + e);
                }
            }

            def json = [:]
            json.success = success.toString();

            render(contentType: 'application/json')
                    {
                        json
                    }
        }

    def exportHistoryToCsv =
    {
        log.debug "AcdController.exportHistoryToCsv: params[${params}]"

        params.sort = 'enqueueTime'
        params.order = 'desc'
        params.max = '1000'
        params.offset = '0'

        DateTime theStart = getStartDate(params.startDate);
        DateTime theEnd = getEndDate(params.endDate);

        def user = springSecurityService.currentUser;

        def calls = acdService.acdHistoryList(params.sort, params.order, params.max, params.offset, theStart, theEnd,
                params.agent, params.skill, user.organization);

        String filename = "listen-acdcallhistory-${new LocalDateTime().toString('yyyyMMddHHmmss')}.csv";

        File tmpFile
        try
        {
            if(log.isDebugEnabled())
            {
                log.debug("Creating temp file to extract ACD Call History Records")
            }
            tmpFile = File.createTempFile("./" + filename,".tmp");
            tmpFile.deleteOnExit();
            if(log.isDebugEnabled())
            {
                log.debug("Created tmp file [${tmpFile.getName()}] to extract ACD Call History Records");
            }
        }
        catch (IOException e)
        {
            log.error("Failed to create temp file for export ${e}")
            flash.errorMessage = message(code: 'page.administration.acd.callHistory.exportCSV.fileCreateFailed');
            redirect(action: "acdCallHistory");
            //TODO perhaps do something to notify system administrators of an important event?
            return
        }

        //Create header row for the call history information
        tmpFile.append(messageSource.getMessage('callHistory.timeStamp.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('callHistory.dateTime.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('callHistory.ani.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('callHistory.outboundAni.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('callHistory.dnis.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('callHistory.inboundDnis.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('callHistory.duration.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('callHistory.organization.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('callHistory.callResult.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('callHistory.sessionId.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('callHistory.commonCallId.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('callHistory.ivr.label', null, null) + ",");

        // now append the header for the acd call histories
        tmpFile.append(messageSource.getMessage('acdCallHistory.skill.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('acdCallHistory.enqueueTime.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('acdCallHistory.dequeueTime.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('acdCallHistory.totalQueueTime.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('acdCallHistory.callStatus.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('acdCallHistory.user.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('acdCallHistory.agentCallStart.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('acdCallHistory.agentCallEnd.label', null, null) + ",");
        tmpFile.append(messageSource.getMessage('acdCallHistory.totalAgentTime.label', null, null) + ",");

        tmpFile << "\n";

        //Write each row
        for(AcdCallHistory thisHistory : calls)
        {
            def callHist = CallHistory.findBySessionIdAndToUser(thisHistory.sessionId, thisHistory.user)
            if (callHist) {
                // We start with the rows from the call history table
                tmpFile << "${callHist.dateTime?.getMillis()},"
                tmpFile << "${callHist.dateTime?.toString("yyyy-MM-dd HH:mm:ss")},"
                tmpFile << "${listen.numberWithRealName(number: callHist.ani, user: callHist.fromUser, personalize: false)},"
                tmpFile << "${callHist.outboundAni},"
                tmpFile << "${listen.numberWithRealName(number: callHist.dnis, user: callHist.toUser, personalize: false)},"
                tmpFile << "${callHist.inboundDnis},"
                tmpFile << "${listen.formatduration(duration: callHist.duration, millis: false)},"
                tmpFile << "${callHist.organization.name},"
                tmpFile << "${callHist.result.replaceAll(",", " ")}," // This is to prevent anything weird...
                tmpFile << "${callHist.sessionId},"
                tmpFile << "${callHist.commonCallId},"
                tmpFile << "${callHist.ivr},"
            } else {
                // We start with the rows from the call history table, but in this case we didn't find an entry, so fill in what we can
                tmpFile << ","                                                  // timestamp
                tmpFile << ","                                                  // began
                tmpFile << "${listen.numberWithRealName(number: thisHistory.ani, user: '', personalize: false)},"   // calling party
                tmpFile << ","                                                  // outbound ani
                tmpFile << "${listen.numberWithRealName(number: thisHistory.dnis, user: '', personalize: false)},"  // called party
                tmpFile << ","                                                  // inbound dnis
                tmpFile << ","                                                  // duration
                tmpFile << "${thisHistory?.user?.organization?.name},"          // organization
                tmpFile << ","                                                  // result
                tmpFile << ","                                                  // sessionid
                tmpFile << ","                                                  // common call id
                tmpFile << ","                                                  // ivr
            }

            // We only want to associate acd histories that match the call histories toUser
            // Now we'll add the rows from the acd history record
            tmpFile << "${thisHistory.skill},"
            tmpFile << "${thisHistory.enqueueTime?.toString("yyyy-MM-dd HH:mm:ss")},"
            tmpFile << "${thisHistory.dequeueTime?.toString("yyyy-MM-dd HH:mm:ss")},"
            tmpFile << "${listen.computeDuration(start: thisHistory.enqueueTime, end:thisHistory.dequeueTime)},"
            tmpFile << "${thisHistory.callStatus.name()},"
            if(thisHistory.user != null)
            {
                tmpFile << "${thisHistory.user.username},";
            }
            else
            {
                tmpFile << ",";
            }
            tmpFile << "${thisHistory.agentCallStart?.toString("yyyy-MM-dd HH:mm:ss")},"
            tmpFile << "${thisHistory.agentCallEnd?.toString("yyyy-MM-dd HH:mm:ss")},"
            if(thisHistory.agentCallEnd != null && thisHistory.agentCallEnd != null)
            {
                tmpFile << "${listen.computeDuration(start: thisHistory.agentCallStart, end:thisHistory.agentCallEnd)},"
            }
            else
            {
                DateTime now = DateTime.now();
                tmpFile << "${listen.computeDuration(start: now, end: now)},"
            }

            tmpFile << "\n";
        }

        if(log.isDebugEnabled())
        {
            log.debug "Generated ACD Call History Records report of size [${tmpFile.length()}]"
        }

        //Now write the outputfile
        response.contentType = 'text/csv';
        response.setHeader('Content-disposition', "attachment;filename=" + filename);
        response.setHeader('Content-length', "${tmpFile.length()}")

        OutputStream outStream = new BufferedOutputStream(response.outputStream)
        InputStream inStream = tmpFile.newInputStream()

        byte[] bytes = new byte[4096]
        int bytesRead;

        while((bytesRead = inStream.read(bytes)) != -1)
        {
            outStream.write(bytes, 0, bytesRead)
        }

        //Close down
        inStream.close()
        outStream.flush()
        outStream.close()

        if (!tmpFile.delete())
        {
            log.error("Failed to delete temporary file [${tmpFile.getName()}]")
        }
        else if(log.isDebugEnabled())
        {
            log.debug("Succeeded in deleting temporary file [${tmpFile.getName()}]")
        }
    }

    def acdCallHistory =
    {
        if (!springSecurityService.currentUser) {
            //Redirect to login
            redirect(controller: 'login', action: 'auth');
        }
        def user = springSecurityService.currentUser

        log.debug "AcdController.acdCallHistory: params[${params}]";

        log.debug "params.sort [${params.sort}]"
        log.debug "params.order [${params.order}]"

        params.sort = params.sort ?: 'enqueueTime'
        params.order = params.order ?: 'desc'
        params.max = params.max ?: '100'
        params.offset = params.offset ?: '0'

        log.debug "params.sort [${params.sort}]"
        log.debug "params.order [${params.order}]"

        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        DateTime theStart = getStartDate(params.startDate);
        DateTime theEnd = getEndDate(params.endDate);

        if ( !user.hasRole( 'ROLE_ORGANIZATION_ADMIN' ) ) {
            params.agent = params.agent ?: user.id
        }
        params.agent = params.agent ?: ""
        def calls = acdService.acdHistoryList(params.sort, params.order, params.max, params.offset, theStart, theEnd,
                params.agent.toString(), params.skill, user.organization);
        def callTotal = calls.totalCount;

        def json = [:]
        def callJson = []
        for (AcdCallHistory call : calls)
        {
            def c = [:]
            c.ani = call.ani
            c.skill = call.skill.description
            c.callStatus = call.callStatus.viewable()
            c.agentCallStart = call.agentCallStart
            c.agentCallEnd = call.agentCallEnd
            c.enqueueTime = call.enqueueTime
            c.dequeueTime = call.dequeueTime
            c.user = ""
            if (call.user != null)
            {
                c.user = call.user.realName
            }
            callJson.add(c)
        }

        json.calls = callJson
        json.callTotal = callTotal
        json.sort = params.sort
        json.order = params.order
        json.max = params.max
        json.offset = params.offset
        json.filtered = true
        json.startDate =  dtf.print(theStart);
        json.endDate = dtf.print(theEnd);
        json.agent = params.agent;

        def agentJson = [];
        List<User> agents = []
        if ( user.hasRole('ROLE_ORGANIZATION_ADMIN') )
        {
            // find all agents
            agents = acdService.getAcdAgentList(user.organization);
        }
        else
        { // list only the current user
            agents.add(user)
        }

        for (User agent : agents)
        {
            def d = [:]
            d.id = agent.id;
            d.realName = agent.realName;
            agentJson.add(d);
        }

        json.skill = params.skill;

        def skillJson = [];
        List<Skill> skills = [];

        if ( user.hasRole('ROLE_ORGANIZATION_ADMIN') )
        {
            // find all skills
            skills = Skill.createCriteria().list(sort: "skillname")
            {
                eq('organization', user.organization)
            }
        }
        else
        {
            // find only skills associated with current user
            UserSkill.findAllByUser(user).each() { userSkill ->
                skills.add(userSkill.skill)
            }
        }
        for (Skill skill : skills)
        {
            def e = [:]
            e.id = skill.id;
            e.skillname = skill.description;
            skillJson.add(e);
        }

        json.skillList = skillJson;
        json.agentList = agentJson;

        render(view: 'acdCallHistory', model: json)
    }

    def agentStatus =
    {
        if (log.isDebugEnabled())
        {
            log.debug "AcdController.agentStatus: params[${params}]";
        }

        Organization userOrg = springSecurityService.currentUser.organization;
        List<Skill> skills = Skill.findAllByOrganization(userOrg, [sort: 'skillname', order: 'asc']);

        List<AcdCall> calls = AcdCall.list();
        Map<User, AcdCall> callMap = new HashMap<User,AcdCall>();
        for(AcdCall thisCall : calls)
        {
            if(thisCall != null && thisCall.user != null)
            {
                callMap.put(thisCall.user, thisCall);
            }
        }

        def skillJson = [];

        for(Skill thisSkill : skills)
        {
            def userSkill = [:];
            def agents = [];

            List<UserSkill> userSkills = thisSkill.userSkill.sort{it.user.realName};
            for(UserSkill thisUserSkill : userSkills)
            {
                def userJson = [:];

                //Don't include voicemailbox users
                if(thisUserSkill.user.acdUserStatus.AcdQueueStatus == AcdQueueStatus.VoicemailBox)
                {
                    continue;
                }

                //Don't include users if they are disabled
                if(!thisUserSkill.user.enabled)
                {
                    log.info("User [${thisUserSkill.user.username}] skipped because it's disabled")
                    continue;
                }
                userJson.agentId = thisUserSkill.user.id;
                userJson.agent = thisUserSkill.user.realName;
                userJson.status = thisUserSkill.user.acdUserStatus.AcdQueueStatus;
                userJson.priority = thisUserSkill.priority;
                if(thisUserSkill.user.acdUserStatus.onacallModified != null)
                {
                    userJson.lastCall = thisUserSkill.user.acdUserStatus.onacallModified;
                }
                if(thisUserSkill.user.acdUserStatus.statusModified != null)
                {
                    userJson.statusModified = thisUserSkill.user.acdUserStatus.statusModified;
                }
                if(callMap && callMap.containsKey(thisUserSkill.user))
                {
                    AcdCall userCall = callMap.get(thisUserSkill.user);
                    //Make sure the right skill
                    if(userCall.skill == thisSkill)
                    {
                        userJson.callStart = userCall.callStart;
                    }
                }

                agents.add(userJson);
            }

            userSkill.skill = thisSkill.description;
            userSkill.agents = agents;
            skillJson.add(userSkill);
        }

        def model = [
                skills: skillJson
        ]

        render(view: 'agentStatus', model: model);
    }

    private def getStartDate(String inputStart)
    {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        DateTime theStart;

        if(inputStart && !inputStart.isEmpty())
        {
            theStart = DateTime.parse(inputStart, dtf);
        }
        else
        {
            theStart = DateTime.now().minusDays(1);
        }

        theStart = theStart.withHourOfDay(0);
        theStart = theStart.withMinuteOfHour(0);
        theStart = theStart.withSecondOfMinute(0);

        return theStart;
    }

    private def getEndDate(String inputEnd)
    {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        DateTime theEnd;

        if(inputEnd && !inputEnd.isEmpty())
        {
            theEnd = DateTime.parse(inputEnd, dtf);
        }
        else
        {
            theEnd = DateTime.now();
        }

        theEnd = theEnd.withHourOfDay(23);
        theEnd = theEnd.withMinuteOfHour(59);
        theEnd = theEnd.withSecondOfMinute(59);

        return theEnd;
    }
}
