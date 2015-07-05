define(['flight/lib/component', 'lamplighter/lib/chart/line', 'bergamot/lib/api', 'bergamot/lib/util/logger'], function (defineComponent, line_chart, bergamot_api, logger) 
{
    return defineComponent(function()
    {
		this.reading_types = {
			"double_gauge_reading": { graph_url: 'graph/reading/gauge/double' },
			"float_gauge_reading": { graph_url: 'graph/reading/gauge/float' },
			"long_gauge_reading": { graph_url: 'graph/reading/gauge/long' },
			"int_gauge_reading": { graph_url: 'graph/reading/gauge/int' },
		};
	
		this.after('initialize', function() {
			// get the check id
			if (! this.attr.check_id)
			{
				this.attr.check_id = this.$node.attr("data-check-id");
			}
			this.readings = {};
			// handle the on connected event
			this.on(document, "bergamot-api-connected", this.onConnected);
			// handle server notifications
			this.on(document, "bergamot-api-update", this.onUpdate);
			// setup the readings for this check
			this.setupReadings();
		});
	
		this.setupReadings = function()
		{
			// get the list of readings
			$.getJSON('/api/lamplighter/check/id/' + this.attr.check_id + '/readings', (function(data) {
				// we have a reading to display this pannel
				if (data.length > 0) this.$node.css("display", "block");
				// setup each reading
				for (var i = 0; i < data.length; i++)
				{
					this.setupReading(data[i]);
				}
			}).bind(this));
		};
		
		this.setupReading = function(reading)
		{
			// hold onto the reading metadata
			this.readings[reading.reading_id] = { 
                "reading": reading, 
                "mode": "hour", 
                "end": (new Date().getTime() - (reading.poll_interval / 2))
            }
			// container for the reading
			var container = this.createReadingContainer(reading.reading_id);
			// append the container to the page
			this.$node.append(container);
			// setup the graph
			$.getJSON(this.getDataURL(reading.reading_id, true), function(data) {
				line_chart.attachTo("#reading-" + reading.reading_id + "-chart", {
					/* "width": 1140, */
					"height": 300,
					"data": data,
					"axis-x-sample": function(l) { return Math.floor(l / 4); },
					"axis-x-formater": function(x) { var d = new Date(x); return d.toLocaleTimeString() + "\n" + d.toLocaleDateString() }
				});
			});
		};
		
		this.getDataURL = function(readingId)
		{
			// get the current graph mode
			var type = this.readings[readingId].mode;
			// get the graph url
			var graphUrl = this.reading_types[this.readings[readingId].reading.reading_type].graph_url;
			// default to last hour
			var end = this.readings[readingId].end;
			var start = end - (3600000 * 1);
			var rollup = Math.min(this.readings[readingId].reading.poll_interval * 2, Math.max(this.readings[readingId].reading.poll_interval, 300000));
			// look at the type
            if ('2hours' == type)
			{
				start = end - (3600000 * 2);
				rollup = Math.max(this.readings[readingId].reading.poll_interval, 300000); /* 5 minutes or poll interval if larger */
			}
            else if ('4hours' == type)
			{
				start = end - (3600000 * 4);
				rollup = Math.max(this.readings[readingId].reading.poll_interval, 300000); /* 5 minutes or poll interval if larger */
			}
			else if ('6hours' == type)
			{
				start = end - (3600000 * 6);
				rollup = 900000; /* 15 minutes */
			}
			else if ('8hours' == type)
			{
				start = end - (3600000 * 8);
				rollup = 900000; /* 15 minutes */
			}
			else if ('12hours' == type)
			{
				start = end - (3600000 * 12);
				rollup = 900000; /* 15 minutes */
			}
			else if ('day' == type)
			{
				start = end - 86400000;
				rollup = 900000; /* 15 minutes */
			}
			else if ('week' == type)
			{
				start = end - (86400000 * 7);
				rollup = (3600000 * 2); /* 2 hours */
			}
			else if ('month' == type)
			{
				start = end - (86400000 * 31);
				rollup = (3600000 * 12); /* 1 day */
			}
			return '/api/lamplighter/' + graphUrl + '/' + readingId + '/date/' + rollup + '/avg/' + start + '/' + end;
		};
		
		this.redrawChart = function(readingId, opts)
		{
            // new options
			if (opts != null && opts.mode != null) this.readings[readingId].mode = opts.mode;
            if (opts != null && opts.end  != null) this.readings[readingId].end  = opts.end;
			// get the data and redraw
			$.getJSON(this.getDataURL(readingId), function(data) {
				$("#reading-" + readingId + "-chart").trigger('redraw', { "data": data });
			});
		};
        
        this.changeChart = function(readingId, opts)
		{
			// update the chart with new options
            $("#reading-" + readingId + "-chart").trigger('redraw', opts);
		};
		
		this.createReadingContainer = function(readingId)
		{
			// create the container
			var container = document.createElement("div");
			$(container).attr("id", "reading-" + readingId);
			$(container).attr("class", "row");
			// the chart
			var chart = this.createChartContainer(readingId);
			$(container).append(chart);
			// the chart options
			var opts = this.createChartOptions(readingId);
			$(container).append(opts);
			return container;
		};
		
		this.createChartOptions = function(readingId)
		{
			// create the options pannel
			var opts = document.createElement("div");
			$(opts).attr("id", "reading-" + readingId + "-options");
			$(opts).attr("class", "col1");
			// header
			var header = $.parseHTML('<h5 style="margin-bottom: 15px; padding-left: 4px; padding-top: 12px;">Chart Options</h5>');
			$(opts).append(header);
			// chart scale dropdown
			var selectorLabel = $.parseHTML('<h6 style="margin-bottom: 5px; padding-left: 4px;">Time range</h6>');
			$(opts).append(selectorLabel);
			var selector = $.parseHTML([
				'<select>',
                    '<option selected value="hour">Last Hour</option>',
                    '<option value="2hours">Last 2 Hours</option>',
					'<option value="4hours">Last 4 Hours</option>',
                    '<option value="6hours">Last 6 Hours</option>',
                    '<option value="8hours">Last 8 Hours</option>',
                    '<option value="12hours">Last 12 Hours</option>',
					'<option value="day">Last Day</option>',
					'<option value="week">Last Week</option>',
					'<option value="month">Last Month</option>',
				'</select>'
			].join(''));
			$(opts).append(selector);
			// actions
			$(selector).change((function(ev) {
				ev.preventDefault();
				this.redrawChart(readingId, { "mode": $(selector).val() });
			}).bind(this));
			// chart start at zero
			var startAtZeroLabel = $.parseHTML('<h6 style="margin-bottom: 5px; padding-left: 4px;">Start at zero</h6>');
			$(opts).append(startAtZeroLabel);
			var startAtZeroCheck = $.parseHTML([
				'<input type="checkbox" checked="checked"/>'
			].join(''));
			$(opts).append(startAtZeroCheck);
			// actions
			$(startAtZeroCheck).change((function(ev) {
				ev.preventDefault();
				this.changeChart(readingId, { "y-starts-at-zero": $(startAtZeroCheck).prop("checked") });
			}).bind(this));
			return opts;
		};
		
		this.createChartContainer = function(readingId)
		{
			var chart = document.createElement("div");
			$(chart).attr("id", "reading-" + readingId + "-chart");
			$(chart).attr("class", "col8");
			return chart;
		};
		
		this.onUpdate = function(/*Event*/ ev, /*Object*/ data)
		{
			if (data.update.check && data.update.check.id == this.attr.check_id)
			{
				// redraw
				for (var readingId in this.readings)
				{
					if (this.readings.hasOwnProperty(readingId))
					{
						this.redrawChart(readingId, { "end": data.update.check.state.last_check_time });
					}
				}
			}
		};
		
		this.onConnected = function(/*Event*/ ev)
		{
			this.registerForUpdates(
                "check", 
                [ this.attr.check_id ], 
                function(message)
				{
				}, 
				function(message)
				{
					this.log_debug("Failed to register for updates: " + message.stat + " " + message.message);
				}
			);
		};
	
    }, bergamot_api, logger);
});