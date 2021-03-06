package com.interact.listen.pbx

import com.interact.listen.Audio
import com.interact.listen.PhoneNumber

class Extension extends PhoneNumber {
    String forwardedTo
    Audio greeting
    int extLength

    static hasOne = [sipPhone: SipPhone]
    static transients = ['extLength']

    static constraints = {
        // TODO IP address validation

        // all fields must be nullable since we extend PhoneNumber
        forwardedTo nullable: true, blank: false, maxSize: 50
        greeting nullable: true
    }

    static mapping = {
        sipPhone fetch: 'join'
    }
}
