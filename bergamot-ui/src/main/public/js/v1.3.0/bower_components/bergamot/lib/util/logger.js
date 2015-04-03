define(function (require) 
{
    return function()
    {
	this.log_debug = function(/*String*/ message)
	{
	    this.trigger("bergamot-log", { "level": "debug", "message": message });
	};
    };
});