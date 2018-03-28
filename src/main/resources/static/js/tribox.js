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

// Communication between the visible tristate widgets and their corresponding
// hidden input widgets.
//
// The hidden input widget is needed to communicate with the server the state of
// the tristate widget, because the tristate widget is seen by the server as a
// plain two-state check box.
//
// This code is applied to all the objects of class "tribox"
//
// W.P.: Made some modification to the JavaScript so that triboxes in a single
//       tab can be initialised without changing triboxes in other tabs.

$(document).ready(function () {
    initTribox($('form'))
});

function initTribox(panel) {
    panel.find(".tribox").tristate(triboxOptions);
}

var triboxOptions = {
    // To be run on tristate widget initialization (page load).
    init: function(state, value) {
        // Get the hidden input widget.
        var hidden = document.getElementById(this.attr('id') + "Hidden");
        // Get the text widget.
        var textSpan = document.getElementById(this.attr('id') + "Text");
        // Initialize the tristate widget and its text representation according to
        // the initial value of the hidden input widget.
        if (hidden.value === "unset") {
            this.tristate('state', null);
            textSpan.innerHTML = 'Not Set';
        } else {
            this.tristate('state', hidden.value === "true");
            if (hidden.value === "true") {
                textSpan.innerHTML = 'Subscribe All';
            } else {
                textSpan.innerHTML = 'Unsubscribe All';
            }
        }
    }
    ,
    // To be run when the tristate widget changes (user click).
    change: function(state, value) {
        // Get the hidden input widget.
        var hidden = document.getElementById(this.attr('id') + "Hidden");
        // Get the text widget.
        var textSpan = document.getElementById(this.attr('id') + "Text");
        // Populate the hidden input widget and its text representation according
        // to the changed value of the tristate widget.
        if (state === null) {
            hidden.value = "unset";
            textSpan.innerHTML = 'Not Set';
        } else {
            hidden.value = state;
            if (state === true) {
                textSpan.innerHTML = 'Subscribe All';
            } else {
                textSpan.innerHTML = 'Unsubscribe All';
            }
        }
    }
}
