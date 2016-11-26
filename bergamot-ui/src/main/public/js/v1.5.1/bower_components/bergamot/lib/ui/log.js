define(['flight/lib/component'], function (defineComponent) 
{
    return defineComponent(function()
    {
	this.after('initialize', function() {
	    this.on(document, 'bergamot-log', this.onLog);
	});
	
	this.onLog = function(/*Event*/ ev, /*Object*/ data)
	{
	    this.appendToLog(data.level, data.message);
	};
	
	this.appendToLog = function(/*String*/ level, /*String*/ message)
	{
	    this.$node.append("<li><span class=\"level\">" + level + " </span><span class=\"message\">" + message + "</span></li>");
	};
    });
});