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
			this.readings[reading.reading_id] = { "reading": reading, "mode": "hour" }
			// container for the reading
			var container = this.createReadingContainer(reading.reading_id);
			// append the container to the page
			this.$node.append(container);
			// setup the graph
			$.getJSON(this.getDataURL(reading.reading_id), function(data) {
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
			// default to last 4 hours
			var end = new Date().getTime();
			var start = end - (3600000 * 4);
			var rollup = this.readings[readingId].reading.poll_interval;
			// look at the type
			if ('day' == type)
			{
				start = end - 86400000;
				rollup = 900000; /* 15 minutes */
			}
			else if ('week' == type)
			{
				start = end - (86400000 * 7);
				rollup = 7200000; /* 2 hours */
			}
			else if ('month' == type)
			{
				start = end - (86400000 * 31);
				rollup = 86400000; /* 1 day */
			}
			return '/api/lamplighter/' + graphUrl + '/' + readingId + '/date/' + rollup + '/avg/' + start + '/' + end;
		};
		
		this.redrawChart = function(readingId, type)
		{
			if (!! type) this.readings[readingId].mode = type;
			// get the data and redraw
			$.getJSON(this.getDataURL(readingId), function(data) {
				$("#reading-" + readingId + "-chart").trigger('redraw', data);
			});
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
			var selectorLabel = $.parseHTML('<h6 style="margin-bottom: 5px; padding-left: 4px;">Chart Range</h6>');
			$(opts).append(selectorLabel);
			var selector = $.parseHTML([
				'<select>',
					'<option selected value="hours">Last 4 Hours</option>',
					'<option value="day">Last Day</option>',
					'<option value="week">Last Week</option>',
					'<option value="month">Last Month</option>',
				'</select>'
			].join(''));
			$(opts).append(selector);
			// actions
			$(selector).change((function(ev) {
				ev.preventDefault();
				this.redrawChart(readingId, $(selector).val());
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
						this.redrawChart(readingId);
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