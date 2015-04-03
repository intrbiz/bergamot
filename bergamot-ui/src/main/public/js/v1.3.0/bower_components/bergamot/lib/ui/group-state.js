define(['flight/lib/component', 'bergamot/lib/api', 'bergamot/lib/util/logger'], function (defineComponent, bergamot_api, logger) 
{
    return defineComponent(function()
    {
	
	this.after('initialize', function() {
	    // get the group id
	    if (! this.attr.group_id)
	    {
	    	this.attr.group_id = this.$node.attr("data-group-id");
	    }
	    // handle the on connected event
	    this.on(document, "bergamot-api-connected", this.onConnected);
	    // handle server notifications
	    this.on(document, "bergamot-api-update", this.onUpdate);
	});
	
	this.updateGroup = function(/*Object*/ group)
	{
	    this.log_debug("Updating group state, to: " + group.state.ok + " " + group.state.status);
	    this.$node.find("h3 span.dash_img").attr("class", "dash_img status_" + group.state.status.toLowerCase());
	    this.$node.find("h3 span.dash_img").attr("title", "The check is " + group.state.status.toLowerCase());
	    this.$node.find("p.field-status span.value").text(group.state.status.toUpperCase().substring(0,1) + group.state.status.toLowerCase().substring(1));
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
	    this.log_debug("Got server notification: " + data.update);
	    if (data.update.group.id == this.attr.group_id)
	    {
	    	this.updateGroup(data.update.group);
	    }
	};
	
	this.onConnected = function(/*Event*/ ev)
	{
	    this.log_debug("Registering for updates, group id: " + this.attr.group_id);
	    this.registerForUpdates([ this.attr.group_id ], function(message)
	    {
	    	this.log_debug("Registered for updates: " + message.stat);
	    }, 
	    function(message)
	    {
	    	this.log_debug("Failed to register for updates: " + message.stat + " " + message.message);
	    });
	};
	
    }, bergamot_api, logger);
});