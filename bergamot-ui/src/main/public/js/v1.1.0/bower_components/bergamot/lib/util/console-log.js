define(['flight/lib/component'], function (defineComponent) 
{
    return defineComponent(function()
    {
	this.after('initialize', function() {
	    this.on(document, 'bergamot-log', this.onLog);
	});
	
	this.onLog = function(/*Event*/ ev, /*Object*/ data)
	{
	    console.log(data.level + ": " + data.message);
	};
    });
});