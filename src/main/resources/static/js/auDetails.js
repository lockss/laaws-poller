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

$(document).ready(function () {
    $("#tabs").tabs({
        beforeLoad: function( event, ui ) {
            ui.jqXHR.error(function() {
                ui.panel.html(
                    "Couldn't load this tab. We'll try to fix this as soon as possible. " +
                        "If this wouldn't be a demo." );
            });
        }
    });
});

$(".selector").tabs({
    selected: 0
});

function updateDiv(divToChange, key, pubImage) {
    key = encodeKey(key);
    var rowTxt, cellTxt, x, i, xmlhttp, crawlError, pollError;
    var headerArray = ["Plugin", "Access Type", "Content Size", "Disk Usage (MB)",
        "Repository"];
    if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp = new XMLHttpRequest();
    } else {// code for IE6, IE5
        xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
    }
    xmlhttp.onreadystatechange = function () {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            xmlDoc = xmlhttp.responseXML;
            rowTxt = "<a href=\"javascript:closeDiv('" + divToChange + "', '" +
                key + "', '" + pubImage + "')\"><img id =\"" +
                pubImage + "\" class=\"title-icon\" src=\"images/collapse.png\">"
                + divToChange.replace(/_/g, " ") + "</a>";
            cellTxt = "<br /><table class=\"detail-table\"><tbody>";
            x = xmlDoc.getElementsByTagNameNS("http://lockss.org/statusui", "summaryinfo");
            for (i = 0; i < x.length; i++) {
                var title = x[i].getElementsByTagNameNS("http://lockss.org/statusui",
                    "title")[0].textContent;
                var value = x[i].getElementsByTagNameNS("http://lockss.org/statusui",
                    "value")[0].textContent;
                if (title != "" && headerArray.indexOf(title) != -1) {
                    cellTxt += "<tr><td class=\"title-cell\">" + title
                        + "</td><td class=\"value-cell\">" + value + "</td></tr>";
                }
                if (getCrawlError(x, i, title) != null) {
                    crawlError = getCrawlError(x, i, title);
                }
                if (getPollError(x, i, title) != null) {
                    pollError = getPollError(x, i, title);
                }
            }
            if (crawlError != null) {
                cellTxt += "<tr><td colspan='2'><div class=\"au-error\">Last Crawl Result - " + crawlError
                    + " <a class=\"error-info\" href=\"javascript:void(0);\" title=\"Error\"><img id=\"" + divToChange
                    + "_error\" src=\"images/button_info.png\" /></a></div></td></tr>";
            }
            if (pollError != null) {
                cellTxt += "<tr><td colspan='2'><div class=\"au-error\">Last Poll Result - " + pollError
                    + " <img src=\"images/button_info.png\" /></div></td></tr>";
            }
            cellTxt += "</tbody></table><br />";
            cellTxt += "<div class='au-details'>[ <a class=\"au-link\"\n\
     href=\"DaemonStatus?table=ArchivalUnitTable&key=" + key + "\" \n\
target=\"_blank\">View Detailed AU information</a><sup><img src=\"images/external_link.png\"/></sup> ]</div>";
            cellTxt += "<div class='remove-au-div'><form method=\"POST\" \n\
action=\"/DisplayContentStatus\" class=\"remove-au-form\">\n\
<input name=\"lockssAction\" type=\"hidden\"><input name=\"removeAu\" value="
                + key + " type=\"hidden\">[ <input name=\"\" class=\"remove-au-button\" value=\"Remove AU\" \n\
type=\"button\" id=\"Ab_Imperio_Volume_2011\" onClick=\"if(confirm('Confirm deletion of AU: "
                + divToChange.replace(/_/g, " ") + "'))lockssButton(this, 'DoRemoveAus')\"> ] " +
                "<a class=\"remove-info\" href=\"javascript:void(0);\" title=\"Removes this AU\"><img id=\"" +
                divToChange + "_info\" src=\"images/button_info.png\" /></a></form></div>";
            document.getElementById(divToChange + "_title").innerHTML = rowTxt;
            var rowId = divToChange + "_cell";
            document.getElementById(rowId).innerHTML = cellTxt;
            document.getElementById(divToChange + "_row").className =
                document.getElementById(divToChange + "_row").className.replace(/\bhide-row\b/, '');
            $( ".remove-info" ).tooltip({position:{my:"center bottom-20",at:"center top",of:"#"+divToChange+"_info",
                using: function(position,feedback){$(this).css(position);$("<div>")
                .addClass("arrow").addClass(feedback.vertical).addClass(feedback.horizontal).appendTo(this);}}});
            $( ".error-info" ).tooltip({position:{my:"center bottom-20",at:"center top",of:"#"+divToChange+"_error",
                using: function(position,feedback){$(this).css(position);$("<div>")
                    .addClass("arrow").addClass(feedback.vertical).addClass(feedback.horizontal).appendTo(this);}}});
        }
    }
    xmlhttp.open("GET", "DaemonStatus?table=ArchivalUnitTable&key=" + key
        + "&output=xml", true);
    xmlhttp.send();
}

function getCrawlError(x, i, title) {
    var crawlErrorArray = ["Can't fetch permission page", "Fetch error", "Interrupted by daemon exit", "No permission from publisher"];
    if (title == "Last Crawl Result") {
        var value = x[i].getElementsByTagNameNS("http://lockss.org/statusui", "value")[0].textContent;
        if (value != "" && crawlErrorArray.indexOf(value) != -1) {
            return value;
        }
    }
    return null;
}

function getPollError(x, i, title) {
    var pollErrorArray = ["Error", "Aborted"];
    if (title == "Last Poll Result") {
        var value = x[i].getElementsByTagNameNS("http://lockss.org/statusui", "value")[0].textContent;
        if (value != "" && pollErrorArray.indexOf(value) != -1) {
            return value;
        }
    }
    return null;
}

function closeDiv(divToClose, key, pubImage) {
    key = encodeKey(key);
    var txt = "<a href=\"javascript:updateDiv('" + divToClose + "', '" + key
        + "')\"><img id =\"" + pubImage
        + "\" class=\"title-icon\" src=\"images/expand.png\"/>"
        + divToClose.replace(/_/g, " ") + "</a>";
    document.getElementById(divToClose + "_row").setAttribute("class",
        document.getElementById(divToClose + "_row").className + " hide-row");
    document.getElementById(divToClose + "_title").innerHTML = txt;
}

function hideRows(styleClass, hrefId, pubImage) {
    $('.' + styleClass).addClass('hide-row');
    document.getElementById(hrefId).href = "javascript:showRows('" + styleClass
        + "', '" + hrefId + "', '" + pubImage + "')";
    document.getElementById(pubImage).src = "images/expand.png";
}

function showRows(styleClass, hrefId, pubImage) {
    $('.' + styleClass).removeClass('hide-row');
    document.getElementById(hrefId).href = "javascript:hideRows('" + styleClass
        + "', '" + hrefId + "', '" + pubImage + "')";
    document.getElementById(pubImage).src = "images/collapse.png";
}

function encodeKey(key) {
    return encodeURI(key).replace(/&/g, "%26").replace(/~/g, "%7E");
}