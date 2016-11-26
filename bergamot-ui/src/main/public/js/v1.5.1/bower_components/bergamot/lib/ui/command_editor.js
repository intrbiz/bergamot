define(['flight/lib/component', 'bergamot/lib/api', 'bergamot/lib/util/logger', 'ace/ace'], function (defineComponent, bergamot_api, logger, ace) 
{
    return defineComponent(function()
    {
	
	this.after('initialize', function() {
        /* Event handlers */
        this.on(document, 'bergamot-api-adhoc-result', this.onAdhocResult);
        /* Setup tabs */
	    $('#verify').hide();
        $('#source').show();
	    /* Setup the command editor */
		this.editor = ace.edit("command");
    	this.editor.setTheme("ace/theme/github");
    	this.editor.getSession().setMode("ace/mode/xml");
    	/* Tab actions */
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
        /* Test command */
        $('#test_command').submit((function(ev) {
            ev.preventDefault();
            // run the test
            this.testCommand();
        }).bind(this));
	});
    
    this.testCommand = function() {
        // build our check
        var check = this.skeletonCheck;
        $('#test_command').find('input[type=text]').each(function(i, e) {
            check.parameters.push({
                "type": "bergamot.parameter",
                "name": $(e).attr('name').substring(10),
                "value": $(e).val()
            });
        });
        // parameters
        var workerPool = $('#worker_pool').val();
        var agentId = $('#agent_id').val();
        // setup our adhoc results queue
        $('#test_results').text('');
        this.registerForAdhocResults((function(response) {
            this.adhocId = response.adhoc_id;
            // execute the check
            this.executeAdhocCheck(check, { "worker_pool": workerPool, "agent_id": agentId });
        }).bind(this));
    };
    
    this.onAdhocResult = function(event, /*Object*/ data) {
        $('#test_results').text(JSON.stringify(data.result, null, "    "));
    };
    
    this.verifyCommand = function() {
        /* Verify the command */
        $.getJSON('/command/editor/verify?command=' + encodeURIComponent(this.editor.getValue()), (function(data) {
            if (data.stat == 'OK')
            {
                $('#verify_progress').hide();
                $('#verify_success').show();
                // replace the parameters view
                var view = $.parseHTML(data.parameters_view);
                $('#test_command').find('.test_parameter').remove();
                $('#test_parameters').after(view);
                // store our skeleton check
                this.skeletonCheck = data.skeleton_check;
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
    };

    }, bergamot_api, logger);
});