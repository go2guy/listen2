package com.interact.listen.attendant

import com.interact.listen.attendant.action.*
import com.interact.listen.acd.*
import org.apache.log4j.Logger

class AttendantTagLib {
    static namespace = 'listen'

    def grailsApplication
    def promptFileService
    def springSecurityService

    String storageLocation = "/attendant";
    
    def attendantPromptSelect = { attrs ->
        def value = attrs.value

        def user = springSecurityService.getCurrentUser()

        def prompts = promptFileService.listNames(storageLocation, user.organization.id)
        out << '<select class="' + attrs.class + '">'
        out << '<option>-- No Prompt --</option>'
        prompts.each { prompt ->
            out << '<option' + (value && prompt == value ? ' selected="selected"' : '') + ">${prompt.encodeAsHTML()}</option>"
        }
        out << '<option>-- Upload New Prompt --</option>'
        out << '</select>'
    }

    def rawAttendantPromptSelect = { attrs ->
        def user = springSecurityService.getCurrentUser()
        def prompts = promptFileService.listNames(storageLocation, user.organization.id)
        attrs.from = prompts
        out << g.select(attrs)
    }

    def actionSelect = { attrs ->
        def action = attrs.action
        out << '<select class="action-select">'
        out << '<option' + (action?.instanceOf(DialNumberAction) ? ' selected="selected"' : '') + '> ' + message(code: 'attendant.menugroup.dialANumber') + '</option>'
        out << '<option' + (action?.instanceOf(DialPressedNumberAction) ? ' selected="selected"' : '') + '> ' + message(code: 'attendant.menugroup.dialWhatTheyPressed') + '</option>'
        out << '<option' + (action?.instanceOf(EndCallAction) ? ' selected="selected"' : '') + '> ' + message(code: 'attendant.menugroup.endTheCall') + '</option>'
        out << '<option' + (action?.instanceOf(GoToMenuAction) ? ' selected="selected"' : '') + '> ' + message(code: 'attendant.menugroup.goToAMenu') + '</option>'
        out << '<option' + (action?.instanceOf(RouteToAnACDAction) ? ' selected="selected"' : '') + '> ' + message(code: 'attendant.menugroup.routeToAnACD') + '</option>'
        out << '<option' + (action?.instanceOf(LaunchApplicationAction) ? ' selected="selected"' : '') + '> ' + message(code: 'attendant.menugroup.launchAnApplication') + '</option>'
        out << '<option' + (!action || action.instanceOf(ReplayMenuAction) ? ' selected="selected"' : '') + '> ' + message(code: 'attendant.menugroup.replayThisMenu') + '</option>'
        out << '</select>'
    }

    def attendantApplicationSelect = { attrs ->
        def action = attrs.action

        def hide = !action?.instanceOf(LaunchApplicationAction)
        def value = action?.instanceOf(LaunchApplicationAction) && action?.applicationName ? action?.applicationName : ''

        out << listen.applicationSelect(value: value, hide: hide, exclude: 'IP PBX')
    }

    def dialNumberInput = { attrs ->
        def action = attrs.action

        out << '<input'
        out << ' type="text"'
        out << ' class="number-to-dial"'
        if(action?.instanceOf(DialNumberAction)) {
            out << ' value="' + fieldValue(bean: action, field: 'number') + '"'
        } else {
            out << ' style="display: none;"'
        }
        out << ' placeholder="Enter number..."'
        out << '/>'
    }

    def menuSelect = { attrs ->
        def action = attrs.action
        def group = attrs.group

        out << '<select class="menu-select"' + (action?.instanceOf(GoToMenuAction) ? '' : ' style="display: none;"') + '>'
        out << '<option>-- Select A Menu --</option>'
        group?.menusInDisplayOrder()?.each { menu ->
            out << '<option' + (action?.instanceOf(GoToMenuAction) && action?.destinationMenuName == menu.name ? ' selected="selected"' : '') + ">${fieldValue(bean: menu, field: 'name')}</option>"
        }
        out << '<option>Create New Menu...</option>'
        out << '</select>'
    }

    def acdSelect = { attrs ->
        def action = attrs.action
        def skillInput = attrs.skill
        
        def user = springSecurityService.getCurrentUser()
        def skills = Skill.findAllByOrganization(user.organization, [sort: 'skillname', order: 'asc'])
        
        out << '<select class="acd-select"' + (action?.instanceOf(RouteToAnACDAction) ? '' : ' style="display: none;"') + '>'
        out << '<option>-- Select An ACD --</option>'
        skills.each { skill ->
            out << '<option' + (action?.instanceOf(RouteToAnACDAction) && action?.skill && action?.skill.id == skill.id ? ' selected="selected"' : '') + ">${fieldValue(bean: skill, field: 'skillname')}</option>"
        }
        out << '</select>'
    }
    
    def entryMenuSelect = { attrs ->
        def group = attrs.group

        out << '<select class="entry-menu-select">'
        group?.menusInDisplayOrder()?.each { menu ->
            out << '<option' + (menu?.isEntry ? ' selected="selected"' : '') + ">${fieldValue(bean: menu, field: 'name')}</option>"
        }
        out << '</select>'
    }

    def menuGroupSelect = { attrs ->
        def user = springSecurityService.getCurrentUser()

        def menuGroups = MenuGroup.findAllByOrganization(user.organization, [sort: 'name', order: 'asc'])
        attrs.from = menuGroups
        attrs.optionKey = 'id'
        attrs.optionValue = 'name'
        out << g.select(attrs)
    }
}
