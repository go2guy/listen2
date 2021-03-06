package com.interact.listen

import com.interact.listen.acd.UserSkill
import com.interact.listen.history.ActionHistory
import com.interact.listen.history.CallHistory
import com.interact.listen.pbx.Extension
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.log4j.Logger
import javax.servlet.http.HttpSession

@Secured(['IS_AUTHENTICATED_FULLY'])
class ProfileController {
    def extensionService
    def historyService
    def mobilePhoneService
    def otherPhoneService
    def realizeAlertUpdateService
    def springSecurityService
    def userService

    static allowedMethods = [
        index: 'GET',
        addMobilePhone: 'POST',
        addOtherPhone: 'POST',
        afterHours: 'GET',
        canDial: 'GET',
        deleteMobilePhone: 'POST',
        deleteOtherPhone: 'POST',
        settings: 'GET',
        history: 'GET',
        phones: 'GET',
        saveAfterHours: 'POST',
        saveSettings: 'POST',
        updateExtension: 'POST',
        updateDid: 'POST',
        updateMobilePhone: 'POST',
        updateOtherPhone: 'POST'
    ]

    def index = {
        redirect(action: 'settings')
    }

    def addMobilePhone = {
        def mobilePhone = mobilePhoneService.create(params)
        if(mobilePhone.hasErrors()) {
            def model = phonesModel()
            model.newMobilePhone = mobilePhone
            render(view: 'phones', model: model)
        } else {
            flash.successMessage = 'Mobile phone saved'
            redirect(action: 'phones')
        }
    }

    def addOtherPhone = {
        def otherPhone = otherPhoneService.create(params)
        if(otherPhone.hasErrors()) {
            def model = phonesModel()
            model.newOtherPhone = otherPhone
            render(view: 'phones', model: model)
        } else {
            flash.successMessage = 'Other phone saved'
            redirect(action: 'phones')
        }
    }

    def afterHours = {
        def user =  springSecurityService.currentUser;
        def afterHoursConfiguration = AfterHoursConfiguration.findByOrganization(user.organization)
        render(view: 'afterHours', model: [afterHoursConfiguration: afterHoursConfiguration])
    }

    // ajax
    def canDial = {
        def number = params.number
        boolean canDial = true

        if(number) {
            def user =  springSecurityService.currentUser;
            canDial = user.canDial(number)
        }

        render(contentType: 'application/json') {
            delegate.canDial = canDial
        }
    }

    def deleteMobilePhone = {
        def mobilePhone = MobilePhone.get(params.id)
        if(!mobilePhone) {
            flash.errorMessage = 'Mobile phone not found'
            redirect(action: 'phone')
            return
        }

        mobilePhoneService.delete(mobilePhone)
        flash.successMessage = 'Mobile phone deleted'
        redirect(action: 'phones')
    }

    def deleteOtherPhone = {
        def otherPhone = OtherPhone.get(params.id)
        if(!otherPhone) {
            flash.errorMessage = 'Other phone not found'
            redirect(action: 'phone')
            return
        }

        otherPhoneService.delete(otherPhone)
        flash.successMessage = 'Other phone deleted'
        redirect(action: 'phones')
    }

    def settings =
    {
        User user = springSecurityService.currentUser;
        List<UserSkill> userSkills = null;

        if(user.hasRole("ROLE_ACD_USER"))
        {
            userSkills = UserSkill.findAllByUser(user);
            if(userSkills == null)
            {
                userSkills = new ArrayList<UserSkill>(0);
            }
        }

        render(view: 'settings', model: [user: user, userSkills: userSkills]);
    }

    def history = {
        def user = springSecurityService.currentUser;

        // TODO since there are two tables on the page, none of the sorting
        // is being used yet. also, both tables will page together (i.e. not separately).
        // we should investigate an elegant way to accomplish this. its likely that
        // these pages will be changed in the future to display the data in a way that is
        // more appropriate to how they're supposed to be viewed. this should be considered
        // at that time.

        params.offset = params.offset ? params.int('offset') : 0
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        
        // call history
        def callHistoryList = CallHistory.createCriteria().list([offset: params.offset, max: params.max, sort: 'dateTime', order: 'desc']) {
            or {
                eq('fromUser', user)
                eq('toUser', user)
            }
        }
        def callHistoryTotal = CallHistory.createCriteria().get {
            projections {
                count('id')
            }
            or {
                eq('fromUser', user)
                eq('toUser', user)
            }
        }

        // action history
        def actionHistoryList = ActionHistory.createCriteria().list([offset: params.offset, max: params.max, sort: 'dateCreated', order: 'desc']) {
            or {
                eq('byUser', user)
                eq('onUser', user)
            }
        }
        def actionHistoryTotal = ActionHistory.createCriteria().get {
            projections {
                count('id')
            }
            or {
                eq('byUser', user)
                eq('onUser', user)
            }
        }
        render(view: 'history', model: [callHistoryList: callHistoryList, callHistoryTotal: callHistoryTotal, actionHistoryList: actionHistoryList, actionHistoryTotal: actionHistoryTotal])
    }

