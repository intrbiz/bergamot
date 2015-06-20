define(function (require) 
{
    return function()
    {
		this.defaultAttrs({
			"background-colour": '#444444',
			"label-colour": '#444444',
			"title-colour": '#EEEEEE',
			"border-colour": '#444444',
			"border-width": 1,
			"legend-width": 200
		});

		this.after('initialize', function() {
			// defaults
			
			// create the paper
			
		});
		
		/* Configuration functions */
		
		this.getWidth = function()
		{
			return this.attr.width;
		};
		
		this.getHeight = function()
		{
			return this.attr.height;
		};
		
		this.getLeftPadding = function()
		{
			return 50;
		};
		
		this.getRightPadding = function()
		{
			return this.attr["legend-width"];
		};
		
		this.getLegendTop = function()
		{
			return this.getTopPadding();
		};
		
		this.getLegendLeft = function()
		{
			return this.getLeftPadding() + this.getGraphWidth() + 10;
		};
		
		this.getTopPadding = function()
		{
			return 46;
		};
		
		this.getTitleBarLeft = function()
		{
			return 10;
		};
		
		this.getTitleBarWidth = function()
		{
			return this.attr.width - 20;
		};
							
		this.getTitleBarTop = function()
		{
			return 5;
		};
		
		this.getTitleBarHeight = function()
		{
			return 30;
		};
		
		this.getBottomPadding = function()
		{
			return 40;
		};
							
		this.getGraphWidth = function()
		{
			return this.attr.width - (this.getLeftPadding() + this.getRightPadding());
		};
		
		this.getGraphHeight = function()
		{
			return this.attr.height - (this.getTopPadding() + this.getBottomPadding() );
		};
		
		/* Scaling functions */
		
		this.translateY = function(y)
		{
			return this.attr.height - y;
		};
		
		/* Drawing functions */
		
		this.drawTitle = function()
		{
			// the title bar
			this.title.frame = this.paper.rect( this.getTitleBarLeft(), this.getTitleBarTop(), this.getTitleBarWidth(), this.getTitleBarHeight(), this.getTitleBarHeight()/2).attr( { fill: this.attr["background-colour"], "stroke-width": 0 } );
			// the title text
			var titleText = this.getTitle();
			if (titleText != null)
			{
				this.title.text = this.paper.text( this.getTitleBarLeft() + (this.getTitleBarWidth() / 2), (this.getTitleBarHeight()/2) + this.getTitleBarTop(), titleText );
				this.title.text.attr( { fill: this.attr["title-colour"], "font-size": '16px', "font-weight": 'bold'} );
			}
		};
		
		this.drawLegend = function()
		{
			var linfo = this.getLegendInfo();
			if ( linfo != null )
			{
				this.legend.blocks = [];
				this.legend.titles = [];
				var t;
				for (var i = 0; i < linfo.length; i++)
				{
					this.legend.blocks.push(this.paper.rect( this.getLegendLeft(), this.getLegendTop() + (24 * i), 16, 16).attr( { fill: linfo[i].colour, "stroke-width": this.attr["border-width"], stroke: this.attr["border-colour"] } ));
					t = this.paper.text( this.getLegendLeft() + 24, this.getLegendTop() + 8 + (24 * i), linfo[i].title ).attr( { "font-size": '12px' } );
					t.attr( { x: ( t.attrs.x + (t.getBBox().width / 2) ) } );
					this.legend.titles.push(t);
				}
			}
		};
    };
});