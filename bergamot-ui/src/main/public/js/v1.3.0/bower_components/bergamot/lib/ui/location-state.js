define(['flight/lib/component', 'bergamot/lib/api', 'bergamot/lib/util/logger'], function (defineComponent, bergamot_api, logger) 
{
    return defineComponent(function()
    {
	
	this.after('initialize', function() {
	    // get the group id
	    if (! this.attr.location_id)
	    {
	    	this.attr.location_id = this.$node.attr("data-location-id");
	    }
	    // handle the on connected event
	    this.on(document, "bergamot-api-connected", this.onConnected);
	    // handle server notifications
	    this.on(document, "bergamot-api-update", this.onUpdate);
	});
	
	this.updateLocation = function(/*Object*/ location)
	{
	    // this.log_debug("Updating location state, to: " + location.state.ok + " " + location.state.status);
	    this.$node.find("h3 span.dash_img").attr("class", "dash_img status_" + location.state.status.toLowerCase());
	    this.$node.find("h3 span.dash_img").attr("title", "The location is " + location.state.status.toLowerCase());
	    this.$node.find("p.field-status span.value").text(location.state.status.toUpperCase().substring(0,1) + location.state.status.toLowerCase().substring(1));
	    // fields
	    this.$node.find("span[data-bind=ok_count]").text(location.state.ok_count);
	    this.$node.find("span[data-bind=warning_count]").text(location.state.warning_count);
	    this.$node.find("span[data-bind=critical_count]").text(location.state.critical_count);
	    // animate the update
	    var $fadeNode = this.$node;
	    $fadeNode.fadeTo(800, 0.2, function() { 
	    	$fadeNode.fadeTo(600, 0.8, function() {
	    		$fadeNode.fadeTo(600, 0.2, function() { 
	    	    	$fadeNode.fadeTo(800, 1); 
	    	    });
	    	}); 
	    });
	};
	    
	this.onUpdate = function(/*Event*/ ev, /*Object*/ data)
	{
	    // this.log_debug("Got server notification: " + data.update);
	    if (data.update.location && data.update.location.id == this.attr.location_id)
	    {
	    	this.updateLocation(data.update.location);
	    }
	};
	
	this.onConnected = function(/*Event*/ ev)
	{
	    // this.log_debug("Registering for updates, location id: " + this.attr.location_id);
	    this.registerForUpdates([ this.attr.location_id ], function(message)
	    {
	    	// this.log_debug("Registered for updates: " + message.stat);
	    }, 
	    function(message)
	    {
	    	this.log_debug("Failed to register for updates: " + message.stat + " " + message.message);
	    });
	};
	
    }, bergamot_api, logger);
});