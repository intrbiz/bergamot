define(['flight/lib/component', 'bergamot/lib/api', 'bergamot/lib/util/logger', 'ace/ace'], function (defineComponent, bergamot_api, logger, ace) 
{
    return defineComponent(function()
    {
	
	this.after('initialize', function() {
        /* Setup Tabs */
	    $('#verify').hide();
        $('#source').show();
	    /* Setup the command editor */
		this.editor = ace.edit("command");
    	this.editor.setTheme("ace/theme/github");
    	this.editor.getSession().setMode("ace/mode/xml");
    	/* Tab Actions */
    	$('#show_source').click(function(ev) {
    		ev.preventDefault();
    		$('#verify').hide();
    		$('#source').show();
    	});
    	$('#show_verify').click((function(ev) {
    		ev.preventDefault();
    		$('#source').hide();
    		$('#verify_progress').show();
    		$('#verify_success').hide();
    		$('#verify_failed').hide();
    		$('#verify').show();
    		this.verifyCommand();
    	}).bind(this));
	});
    
    this.verifyCommand = function() {
        /* Verify the command */
        $.getJSON('/command/editor/verify?command=' + escape(this.editor.getValue()), (function(data) {
            if (data.stat == 'OK')
            {
                $('#verify_progress').hide();
                $('#verify_success').show();
                // replace the parameters view
                var view = $.parseHTML(data.parameters_view);
                $('#test_command_parameters').replaceWith(view);
                // attach our skeleton execution to the test form
            }
            else
            {
                $('#verify_failed_errors').empty();
                if (data.report)
                {
                    for (var i = 0; i < data.report.errors.length; i++)
                    {
                        var li = document.createElement('li');
                        $(li).text(data.report.errors[i]);
                        $('#verify_failed_errors').append(li);
                    }
                }
                else if (data.message)
                {
                    var li = document.createElement('li');
                    $(li).text(data.message);
                    $('#verify_failed_errors').append(li);
                }
                $('#verify_progress').hide();
                $('#verify_failed').show();
            }
        }).bind(this));
        /* Test command */
        $('#test_command').submit((function(ev) {
            ev.preventDefault();
            // setup an adhoc result queue
            // submit the check for execution
        }).bind(this));
    };

    }, bergamot_api, logger);
});