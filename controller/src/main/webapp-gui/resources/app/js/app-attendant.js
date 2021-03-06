var interact = interact || {};
var Attendant;
$(document).ready(function() {

    $('#attendant-menu-add-new-action').click(function() {
        Attendant.addAction(Attendant.getTemplateAction(), $('#attendant-menu-actions-container'), false, false);
    });

    $('#attendant-menu-add-new-menu').click(function() {
        Attendant.deselectMenuList();
        Attendant.loadNewMenu();
    });

    $('#attendant-menu-cancel').click(function() {
        Attendant.deselectMenuList();
        $('#attendant-menu-builder-container').hide();
        $('#attendant-menu-builder-container-placeholder').show();
    });

    $('#attendant-menu-save').click(function() {
        Attendant.saveMenu();
    });

    Attendant = function() {
        return {
            prompts: [],
            Application: function() {
                var interval, promptInterval;
                var dynamicTable = new interact.util.DynamicTable({
                    url: interact.listen.url('/ajax/getAttendantMenuList'),
                    tableId: 'attendant-menu-list',
                    templateId: 'attendant-menu-list-row-template',
                    retrieveList: function(data) {
                        return data;
                    },
                    updateRowCallback: function(row, data, animate) {
                        interact.util.setFieldContent(row.find('.attendant-menu-list-cell-name'), data.name, animate);
                        interact.util.setFieldContent(row.find('.attendant-menu-list-cell-edit'), '<button class="icon-edit" onclick="Attendant.loadMenu(' + data.id + ');"></button>', false, true);
                        if(data.name !== 'Top Menu') {
                            interact.util.setFieldContent(row.find('.attendant-menu-list-cell-delete'), '<button class="icon-delete" onclick="Attendant.deleteMenu(' + data.id + ');"></button>', false, true);
                        }
                    }
                });

                this.load = function() {
                    interact.util.trace('Loading Attendant');
                    dynamicTable.pollAndSet(false);
                    interval = setInterval(function() {
                        dynamicTable.pollAndSet(true);
                    }, 1000);
                    
                    Attendant.loadPrompts();
                    promptInterval = setInterval(function() {
                        Attendant.loadPrompts();
                    }, 30000);
                };
            },

            loadNewMenu: function() {
                Attendant.resetMenu();

                Attendant.addAction({action: 'GoToMenu', arguments: {menuId: 1}}, $('#attendant-menu-actions-default-action-container'), true, false);
                Attendant.addAction({action: 'GoToMenu', arguments: {menuId: 1}}, $('#attendant-menu-actions-timeout-action-container'), false, true);

                $('#attendant-menu-builder-container-placeholder').hide();
                $('#attendant-menu-builder-container').show();
            },

            loadMenu: function(id) {
                var start = interact.util.timestamp();
                    $.ajax({
                        url: interact.listen.url('/ajax/getAttendantMenuList'),
                        dataType: 'json',
                        cache: false,
                        success: function(data, textStatus, xhr) {
                            Attendant.resetMenu();

                            $('#attendant-menu-list tbody tr').each(function(index, value) {
                                var row = $(value);
                                if(row.attr('id') == 'attendant-menu-list-row-' + id) {
                                    row.addClass('selected');
                                } else {
                                    row.removeClass('selected');
                                }
                            });
        
                            var menu;
                            for(var i = 0; i < data.length; i++) {
                                if(data[i].id === id) {
                                    menu = data[i]
                                }
                            }
                            if(menu) {
                                interact.util.debug('Populating menu [' + id + ']');
        
                                $('#attendant-menu-id').val(id);
                                $('#attendant-menu-name input').val(menu.name);
                                if(menu.name === 'Top Menu') {
                                    $('#attendant-menu-name input').attr('readonly', 'readonly').addClass('disabled');
                                }
                                $('#attendant-menu-audio-file').val(menu.audioFile);
        
                                for(var i = 0; i < menu.actions.length; i++) {
                                    var action = menu.actions[i];
                                    interact.util.debug('Loading action [' + action.keyPressed + '] -> [' + action.action + ']');
                                    Attendant.addAction(action, $('#attendant-menu-actions-container'), false, false);
                                }

                                Attendant.addAction(menu.defaultAction, $('#attendant-menu-actions-default-action-container'), true, false);
                                Attendant.addAction(menu.timeoutAction, $('#attendant-menu-actions-timeout-action-container'), false, true);
                            }
                            else
                            {
                                interact.util.debug('Menu not found in list with id [' + id + ']');
                            }
                            $('#attendant-menu-builder-container-placeholder').hide();
                            $('#attendant-menu-builder-container').show();
                        },
                        complete: function(xhr, textStatus) {
                            var elapsed = interact.util.timestamp() - start;
                            $('#latency').text(elapsed);
                        }
                    });
            },

            deleteAction: function(button) {
                $(button).parent().parent().remove();
            },

            resetMenu: function() {
                $('#attendant-menu-id').val('');
                $('#attendant-menu-name input').val('New Menu').removeAttr('readonly').removeClass('disabled');

                $('#attendant-menu-actions-default-action-container div').remove();
                $('#attendant-menu-actions-timeout-action-container div').remove();
                $('#attendant-menu-actions-container div').remove();

                Attendant.deselectMenuList();
            },

            addAction: function(action, container, isDefault, isTimeout) {
                var clone = $('#attendant-menu-action-template').clone()
                clone.removeAttr('id');

                container.append(clone);

                if(!isDefault && !isTimeout) {
                    $('.attendant-menu-action-delete', clone).show();
                    $('.attendant-menu-action-delete', clone).html('<button type="button" class="icon-delete" onclick="Attendant.deleteAction(this);"></button>');
                }

                if(isDefault) {
                    $('.attendant-menu-action-defaultLabel', clone).show();
                } else if(isTimeout) {
                    $('.attendant-menu-action-timeoutLabel', clone).show();
                } else {
                    $('.attendant-menu-action-keypressLabel', clone).show();
                    $('.attendant-menu-action-keypressInput', clone).show();
                    $('.attendant-menu-action-keypressInput input', clone).val(action.keyPressed);
                }

                $('.attendant-menu-action-actionSelect', clone).show();
                $('.attendant-menu-action-actionSelect select', clone).change(function(e) {
                    Attendant.toggleActionFields($(e.target).parent().parent());
                });
                $('.attendant-menu-action-actionSelect select', clone).val(action.action);
                if(isTimeout) {
                    $(".attendant-menu-action-actionSelect select option[value='DialPressedNumber']", clone).remove();
                }

                Attendant.toggleActionFields(clone, action);
                clone.show();
            },

            getTemplateAction: function() {
                return {
                    action: 'DialPressedNumber',
                    arguments: {}
                };
            },

            toggleCustomApplicationInput: function(div) {
                var select = $('.attendant-menu-action-launchApplicationSelect select', div);
                var input = $('.attendant-menu-action-launchApplicationCustomInput', div);

                if(select.val() == 'custom') {
                    input.show();
                } else {
                    input.hide();
                }
            },

            toggleActionFields: function(div, action) {
                var sel = $('.attendant-menu-action-actionSelect select', div);
                var a = {arguments: {}};
                if(interact.util.isDefined(action)) {
                    a = action;
                }

                switch(sel.val()) {
                    case 'DialNumber':
                        $('.attendant-menu-action-dialNumberInput', div).show();
                        $('.attendant-menu-action-dialNumberInput input', div).val(a.arguments.number);
                        
                        $('.attendant-menu-action-menuSelect', div).hide();
                        $('.attendant-menu-action-launchApplicationSelect', div).hide();
                        $('.attendant-menu-action-launchApplicationCustomInput', div).hide();
                        break;

                    case 'GoToMenu':
                        $('.attendant-menu-action-menuSelect', div).show();
                        var select = $('.attendant-menu-action-menuSelect select', div);
                        $('option', select).remove();

                        $('#attendant-menu-list tbody tr').each(function(index, value) {
                            var row = $(value);
                            var id = row.attr('id').substring(row.attr('id').lastIndexOf('-') + 1);
                            var name = $('.attendant-menu-list-cell-name', row).text();
                            select.append($('<option></option>').attr('value', id).text(name));
                        });

                        $('.attendant-menu-action-menuSelect select', div).val(a.arguments.menuId);
                        $('.attendant-menu-action-dialNumberInput', div).hide();
                        $('.attendant-menu-action-launchApplicationSelect', div).hide();
                        $('.attendant-menu-action-launchApplicationCustomInput', div).hide();
                        break;

                    case 'LaunchApplication':
                        $('.attendant-menu-action-launchApplicationSelect', div).show();
                        var appName = a.arguments.applicationName;
                        if(appName != 'conferencing' && appName != 'mailbox' && appName != 'voicemail' && appName != 'directVoicemail') {
                            appName = 'custom';
                        }
                        $('.attendant-menu-action-launchApplicationSelect select', div).val(appName);
                        if(appName == 'custom') {
                            $('.attendant-menu-action-launchApplicationCustomInput input', div).val(a.arguments.applicationName);
                        }
                        $('.attendant-menu-action-launchApplicationSelect', div).change(function(e) {
                            Attendant.toggleCustomApplicationInput($(e.target).parent().parent());
                        });
                        Attendant.toggleCustomApplicationInput(div);

                        $('.attendant-menu-action-menuSelect', div).hide();
                        $('.attendant-menu-action-dialNumberInput', div).hide();
                        break;

                    case 'DialPressedNumber':
                        $('.attendant-menu-action-menuSelect', div).hide();
                        $('.attendant-menu-action-dialNumberInput', div).hide();
                        $('.attendant-menu-action-launchApplicationSelect', div).hide();
                        $('.attendant-menu-action-launchApplicationCustomInput', div).hide();
                        break;

                    default:
                        break;
                }
            },

            deselectMenuList: function() {
                $('#attendant-menu-list tbody tr').removeClass('selected');
            },

            buildMenuObject: function() {
                var menu = {
                    id: $('#attendant-menu-id').val(),
                    name: $('#attendant-menu-name input').val(),
                    audioFile: $('#attendant-menu-audio-file').val(),
                    defaultAction: Attendant.buildAction($('#attendant-menu-actions-default-action-container div:first-child')),
                    timeoutAction: Attendant.buildAction($('#attendant-menu-actions-timeout-action-container div:first-child')),
                    actions: []
                };
                $('#attendant-menu-actions-container div.attendant-menu-action').each(function(index, value) {
                    menu.actions.push(Attendant.buildAction($(value)));
                });
                return menu;
            },

            buildAction: function(container) {
                var actionName = $('.attendant-menu-action-actionSelect select', container).val();
                var action = {
                    keyPressed: $('.attendant-menu-action-keypressInput input', container).val(),
                    action: actionName,
                    arguments: {}
                };
                switch(actionName) {
                    case 'GoToMenu':
                        action.arguments.menuId = $('.attendant-menu-action-menuSelect select', container).val();
                        break;

                    case 'DialNumber':
                        action.arguments.number = $('.attendant-menu-action-dialNumberInput input', container).val();
                        break;

                    case 'LaunchApplication':
                        var select = $('.attendant-menu-action-launchApplicationSelect select', container);
                        var custom = $('.attendant-menu-action-launchApplicationCustomInput input', container);
                        if(select.val() == 'custom') {
                            action.arguments.applicationName = custom.val();
                        } else {
                            action.arguments.applicationName = select.val();
                        }
                        break;
                }
                return action;
            },

            saveMenu: function() {
                var menu = Attendant.buildMenuObject();
                Server.post({
                    url: interact.listen.url('/ajax/saveAttendantMenu'),
                    properties: {
                        menu: JSON.stringify(menu)
                    },
                    successCallback: function(data, textStatus, xhr) {
                        interact.listen.notifySuccess('Menu saved');
                        Attendant.deselectMenuList();
                        $('#attendant-menu-builder-container').hide();
                        $('#attendant-menu-builder-container-placeholder').show();
                    },
                    errorCallback: function(message) {
                        interact.listen.notifyError(message);
                    }
                });
            },

            deleteMenu: function(id) {
                if($('#attendant-menu-id').val() == id) {
                    Attendant.resetMenu();
                }
                Server.post({
                    url: interact.listen.url('/ajax/deleteAttendantMenu'),
                    properties: {
                        id: id
                    },
                    successCallback: function() {
                        interact.listen.notifySuccess('Menu deleted');
                    }
                });
            },

            loadPrompts: function() {
                interact.util.trace('Attendant.loadPrompts');
                $.ajax({
                    url: interact.listen.url('/ajax/getMenuPrompts'),
                    dataType: 'json',
                    cache: false,
                    success: function(data, textStatus, xhr) {
                        interact.util.debug('Loading ' + data.length + ' prompts');
                        Attendant.prompts = data;
                        var select = $('#attendant-menu-audio-file');
                        var currentSelection = select.val();
                        $('option', select).remove();
                        for(var i = 0; i < data.length; i++) {
                            select.append($('<option></option>').attr('value', data[i].file).text(interact.util.isDefined(data[i].description) ? data[i].description : data[i].file));
                        }
                        select.val(currentSelection);
                    }
                });
            }
        }
    }();

    new Attendant.Application().load();
});