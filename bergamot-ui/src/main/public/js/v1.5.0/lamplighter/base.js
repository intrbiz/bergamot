/*
 * Intrbiz JSC JS Library V1.0
 * Copyright (C) 2010 Chris Ellis
 *
 */

/* Create the namespaces */
window.com = {};
com.intrbiz = {};
com.intrbiz.jsc = {};
com.intrbiz.util = {};

/*
 * Create a class com.intrbiz.util.Class(classname, prototypes...) This function will create
 * a class from a name and series of superclasses
 * 
 * This works by creating a constructor function which delegates to the
 * implemenation function 'init' All superclasses and implementation and merged
 * into a single object This merged (extended) object becomes the prototype of
 * the constructor function
 * 
 * Usage: class = com.intrbiz.util.Class(name, superclass, implementation);
 */
com.intrbiz.util.Class = function()
{
	var className = arguments[0];
	var cls = function()
	{
		this.init.apply(this, arguments); /* invoke the constructor */
		return true;
	};
	var extended = {};
	var parent = null;
	/* Extend */
	/*
	 * Functions do not have properties but do have a prototype, therefore when
	 * extending a function the prototype must be used
	 */
	for ( var i = 1; i < arguments.length; i++)
	{
		if (typeof arguments[i] == 'function')
			parent = arguments[i].prototype;
		else
			parent = arguments[i];
		com.intrbiz.util.Extend(parent, extended);
	}
	/* Set ClassName */
	extended.classname = className;
	/* Inherit */
	cls.prototype = extended;
	return cls
}

/*
 * Copy all properties from src -> dst
 * 
 * This function will copy all properties from the src object to the dst object.
 * This effectively allows for extention of objects.
 * 
 */
com.intrbiz.util.Extend = function(src, dst)
{
	if (src)
	{
		for ( var prop in src)
		{
			var val = src[prop];
			if (val != undefined)
			{
				dst[prop] = val;
			}
		}
		/* IE does not include toString, however ensure not to break FF2 */
		if ((!(typeof window.Event == "function" && src instanceof window.Event)) && src.hasOwnProperty && src.hasOwnProperty('toString'))
		{
			dst.toString = src.toString;
		}
	}
}

com.intrbiz.util.getClassName = function(obj)
{
	if (obj && obj.classname)
		return obj.classname;
	return null;
}

/*
 * invoke the constructor of the specified superclass superInit(class, object,
 * arguments ...)
 */
com.intrbiz.util.superInit = function()
{
	var args = [];
	var cls = arguments[0];
	var obj = arguments[1];
	for ( var i = 2; i < arguments.length; i++)
		args.push(arguments[i]);
	// invoke
	cls.prototype.init.apply(obj, args);
}

com.intrbiz.util.superCall = function()
{
	var args = [];
	var cls = arguments[0];
	var fnm = arguments[1];
	var obj = arguments[2];
	for ( var i = 3; i < arguments.length; i++)
		args.push(arguments[i]);
	// invoke
	cls.prototype[fnm].apply(obj, args);
}

com.intrbiz.Object = com.intrbiz.util.Class('com.intrbiz.Object',
{
	init : function()
	{
	},

	toString : function()
	{
		return "Class: " + this.classname;
	}
});
