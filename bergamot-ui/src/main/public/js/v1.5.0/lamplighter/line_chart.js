// Line graph sample data
// {
// 	title: "Line Graph Demo",
// 	"x-title": "X Axis",
// 	"y-title": "Y Axis",
// 	x: [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ],
// 	y: [
// 		{
// 			title: "Line 1",
// 			colour: "#FF0000",
// 			y: [ 0, 15, 30, 0, 15, 30, 0, 15, 30, 0 ]
// 		},
// 		{
// 			title: "Line 2",
// 			colour: "#00FF00",
// 			y: [ 30, 0, 30, 0, 30, 0, 30, 0, 30, 0 ]
// 		},
// 		{
// 			title: "Line 3",
// 			colour: "#0000FF",
// 			y: [ 15, 15, 15, 15, 15, 15, 15, 15, 15, 15 ]
// 		},
// 		{
// 			title: "Line 4",
// 			colour: "#FF00FF",
// 			y: [ 0, 3, 6, 9, 12, 15, 18, 21, 24, 27 ]
// 		}
// 	]
// }
//
com.intrbiz.jsc.chart.LineGraph = com.intrbiz.util.Class('com.intrbiz.jsc.chart.LineGraph', com.intrbiz.jsc.chart.Graph,
{
	data: null,
	
	xScale: null,
	
	yScale: null,
	
	paths: null,
	
	init : function(/*string*/ id, /*int*/ width, /*int*/ height,/*Object*/ parameters, /*Object*/ data)
	{
		this.xScale = 1;
		this.yScale = 1;
		var params = {};
		// Set our default parameters
		com.intrbiz.util.Extend({
			"line-stroke": 3,
			"axis-x-sample": 4
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
			this.xScale = this.getXScale(this.data.x);
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
		if (this.data != null)
		{
			return this.getXScale(this.data.x);
		}
		return 50;
	},
	
	getXLabels: function()
	{
		if (this.data != null)
		{
			var xl = [];
                        var len = this.data.x.length;
                        // ensure start
                        xl.push( { 
                            position: ( this.xScale * 0 ), 
                            label: (this.parameters["axis-x-formater"] ? this.parameters["axis-x-formater"].apply(this, [this.data.x[0]]) : this.data.x[0])
                        } );
                        // mid points
			for (var i = this.parameters["axis-x-sample"]; i < (len -2); i+= this.parameters["axis-x-sample"])
			{
				xl.push( { 
                                    position: ( this.xScale * i ), 
                                    label: (this.parameters["axis-x-formater"] ? this.parameters["axis-x-formater"].apply(this, [this.data.x[i]]) : this.data.x[i])
                                } );
			}
			// ensure last
			xl.push( { 
                            position: ( this.xScale * (len -1) ), 
                            label: (this.parameters["axis-x-formater"] ? this.parameters["axis-x-formater"].apply(this, [this.data.x[len -1]]) : this.data.x[len -1])
                        } );
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
	
	getPath: function(/*Array<number>*/ xdata, /*Array<number>*/ ydata)
	{
		//
		var oX = this.getLeftPadding();
		var oY = this.getBottomPadding();
		//
		var sX = this.xScale;
		var sY = this.yScale;
		var path = ["M" , ((0 * sX) + oX) , (this.translateY((ydata[0] * sY) + oY )) , "L"];
		var nc = false;
		for (var i = 1; i < xdata.length; i++) {
			path.push( ((i * sX) + oX) );
			path.push( this.translateY( (ydata[i] * sY) + oY ) );
			nc = true;
		}
		return path;
	},
	
	addDataset: function(/*Object*/ data)
	{
		this.data.y.push(data);
		this.calculateScale();
		this.redraw();
	},
	
	drawData: function()
	{
		this.paths = [];
		for(var i = 0; i < this.data.y.length; i++)
		{
			var xs = this.data.x;
			var ys = this.data.y[i];
			var path = this.paper.path( this.getPath(xs, ys.y) );
			path.attr({ "stroke-width": this.parameters["line-stroke"], stroke : ys.colour });
			this.paths.push(path);
		}
	}
});

// Sharded line graph sample data
// {
// 	title: "Sharded Line Graph Demo",
// 	"x-title": "X Axis",
// 	"y-title": "Y Axis",
// 	shards: [ "A", "B" ],
// 	x: [
// 		1, 2, 3, 4, 5, 6, 7, 8, 9, 10
// 	],
// 	y: [
// 		{
// 			title: "Line 1",
// 			colour: "#FF0000",
// 			y: [
// 				[ 0, 15, 30, 0, 15, 30, 0, 15, 30, 0 ],
// 				[ 30, 0, 30, 0, 30, 0, 30, 0, 30, 0 ]
// 			]
// 		},
// 		{
// 			title: "Line 2",
// 			colour: "#00FF00",
// 			y: [
// 				[  0,  3,  6,  9, 12, 15, 18, 21, 24, 27 ],
// 				[ 27, 24, 21, 18, 15, 12,  9,  6,  3,  0 ]
// 			]
// 		}
// 	]
// }
//
com.intrbiz.jsc.chart.ShardedLineGraph = com.intrbiz.util.Class('com.intrbiz.jsc.chart.ShardedLineGraph', com.intrbiz.jsc.chart.LineGraph,
{
	shard: null,
	
	init: function(/*string*/ id, /*int*/ width, /*int*/ height,/*Object*/ parameters, /*Object*/ data)
	{
		this.shard = 0;
		var params = {};
		// Set our default parameters
		com.intrbiz.util.Extend({
			"button-colour": '#EEEEEE',
			"button-arrow-colour": '#444'
		}, params);
		com.intrbiz.util.Extend(parameters, params);
		com.intrbiz.util.superInit(com.intrbiz.jsc.chart.LineGraph,this,id, width, height, params, data);
	},
	
	drawData: function()
	{
		this.paths = [];
		for(var i = 0; i < this.data.y.length; i++)
		{
			var xs = this.data.x;
			var ys = this.data.y[i];
			var path = this.paper.path( this.getPath(xs, ys.y[this.shard]) );
			path.attr({ "stroke-width": this.parameters["line-stroke"], stroke : ys.colour });
			this.paths.push(path);
		}
	},
	
	getTitle: function()
	{
		return this.data.title + ' - ' + this.data.shards[this.shard];
	},
	
	drawTitle: function()
	{
		// call the super to render the title
		com.intrbiz.util.superCall(com.intrbiz.jsc.chart.Chart, 'drawTitle', this);
		// previous button
		this.title.previous = this.paper.circle( this.getTitleBarLeft() + (this.getTitleBarHeight()/2), (this.getTitleBarHeight()/2) + this.getTitleBarTop(), this.getTitleBarHeight()/3).attr( { fill: this.parameters["button-colour"], "stroke-width": 0 } );
		this.title.previousArrow = this.paper.path( [ "M", (this.getTitleBarLeft() + (this.getTitleBarHeight()/2)) - ((this.getTitleBarHeight()/6) + 1), (this.getTitleBarHeight()/2) + this.getTitleBarTop() + 1, "l", this.getTitleBarHeight()/3, this.getTitleBarHeight()/6, 0, this.getTitleBarHeight()/-3, "z" ] ).attr( { fill: this.parameters["button-arrow-colour"], stroke: 0 } );
		// next button
		this.title.next = this.paper.circle( ( this.getTitleBarLeft() + this.getTitleBarWidth() ) - (this.getTitleBarHeight()/2), (this.getTitleBarHeight()/2) + this.getTitleBarTop(), (this.getTitleBarHeight()/2) - 5).attr( { fill: this.parameters["button-colour"], "stroke-width": 0 } );
		this.title.nextArrow = this.paper.path( [ "M", (this.getTitleBarLeft() + this.getTitleBarWidth()) - ((this.getTitleBarHeight()/6) + 2), (this.getTitleBarHeight()/2) + this.getTitleBarTop() + 1, "l", this.getTitleBarHeight()/-3, this.getTitleBarHeight()/6, 0, this.getTitleBarHeight()/-3, "z" ] ).attr( { fill: this.parameters["button-arrow-colour"], stroke: 0 } );
		// bind actions
		var comp = this;
		this.title.previous.node.onclick = this.title.previousArrow.node.onclick = function()
		{
			comp.previous();
		};
		this.title.next.node.onclick = this.title.nextArrow.node.onclick = function()
		{
			comp.next();
		};
	},
	
	calculateScale : function()
	{
		if (this.data != null)
		{
			// x scale
			this.xScale = this.getXScale(this.data.x);
			// find the smallest yScale to use
			this.yScale = this.getYScale(this.data.y[0].y[0]);
			for (var i = 0; i < this.data.y.length; i++)
			{
				for (var s = 0, ys; s < this.data.y[i].y.length; s++)
				{
					ys = this.getYScale(this.data.y[i].y[s]);
					if (ys < this.yScale) this.yScale = ys;
				}
			}
		}
	},
	
	reset: function()
	{
		if (this.shard > 0)
		{
			this.shard = 0;
			this.animate();
			return true;
		}
		return false;
	},
	
	next: function()
	{
		if ( this.shard < (this.data.y[0].y.length - 1))
		{
			this.shard++;
			this.animate();
			return true;
		}
		return false;
	},
	
	previous: function()
	{
		if ( this.shard > 0)
		{
			this.shard--;
			this.animate();
			return true;
		}
		return false;
	},
	
	redrawTitle: function()
	{
		if (this.title.text != null)
			this.title.text.remove();
		// the title text
		var titleText = this.getTitle();
		if (titleText != null)
		{
			this.title.text = this.paper.text( this.getTitleBarLeft() + (this.getTitleBarWidth() / 2), (this.getTitleBarHeight()/2) + this.getTitleBarTop(), titleText );
			this.title.text.attr( { fill: this.parameters["title-colour"], "font-size": '16px', "font-weight": 'bold'} );
		}
	},
	
	animate: function()
	{
		// redraw the title
		this.redrawTitle();
		// animate the line
		for(var i = 0; i < this.data.y.length; i++)
		{
			var xs = this.data.x;
			var ys = this.data.y[i];
			this.paths[i].animate({path: this.getPath(xs, ys.y[this.shard]), "stroke-width": this.parameters["line-stroke"], stroke : ys.colour }, this.getAnimateTime(), "<>");
		}
	},
	
	getAnimateTime: function()
	{
		return 250;
	}
}); 