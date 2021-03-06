define(['flight/lib/component', 'bergamot/lib/api', 'bergamot/lib/util/logger'], function (defineComponent, bergamot_api, logger) 
{
    return defineComponent(function()
    {
	
	this.after('initialize', function() {
	    // get the check id
	    if (! this.attr.check_id)
	    {
	    	this.attr.check_id = this.$node.attr("data-check-id");
	    }
	    // handle the on connected event
	    this.on(document, "bergamot-api-connected", this.onConnected);
	    // handle server notifications
	    this.on(document, "bergamot-api-update", this.onUpdate);
        // force setup
        this.onConnected();
	});
	
	this.updateCheck = function(/*Object*/ check)
	{
	    // this.log_debug("Updating check state, to: " + check.state.ok + " " + check.state.status);
	    this.$node.find("h3 span.dash_img").attr("class", "dash_img status_" + check.state.status.toLowerCase());
	    this.$node.find("h3 span.dash_img").attr("title", "The check is " + check.state.status.toLowerCase());
	    this.$node.find("p.field-status span.value").text(check.state.status.toUpperCase().substring(0,1) + check.state.status.toLowerCase().substring(1));
	    this.$node.find("p.field-output").text(check.state.output);
	    // last check time
	    this.$node.find("p.field-last-checked span.value").text(this.formatDate(check.state.last_check_time));
	    // attempt
	    // build each element to avoid XSS
	    var attempt_span = document.createElement("span");
	    if (check.current_attempt_threshold) $(attempt_span).text(check.state.attempt + " of " + check.current_attempt_threshold + " ");
	    var attempt_flag_span = document.createElement("span");
	    $(attempt_flag_span).attr("class", "info")
	    $(attempt_flag_span).attr("title", check.state.hard ? "The host is in a steady state" : "The check is changing state")
	    $(attempt_flag_span).text(check.state.hard ? "Steady" : "Changing");
	    this.$node.find("p.field-attempt span.value").html([attempt_span, attempt_flag_span]);
	    // handle suppressed icon
	    if (check.suppressed)
	    {
	    	if (! this.$node.find('span.suppressed').length) {
				var el = document.createElement('span');
				$(el).attr('class', 'suppressed');
				$(el).attr('title', 'This check is suppressed, notifications will not be sent');
				this.$node.prepend(el);
			}
	    }
	    else
	    {
	    	this.$node.find('span.suppressed').remove();
	    }
	    // handle disabled icon
	    if (check.enabled)
	    {
	    	this.$node.find('span.disabled').remove();
	    }
	    else
	    {
	    	if (! this.$node.find('span.disabled').length) {
				var el = document.createElement('span');
				$(el).attr('class', 'disabled');
				$(el).attr('title', 'This check is disabled and will not be actively checked');
				this.$node.prepend(el);
			}
	    }
	    // handle downtime icon
	    if (check.in_downtime)
	    {
	    	if (! this.$node.find('span.downtime').length) {
				var el = document.createElement('span');
				$(el).attr('class', 'downtime');
				$(el).attr('title', 'This check is in downtime, notifications will not be sent');
				this.$node.prepend(el);
			}
	    }
	    else
	    {
	    	this.$node.find('span.downtime').remove();
	    }
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
	
	this.formatDate = function (/*long*/ date)
	{
	    var days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
	    var theDate = new Date(date);
	    return this.formatDigit(theDate.getUTCHours()) + ":" + this.formatDigit(theDate.getUTCMinutes()) + ":" + this.formatDigit(theDate.getUTCSeconds()) + 
	           " on " + 
	           days[theDate.getUTCDay()] + " " + 
		   this.formatDigit(theDate.getUTCDate() + 1) + "/" + this.formatDigit(theDate.getUTCMonth() + 1) + "/" + theDate.getUTCFullYear();
	    
	};
	
	this.formatDigit = function (num)
	{
	    if (num < 10) 
		return "0" + num;
	    return num;
	};
	    
	this.onUpdate = function(/*Event*/ ev, /*Object*/ data)
	{
	    // this.log_debug("Got server notification: " + data.update);
	    if (data.update.check && data.update.check.id == this.attr.check_id)
	    {
	    	this.updateCheck(data.update.check);
	    }
	};
	
	this.onConnected = function(/*Event*/ ev)
	{
	    // this.log_debug("Registering for updates, check id: " + this.attr.check_id);
	    this.registerForUpdates("check", [ this.attr.check_id ], function(message)
	    {
	    	//	this.log_debug("Registered for updates: " + message.stat);
	    }, 
	    function(message)
	    {
	    	this.log_debug("Failed to register for updates: " + message.stat + " " + message.message);
	    });
	};
	
    }, bergamot_api, logger);
});