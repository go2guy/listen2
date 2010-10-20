$(document).ready(function() {
    Listen.Voicemail = function() {
        return {
            Application: function() {
                var interval;

                var dynamicTable = new Listen.DynamicTable({
                    url: Listen.url('/ajax/getVoicemailList'),
                    tableId: 'voicemail-table',
                    templateId: 'voicemail-row-template',
                    retrieveList: function(data) {
                        return data.results;
                    },
                    countContainer: 'voicemail-new-count',
                    retrieveCount: function(data) {
                        return data.newCount;
                    },
                    reverse: true,
                    isList: true,
                    paginationId: 'voicemail-pagination',
                    updateRowCallback: function(row, data, animate) {
                        if(data.isNew) {
                            row.removeClass('voicemail-read');
                            row.addClass('voicemail-unread');
                        } else {
                            row.removeClass('voicemail-unread');
                            row.addClass('voicemail-read');
                        }

                        var statusButton = '<button class="icon-' + (data.isNew ? 'unread' : 'read') + '" onclick="' + (data.isNew ? 'Server.markVoicemailReadStatus(' + data.id + ', true);' : 'Server.markVoicemailReadStatus(' + data.id + ', false);return false;') + '" title="' + (data.isNew ? 'Mark as old' : 'Mark as new') + '"></button>';
                        Listen.setFieldContent(row.find('.voicemail-cell-from'), '<div>' + statusButton + '</div><div>' + data.leftBy + '</div>', false, true);
                        Listen.setFieldContent(row.find('.voicemail-cell-received'), data.dateCreated, animate);
                        Listen.setFieldContent(row.find('.voicemail-cell-play'), data.duration, animate);

                        var downloadAction = '<a href="' + Listen.url('/ajax/downloadVoicemail?id=' + data.id) + '">Download</a>';
                        var deleteAction = '<a href="#" onclick="Listen.Voicemail.confirmDeleteVoicemail(' + data.id + ');return false;" title="Delete this voicemail">Delete</a>';
                        Listen.setFieldContent(row.find('.voicemail-cell-actions'), '<div>' + deleteAction + '</div><div>' + downloadAction + '</div>', false, true);

                        var transcriptionField = row.find('.voicemail-cell-transcription');
                        if(data.transcription != null && data.transcription.length > 0) {
                            Listen.setFieldContent(transcriptionField, data.transcription, false, true);
                            transcriptionField.css('display', 'block');
                        } else {
                            transcriptionField.css('display', 'none');
                        }
                    }
                });

                this.load = function() {
                    Listen.trace('Loading voicemail');
                    dynamicTable.pollAndSet(false);
                    interval = setInterval(function() {
                        dynamicTable.pollAndSet(true);
                    }, 1000);
                };

                this.unload = function() {
                    Listen.trace('Unloading voicemail');
                    if(interval) {
                        clearInterval(interval);
                    }
                };
            },

            confirmDeleteVoicemail: function(id) {
                Listen.trace('Listen.Voicemail.confirmDeleteVoicemail');
                if(confirm('Are you sure?')) {
                    Listen.Voicemail.deleteVoicemail(id);
                }
            },

            deleteVoicemail: function(id) {
                Listen.trace('Listen.Voicemail.deleteVoicemail');
                Server.post({
                    url: Listen.url('/ajax/deleteVoicemail'),
                    properties: { id: id }
                });
            }
        }
    }();

    Listen.registerApp(new Listen.Application('voicemail', 'voicemail-application', 'menu-voicemail', new Listen.Voicemail.Application()));
});