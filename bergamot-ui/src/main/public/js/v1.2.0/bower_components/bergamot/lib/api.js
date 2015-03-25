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
        
        this.registerForUpdates = function(/*Array*/ checkIds, /*Function*/ onResponse, /*Function*/ onError)
        {
            // a default onResponse handler
            if (! onResponse) { onResponse = function(message) {} }
            // if onError is not provided use onResponse
            if (! onError) { onError = onResponse; }
            // send the ping
            this.trigger("bergamot-api-register-for-updates", { "check_ids": checkIds, "onResponse": onResponse.bind(this), "onError": onError.bind(this) });
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