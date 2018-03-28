/*
 * Copyright (c) 2018 Board of Trustees of Leland Stanford Jr. University,
 * all rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Stanford University shall not
 * be used in advertising or otherwise to promote the sale, use or other dealings
 * in this Software without prior written authorization from Stanford University.
 */

/*jslint devel: true, bitwise: true, regexp: true, browser: true, confusion: true, unparam: true, eqeq: true, white: true, nomen: true, plusplus: true, maxerr: 50, indent: 4 */
/*globals jQuery */

/* Based on work by:
 *  Chris Coyier (http://css-tricks.com/indeterminate-checkboxes/)
 *
 * Tristate checkbox with support features
 * pseudo selectors
 * val() overwrite
 */

(function($){
	'use strict';

	var pluginName	= 'vanderlee.tristate',
		originalVal	= $.fn.val;

	$.widget(pluginName, {
		options: {
			state:				undefined,
			value:				undefined,	// one-way only!
			checked:			undefined,
			unchecked:			undefined,
			indeterminate:		undefined,

			change:				undefined,
			init:				undefined
		},

		_create: function() {
			var that = this,
				state,
				previous = null;

			// Fix for #1
			if (window.navigator.userAgent.indexOf('Trident') >= 0) {
				this.element.click(function(e) {
					if (!this.indeterminate && $(this).attr('indeterminate')) {
						$(this).trigger('change');
					}
				});
			}

			this.element.change(function(e) {
				if (e.isTrigger || !e.hasOwnProperty('which')) {
					e.preventDefault();
				}

				// Modified to change the cycle for LOCKSS from the original
				// null -> false -> true to null -> true -> false.
				switch (that.options.state) {
					case true:  that.options.state = false; break;
					case false: that.options.state = null; break;
					default:    that.options.state = true; break;
				}

				that._refresh(that.options.change);
			});

			this.options.checked		= this.element.attr('checkedvalue')		  || this.options.checked;
			this.options.unchecked		= this.element.attr('uncheckedvalue')	  || this.options.unchecked;
			this.options.indeterminate	= this.element.attr('indeterminatevalue') || this.options.indeterminate;

			// Initially, set state based on option state or attributes
			if (typeof this.options.state === 'undefined') {
				this.options.state		= typeof this.element.attr('indeterminate') !== 'undefined'? null : this.element.is(':checked');
			}

			// If value specified, overwrite with value
			if (typeof this.options.value !== 'undefined') {
				state = this._parseValue(this.options.value);
				if (typeof state !== 'undefined') {
					this.options.state = state;
				}
			}

			this._refresh(this.options.init);

			return this;
		},

		_refresh: function(callback) {
			var value	= this.value();

			this.element.data(pluginName, value);

			this.element[this.options.state === null ? 'attr' : 'removeAttr']('indeterminate', 'indeterminate');
			this.element.prop('indeterminate', this.options.state === null);
			this.element.get(0).indeterminate = this.options.state === null;

			this.element[this.options.state ? 'attr' : 'removeAttr']('checked', true);
			this.element.prop('checked', this.options.state === true);

			if ($.isFunction(callback)) {
				callback.call(this.element, this.options.state, this.value());
			}
		},

		state: function(value) {
			if (typeof value === 'undefined') {
				return this.options.state;
			} else if (value === true || value === false || value === null) {
				this.options.state = value;

				this._refresh(this.options.change);
			}
			return this;
		},

		_parseValue: function(value) {
			if (value === this.options.checked) {
				return true;
			} else if (value === this.options.unchecked) {
				return false;
			} else if (value === this.options.indeterminate) {
				return null;
			}
		},

		value: function(value) {
			if (typeof value === 'undefined') {
				var value;
				switch (this.options.state) {
					case true:
						value = this.options.checked;
						break;

					case false:
						value = this.options.unchecked;
						break;

					case null:
						value = this.options.indeterminate;
						break;
				}
				return typeof value === 'undefined'? this.element.attr('value') : value;
			} else {
				var state = this._parseValue(value);
				if (typeof state !== 'undefined') {
					this.options.state = state;
					this._refresh(this.options.change);
				}
			}
		}
	});

	// Overwrite fn.val
    $.fn.val = function(value) {
        var data = this.data(pluginName);
        if (typeof data === 'undefined') {
	        if (typeof value === 'undefined') {
	            return originalVal.call(this);
			} else {
				return originalVal.call(this, value);
			}
		} else {
	        if (typeof value === 'undefined') {
				return data;
			} else {
				this.data(pluginName, value);
				return this;
			}
		}
    };

	// :indeterminate pseudo selector
    $.expr.filters.indeterminate = function(element) {
		var $element = $(element);
		return typeof $element.data(pluginName) !== 'undefined' && $element.prop('indeterminate');
    };

	// :determinate pseudo selector
    $.expr.filters.determinate = function(element) {
		return !($.expr.filters.indeterminate(element));
    };

	// :tristate selector
    $.expr.filters.tristate = function(element) {
		return typeof $(element).data(pluginName) !== 'undefined';
    };
}(jQuery));
