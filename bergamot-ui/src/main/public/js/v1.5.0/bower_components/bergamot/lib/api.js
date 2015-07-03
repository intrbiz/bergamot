define(function (require) 
{
    return function()
    {
        this.ping = function(/*Function*/ onResponse, /*Function*/ onError)
        {
            // a default onResponse handler
            if (! onResponse) { onResponse = function(message) {} }
            // if onError is not provided use onResponse
            if (! onError) { onError = onResponse; }
            // send the ping
            this.trigger("bergamot-api-ping", { "onResponse": onResponse.bind(this), "onError": onError.bind(this) });
        };
        
        this.registerForUpdates = function(/*String*/ updateType, /*Array*/ ids, /*Function*/ onResponse, /*Function*/ onError)
        {
            // a default onResponse handler
            if (! onResponse) { onResponse = function(message) {} }
            // if onError is not provided use onResponse
            if (! onError) { onError = onResponse; }
            // send the ping
            this.trigger("bergamot-api-register-for-updates", { "update_type": updateType, "ids": ids, "onResponse": onResponse.bind(this), "onError": onError.bind(this) });
        };
        
        this.registerForNotifications = function(/*String*/ siteId, /*Function*/ onResponse, /*Function*/ onError)
        {
            // a default onResponse handler
            if (! onResponse) { onResponse = function(message) {} }
            // if onError is not provided use onResponse
            if (! onError) { onError = onResponse; }
            // send the ping
            this.trigger("bergamot-api-register-for-notifications", { "site_id": siteId, "onResponse": onResponse.bind(this), "onError": onError.bind(this) });
        };
    };
});