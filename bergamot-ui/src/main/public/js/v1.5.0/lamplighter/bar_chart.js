// Bar chart sample data
// {
// 	title: "Bar Chart Demo",
// 	"x-title": "X Axis",
// 	"y-title": "Y Axis",
// 	x: [
// 		"Cat A", "Cat B", "Cat C", "Cat D", "Cat E"
// 	],
// 	y: [
// 		{
// 			title: "Series 1",
// 			colour: "#FF0000",
// 			y: [ 10, 20, 30, 40, 50 ]
// 		},
// 		{
// 			title: "Series 2",
// 			colour: "#00FF00",
// 			y: [ 50, 40, 30, 20, 10 ]
// 		}
// 	]
// }
//
com.intrbiz.jsc.chart.BarChart = com.intrbiz.util.Class('com.intrbiz.jsc.chart.BarGraph', com.intrbiz.jsc.chart.Graph,
{
	data: null,
	
	xScale: null,
	
	yScale: null,
	
	barsets: null,
	
	init : function(/*string*/ id, /*int*/ width, /*int*/ height,/*Object*/ parameters, /*Object*/ data)
	{
		this.xScale = 1;
		this.yScale = 1;
		var params = {};
		// Set our default parameters
		com.intrbiz.util.Extend({
			"bar-stroke": 0,
			"bar-stroke-colour": '#444444', 
			"axis-x-sample": 1
		}, params);
		com.intrbiz.util.Extend(parameters, params);
		//
		this.data = data;
		// call the super
		com.intrbiz.util.superInit(com.intrbiz.jsc.chart.Graph,this,id, width, height, params);
		this.calculateScale();
	},
	
	calculateScale : function()
	{
		if (this.data != null)
		{
			// x scale
			this.xScale = this.getGraphWidth() / this.data.x.length;
			// find the smallest yScale to use
			this.yScale = this.getYScale(this.data.y[0].y);
			for (var i = 1, ys; i < this.data.y.length; i++)
			{
				ys = this.getYScale(this.data.y[i].y);
				if (ys < this.yScale)
					this.yScale = ys;
			}
		}
	},
	
	getTitle: function()
	{
		return this.data.title;
	},
	
	getXGridSpacing: function()
	{
		return null;
	},
	
	getXLabels: function()
	{
		if (this.data != null)
		{
			var xl = [];
			for (var i = 0; i < this.data.x.length; i+= this.parameters["axis-x-sample"] )
			{
				xl.push( { position: ( (this.xScale * i) + (this.xScale / 2) ), label: this.data.x[i] } );
			}
			return xl;
		}
		return null;
	},
	
	getYLabels: function()
	{
		if (this.data != null)
		{
			var yl = [];
			for (var i = 0; i < this.getGraphHeight(); i += this.getYGridSpacing())
			{
				yl.push( { position: i, label: ('' + ( i / this.yScale ).toFixed(1)) } );
			}
			return yl;
		}
		return null;
	},
	
	getLegendInfo: function()
	{
		var li = [];
		for (var i = 0 ; i < this.data.y.length; i++)
		{
			li.push( { colour: this.data.y[i].colour, title: this.data.y[i].title } );
		}
		return li;
	},
	
	addDataset: function(/*Object*/ data)
	{
		this.data.y.push(data);
		this.calculateScale();
		this.redraw();
	},
	
	drawData: function()
	{
		this.barsets = [];
		for(var i = 0; i < this.data.y.length; i++)
		{
			var ys = this.data.y[i];
			var bars = [];
			for (var j = 0; j < ys.y.length; j++)
			{
				var bar = this.paper.rect( this.getLeftPadding() + ( this.xScale * j ) + (this.xScale * 0.1) + (((this.xScale * 0.8)/this.data.y.length) * i), (this.getGraphHeight() + this.getTopPadding()) - (this.yScale * ys.y[j]), ((this.xScale * 0.8)/this.data.y.length), (this.yScale * ys.y[j]) ).attr( { fill: ys.colour, stroke: this.parameters["bar-stroke-colour"], "stroke-width": this.parameters["bar-stroke"] } );
			}
			this.barsets.push(bars);
		}
	}
}); 