define(['flight/lib/component', 'bergamot/lib/api', 'bergamot/lib/util/logger', 'bergamot/lib/ui/check-state'], function (defineComponent, bergamot_api, logger, bergamot_ui_check_state) 
{
    return defineComponent(function()
    {
        this.after('initialize', function()
        {
            // handle the on connected event
            this.on(document, "bergamot-api-connected", this.onConnected);
            // handle server notifications
            this.on(document, "bergamot-api-notification", this.onNotification);
        });
            
        this.onNotification = function(/*Event*/ ev, /*Object*/ data)
        {
            // this.log_debug("Got server notification: " + JSON.stringify(data.notification));
            // update the dashboard display
            var notification = data.notification;
            if (notification.type == 'bergamot.send_alert')
            {
                // render the alert and append it to the dashboard
            	$.get(this.attr.api_path + '/alert/id/' + notification.alert_id + '/dashboard/render', function(alertHtml) {
            		// append
            		var alertElement = $.parseHTML(alertHtml);
            		$('#alerts').append(alertElement);
            		$(alertElement).find('div[data-check-id]').each(function(i, e) {
            			$(e).css('opacity', 0);
            			$(e).fadeTo(1200, 1);
            			bergamot_ui_check_state.attachTo(e);
            		});
            	});
            }
            else if (notification.type == 'bergamot.send_recovery')
            {
                // remove the alert from the dash
            	var $rmNode = $('div[data-check-id=' + notification.check.id + ']');
        	    $rmNode.fadeTo(800, 0, function() {
        	    	$rmNode.remove();
        	    });
            }
        };
        
        this.onConnected = function(/*Event*/ ev)
        {
            // this.log_debug("Registering for notifications, site id: " + this.attr.site_id);
            this.registerForNotifications(
                this.attr.site_id, 
                function(message)
                {
                    // this.log_debug("Registered for notifications: " + message.stat);
                }, 
                function(message)
                {
                    this.log_debug("Failed to register for notifications: " + message.stat + " " + message.message);
                }
            );
        };
	
    }, bergamot_api, logger);
});