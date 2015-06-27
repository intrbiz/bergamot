define(['flight/lib/component','lamplighter/lib/chart/chart', 'lamplighter/lib/chart/graph', 'raphael/raphael'], function (defineComponent, chart, graph, Raphael) 
{
    return defineComponent(function()
    {
		this.defaultAttrs({
			"line-stroke": 3,
			"axis-x-sample": function() { return function(l) { return 4; }; }
		});
		
		this.after('initialize', function() {
			// defaults
			this.title  = {};
			this.legend = {};
			this.axis   = {};
			this.xScale = 1;
			this.yScale = 1;
			this.paths  = null;
			// the paper
			this.paper = Raphael(this.node, this.attr.width, this.attr.height);
			// draw
			this.draw();
			// events
			this.on('redraw', this.redraw);
		});
		
		this.calculateScale = function()
		{
			if (this.attr.data != null)
			{
				// x scale
				this.xScale = this.getXScale(this.attr.data.x);
				// find the smallest yScale to use
				this.yScale = this.getYScale(this.attr.data.y[0].y);
				for (var i = 1, ys; i < this.attr.data.y.length; i++)
				{
					ys = this.getYScale(this.attr.data.y[i].y);
					if (ys < this.yScale) this.yScale = ys;
				}
			}
		};
		
		this.getTitle = function()
		{
			return this.attr.data.title;
		};
		
		this.getXGridSpacing = function()
		{
			if (this.attr.data != null && this.attr.data.x != null && this.attr.data.x.length > 1)
			{
				var xSample = this.attr["axis-x-sample"].apply(this, [this.attr.data.x.length]);
				return (this.getGraphWidth() * xSample) / this.attr.data.x.length;
			}
			return 40;
		};
		
		this.getYGridSpacing = function()
		{
			return 30;
		};
		
		this.getXLabels = function()
		{
			if (this.attr.data != null)
			{
				var xl = [];
				var len = this.attr.data.x.length;
				// ensure start
				xl.push( { 
					position: ( this.xScale * 0 ), 
					label: (this.attr["axis-x-formater"] ? this.attr["axis-x-formater"].apply(this, [this.attr.data.x[0]]) : this.attr.data.x[0])
				} );
				// mid points
				var xSample = this.attr["axis-x-sample"].apply(this, [this.attr.data.x.length]);
				for (var i = xSample; i < (len - (xSample /2)); i+= xSample)
				{
					xl.push( { 
						position: ( this.xScale * i ), 
						label: (this.attr["axis-x-formater"] ? this.attr["axis-x-formater"].apply(this, [this.attr.data.x[i]]) : this.attr.data.x[i])
					} );
				}
				// ensure last
				xl.push( { 
					position: ( this.xScale * (len -1) ), 
					label: (this.attr["axis-x-formater"] ? this.attr["axis-x-formater"].apply(this, [this.attr.data.x[len -1]]) : this.attr.data.x[len -1])
				} );
				return xl;
			}
			return null;
		};
		
		this.getYLabels = function()
		{
			if (this.attr.data != null)
			{
				var yl = [];
				for (var i = 0; i < this.getGraphHeight(); i += this.getYGridSpacing())
				{
					yl.push( { position: i, label: ('' + ( i / this.yScale ).toFixed(1)) } );
				}
				return yl;
			}
			return null;
		};
		
		this.getLegendInfo = function()
		{
			var li = [];
			for (var i = 0 ; i < this.attr.data.y.length; i++)
			{
				li.push( { colour: this.attr.data.y[i].colour, title: this.attr.data.y[i].title } );
			}
			return li;
		};
		
		this.getPath = function(/*Array<number>*/ xdata, /*Array<number>*/ ydata)
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
		};
		
		this.addDataset = function(/*Object*/ data)
		{
			this.attr.data.y.push(data);
			this.calculateScale();
			this.redraw();
		};
		
		this.drawData = function()
		{
			this.paths = [];
			for(var i = 0; i < this.attr.data.y.length; i++)
			{
				var xs = this.attr.data.x;
				var ys = this.attr.data.y[i];
				var path = this.paper.path( this.getPath(xs, ys.y) );
				path.attr({ "stroke-width": this.attr["line-stroke"], stroke : ys.colour });
				this.paths.push(path);
			}
		};
		
		this.draw = function()
		{
			this.calculateScale();
			this.drawTitle();
			this.drawLegend();
			this.drawAxis(this.getXGridSpacing(), this.getYGridSpacing(), this.getXLabels(), this.getYLabels());
			this.drawData();
		};
		
		this.redraw = function(/*Event*/ ev, /*Object*/ data)
		{
			if (data) this.attr.data = data;
			// redraw
			this.paper.clear();
			this.draw();
		};
	
    }, graph, chart);
});