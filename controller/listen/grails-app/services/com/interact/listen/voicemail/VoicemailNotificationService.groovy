package com.interact.listen.voicemail

import com.interact.listen.User
import com.interact.listen.fax.Fax
import com.interact.listen.pbx.NumberRoute
import com.interact.listen.stats.Stat

class VoicemailNotificationService {
    static transactional = false

    def backgroundService
    def grailsApplication
    def historyService
    def statWriterService

	void changeAccountEmailAddress(User user, String originalEmailAddress, String newEmailAddress) {
		def preferences = VoicemailPreferences.findByUser(user)

		if (!preferences) {
			log.warn "No VoicemailPreferences configured for user [${user.username}]"
			return
		}

		if(!preferences.isEmailNotificationEnabled) {
			log.debug "user is not set to receive e-mail notifications"
			return
		}

		if (originalEmailAddress == preferences.emailNotificationAddress) {
			preferences.emailNotificationAddress = newEmailAddress

			if(preferences.validate() && preferences.save()) {
				log.debug "Voicemail preferences email successfully updated"
			} else {
				log.error "We failed to update voicemail preferences email"
			}
		}

		return
	}

    void sendNewVoicemailEmail(Voicemail voicemail) {
        def preferences = VoicemailPreferences.findByUser(voicemail.owner)
        
        if(!preferences) {
            log.warn "No VoicemailPreferences configured for user [${voicemail.owner}]"
            return
        }

        if(!preferences.isEmailNotificationEnabled) {
            log.debug "User is not set to receive e-mail notifications"
            return
        } else {
            log.debug "New voicemail email to [${voicemail.owner.username}] at [${preferences.emailNotificationAddress}] for voicemail id [${voicemail.id}]"
        }

        if(!emailTimeRestrictionsAllow(preferences)) {
            log.debug "Time restrictions disallow sending notification"
            return
        }

        def address = preferences.emailNotificationAddress
        def voicemailNumber = directMailboxNumber(voicemail.owner.organization)
        def retrieve = voicemailNumber ? "Retrieve it at <b>${voicemailNumber.pattern}</b>.<br/><br/>" : ''
        def newCount = Voicemail.countByOwnerAndIsNew(voicemail.owner, true)

        // TODO handle IOException reading file (set file to null)
        def attachedFileName = "Voicemail-${voicemail.dateCreated}${voicemail.audio.file.name.substring(voicemail.audio.file.name.lastIndexOf("."))}"
        def file = voicemail.audio.file

        def subj = "New voicemail from ${voicemail.from()}"
        log.debug "voice mail subject [${subj}]"
        def myBody = """
<html><body>
You received a new voicemail from ${voicemail.from()} at ${voicemail.dateCreated}.<br/><br/>

${retrieve}

${voicemail.audio.transcription == '' ? '': '<i>' + voicemail.audio.transcription + '</i><br/><br/>'}

You currently have ${newCount} new message${newCount == 1 ? '' : 's'}.<br/><br/>

${file ? 'The voicemail is attached' : '(The voicemail could not be attached to this message. Contact a system administrator for assistance.)'}
</body></html>
"""

        log.debug "Kick email processing into the background"
        backgroundService.execute("Background-New voicemail email to [${voicemail.owner.username}] at [${address}] for voicemail id [${voicemail.id}]", {

            sendMail {
                if(file) {
                    // 'multipart true' must be first for multipart messages
                    multipart true
                }

                to address
                subject subj
                html myBody
                // call 'attach' last, see http://jira.grails.org/browse/GPMAIL-60
                if(file) {
                    attach "${attachedFileName}", "application/octet-stream", file.bytes
                }
            }
            log.debug "Background-after sendMail"
            statWriterService.send(Stat.NEW_VOICEMAIL_EMAIL)
            log.debug "Background-after stat writer service"
            historyService.sentNewVoicemailEmail(voicemail)
            log.debug "Background-after history written"
        })
        
        log.debug "After background service sendMail"
    }
    