    def phones = {
        // TODO custodian users shouldnt really be able to get here, or to any other phone actions
        def model = phonesModel()
        log.debug "Profile controller rendering phone view [${model.extensionList.size()}][${model.mobilePhoneList.size()}][${model.otherPhoneList.size()}]"
        render(view: 'phones', model: model)
    }

    def saveAfterHours = {
        def user = springSecurityService.currentUser;
        def afterHours = AfterHoursConfiguration.findByOrganization(user.organization)
        if(!afterHours) {
            flash.errorMessage = 'After Hours has not been configured by an administrator; alternate number cannot be set.'
            redirect(action: 'afterHours')
            return
        }

        def originalAlternateNumber = afterHours.alternateNumber
        if(params.alternateNumber?.trim() == '') {
            afterHours.alternateNumber = ''
        } else {
            afterHours.alternateNumber = params.alternateNumber + '@' + params.provider
        }

        if(afterHours.validate() && afterHours.save()) {
            if(originalAlternateNumber != afterHours.alternateNumber) {
                realizeAlertUpdateService.sendUpdate(afterHours, originalAlternateNumber)
                historyService.changedAfterHoursAlternateNumber(afterHours, originalAlternateNumber)
            }

            flash.successMessage = 'After Hours alternate number updated'
            redirect(action: 'afterHours')
        } else {
            render(view: 'afterHours', model: [afterHoursConfiguration: afterHours])
        }
    }

    def saveSettings = {
        def user =  springSecurityService.currentUser;
        user = userService.update(user, params, false)
        if(user.hasErrors()) {
            render(view: 'settings', model: [user: user])
        } else {
            flash.successMessage = 'Saved'
            redirect(action: 'settings')
        }
    }

    def updateExtension = {
        def extension = Extension.get(params.id)
        if(!extension) {
            flash.errorMessage = 'Extension not found'
            redirect(action: 'phone')
            return
        }
        def organization = session.organization

        def extInfo = extensionService.update(params, extension, organization)
        if(extInfo.extension?.hasErrors() || extInfo?.sipPhone?.hasErrors()) {
            def model = phonesModel()
            model.updatedExtension = extInfo.extension
            render(view: 'phones', model: model)
        } else {
            flash.successMessage = 'Extension updated'
            redirect(action: 'phones')
        }
    }

    def updateDid = {
        log.debug "updateDid with params [${params}]"
        def did = DirectInwardDialNumber.get(params.id)
        if(!did) {
            flash.errorMessage = 'DID not found'
            redirect(action: 'phone')
            return
        }

        did.properties['forwardedTo', 'greeting'] = params
        // Don't want any non-numeric characters
        did.forwardedTo = String.valueOf(params.forwardedTo).replaceAll("\\D+", "")

        if(did.validate() && did.save()) {
            log.error "We've saved the did properties"
            flash.successMessage = 'Direct Inward Dial number updated'
            redirect(action: 'phones')
        } else {
            def model = phonesModel()
            model.updatedDid = did
            render(view: 'phones', model: model)
        }
    }
    
    def updateMobilePhone = {
        def mobilePhone = MobilePhone.get(params.id)
        if(!mobilePhone) {
            flash.errorMessage = 'Mobile phone not found'
            redirect(action: 'phone')
            return
        }

        mobilePhone = mobilePhoneService.update(mobilePhone, params)
        if(mobilePhone.hasErrors()) {
            def model = phonesModel()
            model.updatedMobilePhone = mobilePhone
            render(view: 'phones', model: model)
        } else {
            flash.successMessage = 'Mobile phone saved'
            redirect(action: 'phones')
        }
    }

    def updateOtherPhone = {
        def otherPhone = OtherPhone.get(params.id)
        if(!otherPhone) {
            flash.errorMessage = 'Other phone not found'
            redirect(action: 'phone')
            return
        }

        otherPhone = otherPhoneService.update(otherPhone, params)
        if(otherPhone.hasErrors()) {
            def model = phonesModel()
            model.updatedOtherPhone = otherPhone
            render(view: 'phones', model: model)
        } else {
            flash.successMessage = 'Other phone saved'
            redirect(action: 'phones')
        }
    }

    private def phonesModel() {
        def user = springSecurityService.currentUser;
        def extensionList = Extension.withCriteria {
            eq('owner', user)
        }
        def mobilePhoneList = MobilePhone.withCriteria {
            eq('owner', user)
        }
        def otherPhoneList = OtherPhone.withCriteria {
            eq('owner', user)
        }
        def externalDIDList = DirectInwardDialNumber.withCriteria {
            eq('owner', user)
        }
        def externalDMNList = DirectMessageNumber.withCriteria {
            eq('owner', user)
        }
        return [
            extensionList: extensionList,
            externalDIDList: externalDIDList,
            mobilePhoneList: mobilePhoneList,
            otherPhoneList: otherPhoneList,
            externalDMNList: externalDMNList
        ]
    }
}
