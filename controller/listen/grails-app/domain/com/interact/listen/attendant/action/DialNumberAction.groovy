package com.interact.listen.attendant.action

class DialNumberAction extends Action {
    String number

    static constraints = {
        number nullable: true, blank: false, validator: { val -> return (val == null ? 'nullable' : true) }
    }

    def toIvrCommand(String promptDirectory, String promptBefore) {
        def args = [
            number: number
        ]

        return [
            action: 'DIAL_NUMBER',
            promptBefore: !promptBefore || promptBefore.trim().equals('') ? '' : promptDirectory + '/' + promptBefore,
            args: args
        ]
    }
}