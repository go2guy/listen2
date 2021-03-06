package com.interact.listen

class DirectInwardDialNumber extends PhoneNumber {
    String forwardedTo
    Audio greeting
    
    static constraints = {
        // all fields must be nullable since we extend PhoneNumber
        forwardedTo nullable: true, blank: false, maxSize: 50
        greeting nullable: true
    }

}