    void sendNewFaxEmail(Fax fax) {
            def preferences = VoicemailPreferences.findByUser(fax.owner)
            if(!preferences) {
                log.warn "No VoicemailPreferences configured for user [${fax.owner}]"
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
            def newCount = Fax.countByOwnerAndIsNew(fax.owner, true)
    
            // TODO handle IOException reading file (set file to null)
            def attachedFileName = "Fax-${fax.dateCreated}${fax.file.name.substring(fax.file.name.lastIndexOf("."))}"
            def file = fax.file
    
            def subj = "New fax from ${fax.from()}"
            def myBody = """
    <html><body>
    You received a new fax from ${fax.from()} at ${fax.dateCreated}.<br/><br/>

    You currently have ${newCount} new fax${newCount == 1 ? '' : 'es'}.<br/><br/>
    
    ${file ? 'The fax is attached' : '(The fax could not be attached to this message. Contact a system administrator for assistance.)'}
    </body></html>
    """
    
            backgroundService.execute("New fax email to [${fax.owner.username}] at [${address}] for fax id [${fax.id}]", {
                sendMail {
                    if(file) {
                        // 'multipart true' must be first for multipart messages
                        multipart true
                    }
    
                    to address
                    subject subj
                    html myBody
                    // call 'attach' last, see http://jira.grails.org/browse/GPMAIL-60
                    if(file) {
                        attach "${attachedFileName}", "application/octet-stream", file.bytes
                    }
                }
    
                statWriterService.send(Stat.NEW_FAX_EMAIL)
                historyService.sentNewFaxEmail(fax)
            })
    }

    void sendNewVoicemailTestEmail(String address) {
        backgroundService.execute("New voicemail test email to [${address}]", {
            sendMail {
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
        statWriterService.send(Stat.TEST_VOICEMAIL_EMAIL)
    }

    void sendNewVoicemailSms(Voicemail voicemail, def toAddress = null, Stat stat = Stat.NEW_VOICEMAIL_SMS) {
        def address = toAddress
        
        if(!address) {
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
    
            address = preferences.smsNotificationAddress
            if(!address) {
                log.debug "VoicemailPreferences.smsNotificationAddress is not set"
                return
            }
        }

        def voicemailFrom = voicemail.from()
        def transcription = voicemail.audio.transcription
        def voicemailNumber = directMailboxNumber(voicemail.owner.organization)

        backgroundService.execute("New voicemail SMS to [${voicemail.owner.username}] at [${address}] for voicemail id [${voicemail.id}]", {
            def max = 160
            def message = "New voicemail from ${voicemailFrom}. ${transcription}"
            def retrieve = voicemailNumber ? " Retrieve it at ${voicemailNumber.pattern}." : ''
            if(message.size() + retrieve.size() <= max) {
                message += retrieve
            } else if(message.size() > max) {
                message = message[0..(max - 1)]
            }

            sendMail {
                to address
                subject "New voicemail from ${voicemailFrom}"
                body message
            }
        })
        if(stat) {
            statWriterService.send(stat)
        }
        historyService.sentNewVoicemailSms(voicemail)
    }
    
    void sendNewFaxSms(Fax fax) {
            def preferences = VoicemailPreferences.findByUser(fax.owner)
            if(!preferences) {
                log.warn "No VoicemailPreferences configured for user [${fax.owner}]"
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
    
            def faxFrom = fax.from()
    
            backgroundService.execute("New fax SMS to [${fax.owner.username}] at [${preferences.smsNotificationAddress}] for fax id [${fax.id}]", {
                def max = 160
                def message = "New fax from ${faxFrom}."
                if(message.size() > max) {
                    message = message[0..(max - 1)]
                }
    
                sendMail {
                    to preferences.smsNotificationAddress
                    subject "New fax from ${faxFrom}"
                    body message
                }
            })
            
            statWriterService.send(Stat.NEW_FAX_SMS)
            historyService.sentNewFaxSms(fax)
    }

    void sendNewVoicemailTestSms(String address) {
        backgroundService.execute("New voicemail test SMS to addres [${address}]", {
            sendMail {
                to address
                subject 'Listen Notification Test Message'
                body 'You have correctly configured your settings to receive SMS notifications at this address'
            }
        })
        statWriterService.send(Stat.TEST_VOICEMAIL_SMS)
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

    private def directMailboxNumber(def organization) {
        // TODO hard-coded destination application
        def routes = NumberRoute.findAllByDestinationAndOrganization('Mailbox', organization).findAll { !it.pattern.contains('*') }
        if(routes.size() == 0) {
            return null
        }
        return routes.sort { it.pattern.size() }.reverse()[0]
    }
}
