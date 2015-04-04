define(['flight/lib/component', 'bergamot/lib/api', 'bergamot/lib/util/logger'], function (defineComponent, bergamot_api, logger) 
{
    return defineComponent(function()
    {
        this.after('initialize', function()
        {
            // if we have support for desktop notifications, ask to grant permission
            if (("Notification" in window) && (!(Notification.permission === "granted" || Notification.permission === 'denied')))
            {
                // request permission to raise notification is not granted or denied
                Notification.requestPermission();
            }
            // handle the on connected event
            this.on(document, "bergamot-api-connected", this.onConnected);
            // handle server notifications
            this.on(document, "bergamot-api-notification", this.onNotification);
        });
            
        this.onNotification = function(/*Event*/ ev, /*Object*/ data)
        {
            // this.log_debug("Got server notification: " + JSON.stringify(data.notification));
            // raise desktop notification
            if (("Notification" in window) && Notification.permission === "granted")
            {
                var title = this.getNotificationTitle(data.notification);
                var url   = this.getCheckUrl(data.notification.check);
                var id    = data.notification.alert_id;
                var text  = data.notification.check.state.status + ": " + data.notification.check.state.output;
                // raise
                // this.log_debug("Raising desktop notification: " + title + " ==> " + url);
                var notification = new Notification(title, { tag:  id, body: text, icon: "/images/icons/64/alert.png" });
                $(notification).click(function(ev) {
                    window.location = url;
                });
            }
        };
        
        this.getNotificationTitle = function(/*Notification*/ notification)
        {
            var title = "Notification for ";
            // notification type
            if (notification.type == 'bergamot.send_alert')
            {
                title = "Alert for ";
            }
            else if (notification.type == 'bergamot.send_recovery')
            {
                title = "Recovery for ";
            }
            // check name
            title += notification.check.summary;
            // on host ?
            if (notification.check.host)
            {
                title += " on " + notification.check.host.summary;
            }
            // on cluster ?
            if (notification.check.cluster)
            {
                title += " on " + notification.check.cluster.summary;
            }
            return title
        };
        
        this.getCheckUrl = function(/*Check*/ check)
        {
            return "/" + check.type.replace('bergamot.', '') + "/id/" + check.id;
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