// Pie chart sample data
// {
// 	title: 'Pie Chart Demo',
// 	segments: [
// 		{ title:'Segment 1', value: 0.10, colour: '#FF0000' },
// 		{ title:'Segment 2', value: 0.20, colour: '#00FF00' },
// 		{ title:'Segment 3', value: 0.50, colour: '#0000FF' },
// 		{ title:'Segment 4', value: 0.10, colour: '#FFFF00' },
// 		{ title:'Segment 5', value: 0.05, colour: '#00FFFF' },
// 		{ title:'Segment 6', value: 0.05, colour: '#FF00FF' }
// 	]
// }
//
com.intrbiz.jsc.chart.PieChart = com.intrbiz.util.Class('com.intrbiz.jsc.chart.PieChart', com.intrbiz.jsc.chart.Chart,
{
	data: null,
	
	pie : null,
	
	init : function(/*string*/ id, /*int*/ width, /*int*/ height,/*Object*/ parameters, /*Object*/ data)
	{
		this.data = data;
		this.pie = {};
		// call the super
		com.intrbiz.util.superInit(com.intrbiz.jsc.chart.Chart,this,id, width, height, parameters);
	},
	
	
	getLeftPadding : function()
	{
		return 10;
	},
	
	getBottomPadding : function()
	{
		return 10;
	},
	
	drawData: function()
	{
		// calculate the centre
		var cx = this.getLeftPadding() + (this.getGraphWidth() / 2);
		var cy = this.getTopPadding() + (this.getGraphHeight() / 2);
		// calculate the radius
		var r = (this.getGraphWidth() < this.getGraphHeight() ? this.getGraphWidth() : this.getGraphHeight()) / 2;
		// draw the segments
		var ds, lx = 0, ly = -r, a = 0, x, y;
		this.pie.segments = [];
		for (var i = 0; i < this.data.segments.length; i++)
		{
			ds = this.data.segments[i];
			a += (2 * Math.PI * ds.value);
			y = Math.cos(a) * -r;
			x = Math.sin(a) * r;
			this.pie.segments.push(this.paper.path( [ "M", cx, cy, "L", cx + lx, cy + ly, "A", r, r, 0, 0, 1, cx + x, cy + y, "z" ] ).attr( { fill: ds.colour, "stroke-width": this.parameters["border-width"], stroke: this.parameters["border-colour"] } ));
			lx = x;
			ly = y;
		}
	},
	
	getLegendInfo: function()
	{
		var li = [];
		for (var i = 0; i < this.data.segments.length; i++)
		{
			li.push( { colour: this.data.segments[i].colour, title: this.data.segments[i].title } );
		}
		return li;
	},
	
	getTitle: function()
	{
		return this.data.title;
	}
});