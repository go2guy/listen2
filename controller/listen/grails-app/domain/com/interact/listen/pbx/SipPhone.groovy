package com.interact.listen.pbx

import com.interact.listen.ProvisionerTemplate
import com.interact.listen.pbx.Extension
import com.interact.listen.Organization
import org.joda.time.DateTime

class SipPhone {
    Extension extension
    Organization organization
    Boolean registered = false
    String phoneUserId;
    String realName
    String username
    String password
    String passwordConfirm
    String ip
    Integer cseq
    String userAgent
    DateTime dateRegistered = null
    DateTime dateExpires = null
	String provisionerIdentifier = null
	ProvisionerTemplate provisionerTemplate = null
	DateTime provisionerLastUpdated = null

    Integer expires = 0

    static belongsTo = [extension: Extension, organization: Organization]

    static transients = ['passwordConfirm', 'expires']

    static constraints = {
        extension nullable: false
        organization nullable: false
        realName nullable: true, unique: false, maxSize: 50
        username blank: false, maxSize: 50, matches: '^[^:]+$'
        phoneUserId maxSize: 50, unique: true
        password nullable: true, maxsize: 255
        /*passwordConfirm blank: false, maxsize: 255, validator: { val, obj ->
            if(obj.password == val) {
                return true
            } else {
                return ['passwordMismatch']
            }
        }*/
        ip nullable: true, blank: false, unique: false, maxSize: 50
        dateRegistered nullable: true, unique: false
        dateExpires nullable: true, unique: false
        userAgent nullable:true
	    provisionerTemplate nullable:true
	    provisionerIdentifier nullable:true
	    provisionerLastUpdated nullable:true
    }

    def isRegistered() {
        def dateNow = new DateTime()
        if ((registered == true) && (dateExpires > dateNow))
            return true
        else
            return false
    }

    def friendlyName() {
        realName ?: username
    }
}
