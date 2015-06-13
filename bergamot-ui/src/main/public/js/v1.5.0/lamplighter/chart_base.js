/*
 * Intrbiz JSC Charting Library V1.0
 * Copyright (C) 2010 Chris Ellis
 *
 */

com.intrbiz.jsc.chart = {};

com.intrbiz.jsc.chart.Chart = com.intrbiz.util.Class('com.intrbiz.jsc.chart.Chart', com.intrbiz.Object,
{
        id: null,
        
	width: null,
	
	height: null,
	
	container: null,
	
	paper: null,
	
	title: null,
	
	legend: null,
	       
	parameters : null,
	
	init : function(/*string*/ id, /*int*/ width, /*int*/ height, /*Object*/ parameters)
	{
                this.id = id;
		this.title = {};
		this.legend = {};
		this.parameters = {};
		// Set the default parameters
		com.intrbiz.util.Extend({
			"background-colour": '#444444',
			"label-colour": '#444444',
			"title-colour": '#EEEEEE',
			"border-colour": '#444444',
			"border-width": 1,
			"legend-width": 200
		}, this.parameters);
		// Merge the custom parameters
		com.intrbiz.util.Extend(parameters, this.parameters);
		// set width and height
		this.width = width;
		this.height = height;
		// call the super
		com.intrbiz.util.superInit(com.intrbiz.Object,this,id);
	},

	load : function()
	{
		// create the paper
                require([ "raphael/raphael" ], (function (Raphael) {
                    this.paper = Raphael(this.id, this.width, this.height)
                    this.draw();
                }).bind(this));
	},
	
	/* Configuration functions */
	
	getWidth : function()
	{
		return this.width;
	},
	
	getHeight : function()
	{
		return this.height;
	},
	
	getLeftPadding : function()
	{
		return 50;
	},
	
	getRightPadding : function()
	{
		return this.parameters["legend-width"];
	},
						     
	getLegendTop: function()
	{
		return this.getTopPadding();
	},
	
	getLegendLeft: function()
	{
		return this.getLeftPadding() + this.getGraphWidth() + 10;
	},
	
	getTopPadding : function()
	{
		return 46;
	},
	
	getTitleBarLeft: function()
	{
		return 10;
	},
						     
	getTitleBarWidth: function()
	{
		return this.width - 20;
	},
					       
	getTitleBarTop: function()
	{
		return 5;
	},
	
	getTitleBarHeight: function()
	{
		return 30;
	},
	
	getBottomPadding : function()
	{
		return 30;
	},
					       
	getGraphWidth: function()
	{
		return this.width - (this.getLeftPadding() + this.getRightPadding());
	},
	
	getGraphHeight: function()
	{
		return this.height - (this.getTopPadding() + this.getBottomPadding() );
	},
	
	/* Scaling functions */
	
	translateY: function(y)
	{
		return this.height - y;
	},
	
	/* Drawing functions */
	
	drawTitle: function()
	{
		// the title bar
		this.title.frame = this.paper.rect( this.getTitleBarLeft(), this.getTitleBarTop(), this.getTitleBarWidth(), this.getTitleBarHeight(), this.getTitleBarHeight()/2).attr( { fill: this.parameters["background-colour"], "stroke-width": 0 } );
		// the title text
		var titleText = this.getTitle();
		if (titleText != null)
		{
			this.title.text = this.paper.text( this.getTitleBarLeft() + (this.getTitleBarWidth() / 2), (this.getTitleBarHeight()/2) + this.getTitleBarTop(), titleText );
			this.title.text.attr( { fill: this.parameters["title-colour"], "font-size": '16px', "font-weight": 'bold'} );
		}
	},
	
	drawLegend: function()
	{
		var linfo = this.getLegendInfo();
		if ( linfo != null )
		{
			this.legend.blocks = [];
			this.legend.titles = [];
			var t;
			for (var i = 0; i < linfo.length; i++)
			{
				this.legend.blocks.push(this.paper.rect( this.getLegendLeft(), this.getLegendTop() + (24 * i), 16, 16).attr( { fill: linfo[i].colour, "stroke-width": this.parameters["border-width"], stroke: this.parameters["border-colour"] } ));
				t = this.paper.text( this.getLegendLeft() + 24, this.getLegendTop() + 8 + (24 * i), linfo[i].title ).attr( { "font-size": '12px' } );
				t.attr( { x: ( t.attrs.x + (t.getBBox().width / 2) ) } );
				this.legend.titles.push(t);
			}
		}
	},
	
	draw: function()
	{
		this.drawTitle();
		this.drawLegend();
		this.drawData();
	},
	
	
	redraw: function()
	{
		this.paper.clear();
		this.draw();
	},
	
	/* functions to be extended */
	
	getLegendInfo: function()
	{
		return null;
	},
	
	getTitle: function()
	{
		return null;
	},
	
	drawData: function()
	{
		// EXTEND
	}
});


