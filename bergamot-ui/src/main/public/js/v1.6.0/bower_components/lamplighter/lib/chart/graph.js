define(function (require) 
{
    return function()
    {
		this.defaultAttrs({
			"grid-colour": '#888888',
			"grid-stroke": 1,
            "y-starts-at-zero": true,
            "x-starts-at-zero": true
		});
		
		/* Configuration functions */
		
		this.getXLabelsMargin = function()
		{
			return 25;
		};
		
		this.getYLabelsMargin = function()
		{
			return 35;
		};
		
		this.getStartXAtZero = function()
		{
			return this.attr["x-starts-at-zero"];
		};
		
		this.getStartYAtZero = function()
		{
			return this.attr["y-starts-at-zero"];
		};
		
		/* Scaling functions */
		
		this.min = function(/*Array<number>*/ data)
		{
			var min = data[0];
			for (var i = 1; i < data.length; i++)
				if (min > data[i]) min = data[i];
			return min;
		};

		this.max = function(/*Array<number>*/ data)
		{
			var max = data[0];
			for (var i = 1; i < data.length; i++)
				if (max < data[i]) max = data[i];
			return max;
		};
		
		this.getXScale = function(/*Array<number>*/ data)
		{
			return data.length == 0 ? this.getGraphWidth() / 2 : this.getGraphWidth() / (data.length - 1) ;
		};
		
		this.getYScale = function(/*Array<number>*/ data)
		{
			var min = this.getStartYAtZero() ? 0 : this.min(data);
			var max = this.max(data);
			return (max - min) <= 1 ? (this.getGraphHeight() - 5) / 2 : (this.getGraphHeight() - 5) / (max - min);
		};
		
		/* Drawing functions */
		
		this.drawAxis = function(/*number*/ xSpacing, /*number*/ ySpacing, /*Array<Object<position:Number,label:String>>*/ xLabels, /*Array<Object<position:Number,label:String>>*/ yLabels)
		{
			var axisWidth = this.getGraphWidth();
			var axisHeight = this.getGraphHeight();
			// axis background
			this.axis.frame = this.paper.rect(this.getLeftPadding(), this.getTopPadding(), axisWidth, axisHeight);
			this.axis.frame.attr( { fill: this.attr["background-colour"], "stroke-width": 0 } );
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
			this.axis.grid.attr({ "stroke-width": this.attr["grid-stroke"], stroke : this.attr["grid-colour"] });
			// axis line
			this.axis.path = this.paper.path( ["M", this.getLeftPadding(), this.getTopPadding(), "L", this.getLeftPadding(), this.translateY(this.getBottomPadding()), (this.width - this.getRightPadding()), this.translateY(this.getBottomPadding())] );
			this.axis.path.attr({ "stroke-width": this.attr["grid-stroke"], stroke : this.attr["grid-colour"] });
			// x labels
			this.axis.xLabels = [];
			if (xLabels != null)
			{
				for (var i = 0 ; i < xLabels.length; i++)
				{
					var txt = this.paper.text( xLabels[i].position + this.getLeftPadding(), this.translateY(this.getBottomPadding() - this.getXLabelsMargin()), xLabels[i].label );
					txt.attr( { "fill": this.attr["label-colour"], "font-size": '12px' } );
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
					txt.attr( { "fill": this.attr["label-colour"], "font-size": '12px' } );
					this.axis.yLabels.push(txt);
				}
			}		
		};

    };
});