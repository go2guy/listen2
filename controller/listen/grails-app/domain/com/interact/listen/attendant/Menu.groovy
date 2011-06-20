package com.interact.listen.attendant


import com.interact.listen.Organization
import com.interact.listen.attendant.action.Action

class Menu {
    Action defaultAction
    boolean isEntry
    String name
    String optionsPrompt // path to options prompt wav file to play at beginning of menu
    Action timeoutAction

    static belongsTo = [menuGroup: MenuGroup]
    static hasMany = [keypressActions: Action]

    static constraints = {
        name blank: false, maxSize: 50, unique: 'menuGroup'
        optionsPrompt: blank: false
    }

    def toIvrCommand(String promptDirectory, String promptBefore) {
        def args = [
            id: id,
            keyPresses: keypressActions.inject([]) { list, action ->
                list.add(action.keysPressed)
                return list
            },
            audioFile: optionsPrompt && !optionsPrompt.trim().equals('') ? promptDirectory + '/' + optionsPrompt : ''
        ]

        return [
            action: 'PROMPT',
            promptBefore: !promptBefore || promptBefore.trim().equals('') ? '' : promptDirectory + '/' + promptBefore,
            args: args
        ]
    }
}