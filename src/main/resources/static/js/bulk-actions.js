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

/**
 * Manage Bulk Actions button on subscription page
 *
 * Picks up the action in the select box and
 * applies it to every triboxes in the selected rows.
 */

$(document).ready(function () {
    $("#bulk-actions-menu").shiftSelectable();

    $("#bulk-actions-btn").click(function(){
        $("#bulk-actions-msg-box").text("");

        var action = $("#bulk-actions-menu option:selected").val();
        if(action == ""){
            $("#bulk-actions-msg-box").removeClass("success")
            $("#bulk-actions-msg-box").addClass("warning")
            $("#bulk-actions-msg-box").text("Please select an action first.");
        } else {
            var count = 0;

            $(".bulk-actions-ckbox:checked").each( function(){
                count++;
                $(this).prop('checked', false);

                // Get id from check box
                var publicationNumber = $(this).attr('id').replace("bulk-action-ckbox_","");

                // Get tribox
                var triboxSelector = "#publicationSubscription"+publicationNumber;

                // Update tribox
                switch (action) {
                    case "subscribeAll":
                        $(triboxSelector).tristate('state', true);
                        break;
                    case "unsubscribeAll":
                        $(triboxSelector).tristate('state', false);
                        break;
                    case "unsetAll":
                        $(triboxSelector).tristate('state', null);
                        break;
                }
                refreshPublicationSubscription(
                        "publicationSubscription" + publicationNumber + "Hidden",
                        "subscribedRanges" + publicationNumber,
                        "unsubscribedRanges" + publicationNumber)
            });
            $("#bulk-actions-msg-box").removeClass("warning")
            $("#bulk-actions-msg-box").addClass("success")
            if(count > 0){
                $("#bulk-actions-msg-box").text(count+" selections have been set for "+action+". Press the 'Add' button to apply the change.");
            } else {
                $("#bulk-actions-msg-box").removeClass("success")
                $("#bulk-actions-msg-box").addClass("warning")
                $("#bulk-actions-msg-box").text("You haven't selected any publication.")
            }
        }
    });


    /*
     * Rewrite the code for the tribox as it needs to works slightly differently.
     *
     * This function set every elements in the same row than the tribox
     * which has change status after the bulk action as been applied.
     */
    function refreshPublicationSubscription(
            pubSubId,
            subscribedRangesId,
            unsubscribedRangesId) {
        // The publication subscription tribox.
        var pubSub = document.getElementById(pubSubId);

        // The subscribed ranges input box.
        var subscribedRanges = document
                .getElementById(subscribedRangesId);

        // The unsubscribed ranges input box.
        var unsubscribedRanges = document
                .getElementById(unsubscribedRangesId);

        // Check whether the publication subscription is
        // changing to "Not Set".
        if (pubSub.value == "unset") {
            // Yes: Enable both ranges.
            if (subscribedRanges !== undefined) {
                subscribedRanges.disabled = false;
            }
            if (unsubscribedRanges !== undefined) {
                unsubscribedRanges.disabled = false;
            }
            return;
        }

        // No: Disable the subscribed ranges.
        if (subscribedRanges !== "unset") {
            subscribedRanges.disabled = true;
        }

        // Check whether the publication subscription is
        // changing to "Unsubscribe All".
        if (pubSub.value == "false") {
            // Yes: Disable the unsubscribed ranges.
            if (unsubscribedRanges !== undefined) {
                unsubscribedRanges.disabled = true;
            }
        } else {
            // No: Enable the unsubscribed ranges.
            if (unsubscribedRanges !== undefined) {
                unsubscribedRanges.disabled = false;
            }
        }
    }
});