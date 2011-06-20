package com.interact.listen.voicemail

import com.interact.listen.pbx.NumberRoute

class VoicemailNotificationService {
    static scope = 'singleton'
    static transactional = false

    def backgroundService
    def grailsApplication
    def historyService

    void sendNewVoicemailEmail(Voicemail voicemail) {
        def preferences = VoicemailPreferences.findByUser(voicemail.owner)
        if(!preferences) {
            log.warn "No VoicemailPreferences configured for user [${voicemail.owner}]"
            return
        }

        if(!preferences.isEmailNotificationEnabled) {
            log.debug "User is not set to receive e-mail notifications"
            return
        }

        if(!emailTimeRestrictionsAllow(preferences)) {
            log.debug "Time restrictions disallow sending notification"
            return
        }

        def address = preferences.emailNotificationAddress
        def voicemailNumber = directVoicemailNumber(voicemail.owner.organization)
        def retrieve = voicemailNumber ? "Retrieve it at <b>${voicemailNumber.pattern}</b>.<br/><br/>" : ''
        def newCount = Voicemail.countByOwnerAndIsNew(voicemail.owner, true)

        // TODO handle IOException reading file (set file to null)
        def attachedFileName = "Voicemail-${voicemail.dateCreated}"
        def file
        try {
            file = download(voicemail.audio.uri)
        } catch(IOException e) {
            log.warn "Unable to download audio file [${voicemail.audio.uri}]"
        }

        def subj = "New voicemail from ${voicemail.from()}"
        def body = """
<html><body>
You received a new voicemail from ${voicemail.from()} at ${voicemail.dateCreated}.<br/><br/>

${retrieve}

${voicemail.audio.transcription != '' ? '<i>' + voicemail.audio.transcription + '</i><br/><br/>' : ''}

You currently have ${newCount} new message${newCount != 1 ? 's' : ''}.<br/><br/>

${file ? 'The voicemail is attached' : '(The voicemail could not be attached to this message. Contact a system administrator for assistance.)'}
</body></html>
"""

        backgroundService.execute("New voicemail email to [${voicemail.owner.username}] at [${address}] for voicemail id [${voicemail.id}]", {
            sendMail {
                if(file) {
                    // 'multipart true' must be first for multipart messages
                    multipart true
                }

                from 'Listen'
                to address
                subject subj
                html body
                // call 'attach' last, see http://jira.grails.org/browse/GPMAIL-60
                if(file) {
                    attach "${attachedFileName}", "application/octet-stream", file.bytes
                }
            }

            // TODO send stat
            historyService.sentNewVoicemailEmail(voicemail)

            if(file) {
                file.delete()
            }
        })
    }

    void sendNewVoicemailTestEmail(String address) {
        backgroundService.execute("New voicemail test email to [${address}]", {
            sendMail {
                from 'Listen'
                to address
                subject 'Listen Notification Test Message'
                html """
<html><body>
Hello,<br/><br/>

You have correctly configured your settings to receive Listen email notifications at this address.
</html></body>
"""
            }
        })
    }

    void sendNewVoicemailSms(Voicemail voicemail, def toAddress = null) {
        def preferences = VoicemailPreferences.findByUser(voicemail.owner)
        if(!preferences) {
            log.warn "No VoicemailPreferences configured for user [${voicemail.owner}]"
            return
        }

        if(!preferences.isSmsNotificationEnabled) {
            log.debug "User is not set to receive sms notifications"
            return
        }

        if(!smsTimeRestrictionsAllow(preferences)) {
            log.debug "Time restrictions disallow sending notification"
            return
        }

        def address = toAddress ?: preferences.smsNotificationAddress
        if(!address) {
            log.debug "VoicemailPreferences.smsNotificationAddress is not set"
            return
        }

        def voicemailFrom = voicemail.from()
        def transcription = voicemail.audio.transcription
        def voicemailNumber = directVoicemailNumber(voicemail.owner.organization)

        backgroundService.execute("New voicemail SMS to [${voicemail.owner.username}] at [${address}] for voicemail id [${voicemail.id}]", {
            def max = 160
            def message = "New voicemail from ${voicemailFrom}. ${transcription}"
            def retrieve = voicemailNumber ? " Retreieve it at ${voicemailNumber.pattern}." : ''
            if(message.size() + retrieve.size() <= max) {
                message += retrieve
            } else if(message.size() > max) {
                message = message[0..(max - 1)]
            }

            sendMail {
                from 'Listen'
                to address
                subject "New voicemail from ${voicemailFrom}"
                body message
            }

            // TODO send stat
        })
        historyService.sentNewVoicemailSms(voicemail)
    }

    void sendNewVoicemailTestSms(String address) {
        backgroundService.execute("New voicemail test SMS to addres [${address}]", {
            sendMail {
                from 'Listen'
                to address
                subject 'Listen Notification Test Message'
                body 'You have correctly configured your settings to receive SMS notifications at this address'
            }
        })
    }

    private boolean emailTimeRestrictionsAllow(VoicemailPreferences preferences) {
        return restrictionsAllow(preferences.emailTimeRestrictions)
    }

    private boolean smsTimeRestrictionsAllow(VoicemailPreferences preferences) {
        return restrictionsAllow(preferences.smsTimeRestrictions)
    }

    private boolean restrictionsAllow(def restrictions) {
        if(restrictions.size() == 0) {
            // no restrictions, allow everything
            return true
        }
        return restrictions.any { it.appliesToNow() }
    }

    private File download(String uri) {
        def file = File.createTempFile('Voicemail', null)
        file.deleteOnExit()
        def out = new BufferedOutputStream(new FileOutputStream(file))
        out << new URL(uri).openStream()
        out.close()
        return file
    }

    private def directVoicemailNumber(def organization) {
        // TODO hard-coded destination application
        def routes = NumberRoute.findAllByDestination('Mailbox').findAll { !it.pattern.contains('*') }
        if(routes.size() == 0) {
            return null
        }
        return routes.sort { it.pattern.size() }.reverse()[0]
    }
}