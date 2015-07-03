define(['flight/lib/component', 'bergamot/lib/api', 'bergamot/lib/util/logger', 'bergamot/lib/ui/check-state'], function (defineComponent, bergamot_api, logger, bergamot_ui_check_state) 
{
    return defineComponent(function()
    {
        this.after('initialize', function()
        {
            // handle the on connected event
            this.on(document, "bergamot-api-connected", this.onConnected);
            // handle server updates
            this.on(document, "bergamot-api-update", this.onUpdate);
            // force setup
            this.onConnected();
        });
            
        this.onUpdate = function(/*Event*/ ev, /*Object*/ data)
        {
            if (data.update.alert)
            {
                this.log_debug("Got alert update: " + JSON.stringify(data));
                // update the dashboard display
                var alert = data.update.alert;
                if (alert.acknowledged || alert.recovered)
                {
                    // remove the alert from the dash
                    var $rmNode = $('div[data-check-id=' + alert.check.id + ']');
                    $rmNode.fadeTo(800, 0, function() {
                        $rmNode.remove();
                    });
                }
                else
                {
                    // render the alert and append it to the dashboard
                    $.get(this.attr.api_path + '/alert/id/' + alert.id + '/dashboard/render', function(alertHtml) {
                        // append
                        var alertElement = $.parseHTML(alertHtml);
                        $('#alerts').append(alertElement);
                        $(alertElement).find('div[data-check-id]').each(function(i, e) {
                            $(e).css('opacity', 0);
                            $(e).fadeTo(1200, 1);
                            bergamot_ui_check_state.attachTo($(e));
                        });
                    });
                }
            }
        };
        
        this.onConnected = function(/*Event*/ ev)
        {
            this.log_debug("Registering for alert updates");
            this.registerForUpdates("alert", [ /* empty means all alerts*/ ], function(message)
            {
                this.log_debug("Registered for alert updates: " + message.stat);
            }, 
            function(message)
            {
                this.log_debug("Failed to register for alert updates: " + message.stat + " " + message.message);
            });
        };
	
    }, bergamot_api, logger);
});