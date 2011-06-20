package com.interact.listen.conferencing

import grails.plugins.springsecurity.Secured
import org.joda.time.LocalDate
import org.joda.time.LocalTime

@Secured(['ROLE_CONFERENCE_USER'])
class ConferencingController {
    static allowedMethods = [
        index: 'GET',
        ajaxPagination: 'GET',
        deleteRecording: 'POST',
        downloadRecording: 'GET',
        dropCaller: 'POST',
        manage: 'GET',
        muteCaller: 'POST',
        outdial: 'POST',
        polledConference: 'GET',
        recordings: 'GET',
        schedule: 'POST',
        scheduling: 'GET',
        startRecording: 'POST',
        stopRecording: 'POST',
        unmuteCaller: 'POST'
    ]

    def audioDownloadService
    def deleteRecordingService
    def grailsApplication
    def scheduledConferenceNotificationService
    def spotCommunicationService
    def springSecurityService

    def index = {
        redirect(action: 'manage')
    }

    def ajaxPagination = {
        render listen.paginateTotal(total: params.total, messagePrefix: 'paginate.total.callers') + g.paginate(controller: params.c, action: params.a, total: params.total, max: params.max, offset: params.offset, params: [sort: params.sort, order: params.order], maxsteps: 5)
    }

    def deleteRecording = {
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max

        def recording = Recording.get(params.id)
        if(!recording) {
            flash.errorMessage = 'Recording not found'
            redirect(action: 'recordings', params: preserve)
        }

        def user = springSecurityService.getCurrentUser()
        if(recording.conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        deleteRecordingService.deleteRecording(recording)

        flash.successMessage = 'Recording deleted'
        redirect(action: 'recordings', params: preserve)
    }

    def downloadRecording = {
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max

        def recording = Recording.get(params.id)
        if(!recording) {
            flash.errorMessage = 'Recording not found'
            redirect(action: 'recordings', params: preserve)
        }

        def user = springSecurityService.getCurrentUser()
        if(recording.conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        // TODO stat? history?
        audioDownloadService.download(recording.audio, response)
    }

    // ajax
    def polledConference = {
        def conference = Conference.get(params.id)
        if(!conference) {
            response.sendError(404)
            return
        }

        params.sort = params.sort ?: 'ani'
        params.order = params.order ?: 'asc'
        params.max = 10
        params.offset = params.offset ?: 0

        def participants = Participant.findAllByConference(conference, params)
        def total = Participant.countByConference(conference)

        def json = [:]

        def c = [:]
        c.isStarted = conference.isStarted
        c.isRecording = conference.isRecording
        c.started = c.isStarted ? listen.prettytime(date: conference.startTime) : ''

        def p = [:]
        p.total = total
        p.max = params.max
        p.offset = params.offset
        p.sort = params.sort
        p.order = params.order
        p.ids = participants.collect { it.id }
        p.list = participants.collect {
            [id: it.id,
             displayName: it.displayName(),
             joined: listen.prettytime(date: it.dateCreated),
             isAdmin: it.isAdmin,
             isAdminMuted: it.isAdminMuted,
             isMuted: it.isMuted,
             isPassive: it.isPassive]
        }

        json.participants = p
        json.conference = c

        render(contentType: 'application/json') {
            json
        }
    }

    def dropCaller = {
        def participant = Participant.get(params.id)
        if(!participant) {
            flash.errorMessage = 'Participant not found'
            redirect(action: 'manage')
            return
        }

        def user = springSecurityService.getCurrentUser()
        if(participant.conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        if(participant.isAdmin) {
            flash.errorMessage = 'Cannot drop admin caller'
            redirect(action: 'manage')
            return
        }

        spotCommunicationService.dropParticipant(participant)
        flash.successMessage = 'Caller dropped'
        redirect(action: 'manage')
    }

    def manage = {
        def user = springSecurityService.getCurrentUser()
        // TODO only works for current user, needs to be usable by admins
        def conference = Conference.findByOwner(user)
        if(!conference) {
            // TODO render a conference not found view
            throw new AssertionError("User ${user} does not have a conference")
        }

        params.sort = params.sort ?: 'ani'
        params.order = params.order ?: 'asc'
        params.max = 10
        params.offset = params.offset ?: 0

        def participantList = Participant.findAllByConference(conference, params)
        def participantTotal = Participant.countByConference(conference)
        render(view: 'manage', model: [conference: conference, participantList: participantList, participantTotal: participantTotal])
    }

    def muteCaller = {
        def participant = Participant.get(params.id)
        if(!participant) {
            flash.errorMessage = 'Participant not found'
            redirect(action: 'manage')
            return
        }

        def user = springSecurityService.getCurrentUser()
        if(participant.conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        if(participant.isAdmin) {
            flash.errorMessage = 'Cannot mute admin caller'
            redirect(action: 'manage')
            return
        } else if(participant.isPassive) {
            flash.errorMessage = 'Cannot mute passive caller'
            redirect(action: 'manage')
            return
        }

        spotCommunicationService.muteParticipant(participant)
        flash.successMessage = 'Caller muted'
        redirect(action: 'manage')
    }

    def outdial = {
        // TODO stat?

        def conference = Conference.get(params.id)
        if(!conference) {
            flash.errorMessage = 'Conference not found'
            redirect(action: 'manage')
            return
        }

        def user = springSecurityService.getCurrentUser()
        if(conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        if(!params.onDemandNumber) {
            flash.errorMessage = 'Missing number to outdial'
            redirect(action: 'manage')
            return
        }

        def automated = (params.onDemandMode != 'interactive')
        def number = params.onDemandNumber.replaceAll(' ', '')
        def requestingNumber = grailsApplication.config.com.interact.listen.phoneNumber

        if(!user.canDial(number)) {
            flash.errorMessage = 'You are not allowed to dial that number'
            redirect(action: 'manage')
            return
        }

        log.debug "Outdialing to number [${number}] for conference id [${conference.id}], automated = [${automated}]"

        if(automated) {
            spotCommunicationService.outdial(number, conference, requestingNumber)
        } else {
            spotCommunicationService.interactiveOutdial(number, conference, requestingNumber)
        }

        flash.successMessage = 'Bridging admin conference user for On Demand dial'
        redirect(action: 'manage')
    }

    def recordings = {
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        params.offset = params.offset ? params.int('offset') : 0
        params.sort = params.sort ?: 'audio.dateCreated'
        params.order = params.order ?: 'desc'

        def user = springSecurityService.getCurrentUser()
        // TODO doesnt work with multiple conferences per user
        def conference = Conference.findByOwner(user)
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        def recordingList = Recording.findAllByConference(conference, params)
        def recordingTotal = Recording.countByConference(conference)
        render(view: 'recordings', model: [recordingList: recordingList, recordingTotal: recordingTotal])
    }

    def schedule = {
        def user = springSecurityService.getCurrentUser()
        // TODO doesnt work with multiple conferences per user
        def conference = Conference.findByOwner(user)
        if(!conference) {
            flash.errorMessage = 'User does not have a conference to schedule'
            redirect(action: 'scheduling')
            return
        }

        def scheduledConference = new ScheduledConference()
        scheduledConference.properties = params
        scheduledConference.forConference = conference
        scheduledConference.scheduledBy = user

        if(scheduledConference.validate() && scheduledConference.save()) {
            scheduledConferenceNotificationService.sendEmails(scheduledConference)
            flash.successMessage = 'Conference has been scheduled and email invitations have been sent'
            redirect(action: 'scheduling')
        } else {
            render(view: 'scheduling', model: [scheduledConference: scheduledConference, scheduleLists: scheduleLists(user)])
        }
    }

    def scheduling = {
        render(view: 'scheduling', model: [scheduleLists: scheduleLists(springSecurityService.getCurrentUser())])
    }

    private def scheduleLists(def user) {
        def future = ScheduledConference.withCriteria {
            eq('scheduledBy', user)
            or {
                and {
                    eq('date', new LocalDate())
                    ge('starts', new LocalTime())
                }
                gt('date', new LocalDate())
            }
            order('date', 'asc')
            order('starts', 'asc')
        }
        def past = ScheduledConference.withCriteria {
            eq('scheduledBy', user)
            or {
                and {
                    eq('date', new LocalDate())
                    lt('starts', new LocalTime())
                }
                lt('date', new LocalDate())
            }
            order('date', 'asc')
            order('starts', 'asc')
        }
        return [future: future, past: past]
    }

    def startRecording = {
        def conference = Conference.get(params.id)
        if(!conference) {
            flash.errorMessage = 'Conference not found'
            redirect(action: 'manage')
            return
        }

        def user = springSecurityService.getCurrentUser()
        if(conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        spotCommunicationService.startRecording(conference)
        flash.successMessage = 'Conference recording started'
        redirect(action: 'manage')
    }

    def stopRecording = {
        def conference = Conference.get(params.id)
        if(!conference) {
            flash.errorMessage = 'Conference not found'
            redirect(action: 'manage')
            return
        }

        def user = springSecurityService.getCurrentUser()
        if(conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        spotCommunicationService.stopRecording(conference)
        flash.successMessage = 'Conference recording stopped'
        redirect(action: 'manage')
    }

    def unmuteCaller = {
        def participant = Participant.get(params.id)
        if(!participant) {
            flash.errorMessage = 'Participant not found'
            redirect(action: 'manage')
            return
        }

        def user = springSecurityService.getCurrentUser()
        if(participant.conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        if(participant.isAdmin) {
            flash.errorMessage = 'Cannot unmute admin caller'
            redirect(action: 'manage')
            return
        } else if(participant.isPassive) {
            flash.errorMessage = 'Cannot unmute passive caller'
            redirect(action: 'manage')
            return
        }

        spotCommunicationService.unmuteParticipant(participant)
        flash.successMessage = 'Caller unmuted'
        redirect(action: 'manage')
    }
}