com.intrbiz.jsc.chart.Graph = com.intrbiz.util.Class('com.intrbiz.jsc.chart.Graph', com.intrbiz.jsc.chart.Chart,
{
	axis: null,
	
	init : function(/*string*/ id, /*int*/ width, /*int*/ height, /*Object*/ parameters)
	{
		this.axis = {};
		var params = {}
		// Set the default parameters
		com.intrbiz.util.Extend({
			"background-colour": '#444444',
			"grid-colour": '#888888',
			"grid-stroke": 1,
			"label-colour": '#444444',
			"title-colour": '#EEEEEE'
		}, params);
		com.intrbiz.util.Extend(parameters, params);
		// call the super
		com.intrbiz.util.superInit(com.intrbiz.jsc.chart.Chart, this, id, width, height, params);
	},
	
	/* Configuration functions */
	
	getXLabelsMargin: function()
	{
		return 15;
	},
	
	getYLabelsMargin: function()
	{
		return 25;
	},
	
	getStartXAtZero : function()
	{
		return true;
	},
	
	getStartYAtZero : function()
	{
		return true;
	},
	
	/* Scaling functions */
	
	min : function(/*Array<number>*/ data)
	{
		var min = data[0];
		for (var i = 1; i < data.length; i++)
			if (min > data[i]) min = data[i];
		return min;
	},

	max: function(/*Array<number>*/ data)
	{
		var max = data[0];
		for (var i = 1; i < data.length; i++)
			if (max < data[i]) max = data[i];
		return max;
	},
					       
	getXScale: function(/*Array<number>*/ data)
	{
		return data.length == 0 ? this.getGraphWidth() / 2 : this.getGraphWidth() / (data.length - 1) ;
	},
	
	getYScale: function(/*Array<number>*/ data)
	{
		var min = this.getStartYAtZero() ? 0 : this.min(data);
		var max = this.max(data);
		return (max - min) == 0 ? (this.getGraphHeight() - 5) / (max * 2) : (this.getGraphHeight() - 5) / (max - min);
	},
	
	/* Drawing functions */
	
	drawAxis: function(/*number*/ xSpacing, /*number*/ ySpacing, /*Array<Object<position:Number,label:String>>*/ xLabels, /*Array<Object<position:Number,label:String>>*/ yLabels)
	{
		var axisWidth = this.getGraphWidth();
		var axisHeight = this.getGraphHeight();
		// axis background
		this.axis.frame = this.paper.rect(this.getLeftPadding(), this.getTopPadding(), axisWidth, axisHeight);
		this.axis.frame.attr( { fill: this.parameters["background-colour"], "stroke-width": 0 } );
		// draw grid
		var path = [];
		if (xSpacing != null)
		{
			for (var x = 0; x <= axisWidth; x += xSpacing)
			{
				path.push("M", (x + this.getLeftPadding()), this.getTopPadding(), "L", (x + this.getLeftPadding()), this.translateY(this.getBottomPadding()) );
			}
		}
		for (var y = 0; y <= axisHeight; y += ySpacing)
		{
			path.push("M", this.getLeftPadding(), this.translateY(y + this.getBottomPadding()), "L", (axisWidth + this.getLeftPadding()), this.translateY(y + this.getBottomPadding()) );
		}
		this.axis.grid = this.paper.path(path);
		this.axis.grid.attr({ "stroke-width": this.parameters["grid-stroke"], stroke : this.parameters["grid-colour"] });
		// axis line
		this.axis.path = this.paper.path( ["M", this.getLeftPadding(), this.getTopPadding(), "L", this.getLeftPadding(), this.translateY(this.getBottomPadding()), (this.width - this.getRightPadding()), this.translateY(this.getBottomPadding())] );
		this.axis.path.attr({ "stroke-width": this.parameters["grid-stroke"], stroke : this.parameters["grid-colour"] });
		// x labels
		this.axis.xLabels = [];
		if (xLabels != null)
		{
			for (var i = 0 ; i < xLabels.length; i++)
			{
				var txt = this.paper.text( xLabels[i].position + this.getLeftPadding(), this.translateY(this.getBottomPadding() - this.getXLabelsMargin()), xLabels[i].label );
				txt.attr( { "fill": this.parameters["label-colour"], "font-size": '12px' } );
				this.axis.xLabels.push(txt);
			}
		}
		// y labels
		this.axis.yLabels = [];
		if (yLabels != null)
		{
			for (var i = 0 ; i < yLabels.length; i++)
			{
				var txt = this.paper.text( (this.getLeftPadding() - this.getYLabelsMargin()), this.translateY(yLabels[i].position +
				this.getBottomPadding()), yLabels[i].label );
				txt.attr( { "fill": this.parameters["label-colour"], "font-size": '12px' } );
				this.axis.yLabels.push(txt);
			}
		}		
	},
	
	draw: function()
	{
		this.drawTitle();
		this.drawLegend();
		this.drawAxis(this.getXGridSpacing(), this.getYGridSpacing(), this.getXLabels(), this.getYLabels());
		this.drawData();
	},
	
	/* functions to be extended */
	
	getXGridSpacing: function()
	{
		return 50;
	},
	
	getXLabels: function()
	{
		return null;
	},
	
	getYGridSpacing: function()
	{
		return 50;
	},
	
	getYLabels: function()
	{
		return null;
	}
}); 