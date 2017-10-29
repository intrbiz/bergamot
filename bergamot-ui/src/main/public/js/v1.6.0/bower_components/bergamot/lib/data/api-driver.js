define(['flight/lib/component', 'bergamot/lib/util/logger'], function (defineComponent, logger) 
{
    return defineComponent(function()
    {
	/* Metadata about the API types */
	this.api_types = {
	    "bergamot.api.util.ping": { request: true },
	    "bergamot.api.util.pong": { response: true },
	    "bergamot.api.error": { response: true },
	    "bergamot.api.register_for_updates": { request: true },
	    "bergamot.api.registered_for_updates": { response: true },
	    "bergamot.api.event.update": { event: true, raise_event: "bergamot-api-update" },
        "bergamot.api.register_for_notifications": { request: true },
        "bergamot.api.registered_for_notifications": { response: true },
        "bergamot.api.event.notification": { event: true, raise_event: "bergamot-api-notification" },
        "bergamot.api.register_for_adhoc_results": { request: true },
        "bergamot.api.registered_for_adhoc_results": { response: true },
        "bergamot.api.event.adhoc_result": { event: true, raise_event: "bergamot-api-adhoc-result"},
        "bergamot.api.execute_adhoc_check": { request: true },
        "bergamot.api.executed_adhoc_check": { response: true },
	};
	
	this.defaultAttrs({
	    url: "ws://127.0.0.1:8081/websocket",
	    auth: { user_name: "", token: "" }
	});

	this.after('initialize', function()
	{
	    // a map of requests that are pending
	    this.state = "disconnected";
	    this.clientId = (new Date()).getTime() + "-" + Math.floor((Math.random() * 1000000000) + 1);
	    this.requestIdCounter = 0;
	    this.sendQueue = [];
	    this.pendingRequests = { };
	    // setup our event handlers
	    this.on('bergamot-api-send', this.onAPISend);
	    this.on('bergamot-api-ping', this.onPing);
	    this.on('bergamot-api-register-for-updates', this.onRegisterForUpdates);
        this.on('bergamot-api-register-for-notifications', this.onRegisterForNotifications);
        this.on('bergamot-api-register-for-adhoc-results', this.onRegisterForAdhocResults);
        this.on('bergamot-api-execute-adhoc-check', this.onExecuteAdhocCheck);
	    // setup internal on connected handler
	    this.on('bergamot-api-connected', this.pingOnConnected);
	    // connect the websocket
	    $(document).ready((function() {
	    	this.connectWebSocket();
	    }).bind(this));
	    // scheduled task, every 5 seconds
	    var comp = this;
	    setInterval(function() { comp.doScheduledTasks() }, 5000);
	});
	
	/* Scheduled tasks */
	
	this.doScheduledTasks = function()
	{
	    // check if we need to reconnect
	    if (this.state == "disconnected")
	    {
		this.connectWebSocket();
	    }
	    this.flushQueuedRequests();
	};
	
	/* Send Queue */
	
	this.flushQueuedRequests = function()
	{
	    // check if there are any queued messages
	    if (this.state == "connected")
	    {
		for (var i = 0; i < this.sendQueue.length; i++)
		{
		    this.webSocket.send(JSON.stringify(this.sendQueue[i]));
		}
		this.sendQueue = [];
	    }
	};
	
	/* WebSocket lifecycle */
	
	this.pingOnConnected = function(/*Event*/ ev)
	{
	    this.trigger("bergamot-api-ping", {
		onResponse: (function (message) { this.log_debug("Got pong from server :D"); }).bind(this),
		onError:    (function (message) { this.log_debug("Got error from server, expected pong!"); }).bind(this),
	    });
	};
	
	this.connectWebSocket = function()
	{
	    this.state = "connecting";
	    this.webSocket = new WebSocket(this.attr.url);
	    // deleate the events
	    var comp = this;
	    this.webSocket.onopen    = function(ev) { comp.onWebSocketOpen(ev);    };
	    this.webSocket.onclose   = function(ev) { comp.onWebSocketClose(ev);   };
	    this.webSocket.onerror   = function(ev) { comp.onWebSocketError(ev);   };
	    this.webSocket.onmessage = function(ev) { comp.onWebSocketMessage(ev); };
	};
    
	this.onWebSocketOpen = function()
	{
	    this.log_debug("WebSocket opened");
	    this.state = "connected";
	    this.flushQueuedRequests();
	    // broadcast an event for anything that might need to know about a reconnect
	    this.trigger('bergamot-api-connected');
	};
	
	this.onWebSocketClose = function()
	{
	    this.log_debug("WebSocket closed");
	    this.pendingRequests = {};
	    this.state = "disconnected";
	    this.webSocket = null;
	    // broadcast
	    this.trigger('bergamot-api-disconnected');
	};
	
	this.onWebSocketError = function(ev)
	{
	    this.log_debug("WebSocket error: " + JSON.stringify(ev));
	    this.state = "error";
	};
	
	this.onWebSocketMessage = function(ev)
	{
	    this.log_debug("Got message: " + ev.data);
	    // parse the message
	    var message = JSON.parse(ev.data);
	    // lookup the metadata
	    var messageClass = this.api_types[message.type];
	    if (! messageClass)
	    {
		this.log_debug("Got sent unknown message: " + message.type + ", dropping!");
	    }
	    else
	    {
		if (messageClass.response)
		{
		    // a response
		    // lookup the callbacks
		    var pendingRequest = this.pendingRequests[message.in_response_to];
		    if (!! pendingRequest)
		    {
			// delete the pending request
			delete this.pendingRequests[message.in_response_to];
			// log info
			this.log_debug("Got response in " + ( (new Date()).getTime() - pendingRequest.sent_at ) + "ms");
			// invoke the callbacks
			if (message.type == "bergamot.api.error")
			{
			    if (!! pendingRequest.onError) { pendingRequest.onError(message) };
			}
			else
			{
			    if (!! pendingRequest.onResponse) { pendingRequest.onResponse(message) };
			}
		    }
		}
		else if (messageClass.event && messageClass.raise_event)
		{
		    // an event
		    // raise an event
		    this.log_debug("Raising event " + messageClass.raise_event);
		    this.trigger(messageClass.raise_event, message);
		}
		else
		{
		    // WTF
		    this.log_debug("The message " + message.type + " is not a valid response or event, dropping!");
		}   
	    }
	};
	
	/* Low level handlers */
	
	this.addAuthToRequest = function(/*Object*/ request)
	{
	    request.auth = {
		type: "bergamot.api.auth",
		client: "bergamot-web-client",
		user_name: this.attr.auth.user_name,
		token: this.attr.auth.token,
	    };
	};
	
	this.newRequestId = function()
	{
	    return "web-" + this.clientId + "-" + (this.requestIdCounter++); 
	};
	
	this.doAPISend = function(/*Object*/ request, /*Function*/ onResponse, /*Function*/ onError)
	{
	    // validate the type
	    var requestClass = this.api_types[request.type];
	    if ((! requestClass) || (! requestClass.request))
	    {
		// not a valid request
		this.log_debug("Cannot send a message of type" + request.type + " it is not a valid request, dropping!");
		return;
	    }
	    // add various info to the request
	    // this.addAuthToRequest(request);
	    // set id and add to pending requests
	    request.request_id = this.newRequestId();
	    this.pendingRequests[request.request_id] = {
		request: request, 
		sent_at: (new Date()).getTime(),
		"onResponse": onResponse, 
		"onError": onError
	    };
	    // log
	    this.log_debug("Sending message: " + JSON.stringify(request));
	    // send the message
	    if (this.state == "connected")
	    {
		this.webSocket.send(JSON.stringify(request));
	    }
	    else
	    {
		console.log("Queueing message");
		this.sendQueue.push(request);
	    }
	};
	
	this.onAPISend = function(/*Event*/ ev, /*Object*/ data)
	{
	    this.doAPISend(data.request, data.onResponse, data.onError);
	};
	
	/* API methods */
	
	this.onPing = function(/*Event*/ ev, /*Object*/ data)
	{
	    this.doAPISend({ type: "bergamot.api.util.ping" }, data.onResponse, data.onError);
	};
	
	this.onRegisterForUpdates = function(/*Event*/ ev, /*Object*/ data)
	{
	    this.doAPISend({
			type: "bergamot.api.register_for_updates", 
			update_type: data.update_type,
			ids: data.ids
	    }, data.onResponse, data.onError);
	};
    
    this.onRegisterForNotifications = function(/*Event*/ ev, /*Object*/ data)
    {
        this.doAPISend({
            type: "bergamot.api.register_for_notifications", 
            site_id: data.site_id
        }, data.onResponse, data.onError);
    };
    
    this.onRegisterForAdhocResults = function(/*Event*/ ev, /*Object*/ data)
    {
        this.doAPISend({
            type: "bergamot.api.register_for_adhoc_results"
        }, data.onResponse, data.onError);
    };
    
    this.onExecuteAdhocCheck = function(/*Event*/ ev, /*Object*/ data)
    {
        this.doAPISend({
            type: "bergamot.api.execute_adhoc_check",
            check: data.check,
            worker_pool: data.worker_pool,
            engine: data.engine,
            agent_id: data.agent_id,
            ttl: data.ttl
        }, data.onResponse, data.onError);
    };
	
    }, logger);
});