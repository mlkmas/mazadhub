/*
 * Live update for the item page.
 *
 * Polls the lightweight /api/live/{itemId} endpoint every few seconds. While
 * nothing has changed it simply keeps the displayed price in step. As soon as
 * the server reports a DIFFERENT price or status — someone else bid, or the
 * auction closed — the page is reloaded so that every part of it is refreshed
 * together: the current price, the next minimum bid, the bid history, the
 * status and the available actions.
 *
 * Reloading (rather than patching each element in JavaScript) keeps JSF as the
 * single source of truth, so the view can never drift out of step with the
 * server.
 *
 * Note: the request must not be served from the browser cache — a cached copy
 * still reports HTTP 200, which would silently freeze the page.
 */
(function () {
    "use strict";

    var DEBUG = false;              // set to true to trace polling in the console
    var INTERVAL_MS = 5000;

    var priceEl = document.getElementById("livePrice");
    if (!priceEl) {
        return;                     // not on the item page
    }

    var itemId = priceEl.getAttribute("data-item-id");
    var base = priceEl.getAttribute("data-api-base") || "";

    // Baseline: what the server rendered into this page.
    var knownPrice = null;
    var knownStatus = null;
    var reloading = false;

    function log() {
        if (DEBUG && window.console) {
            console.log.apply(console, ["[live-price]"].concat([].slice.call(arguments)));
        }
    }

    function refresh() {
        if (reloading) {
            return;
        }
        var url = base + "/api/live/" + itemId + "?t=" + Date.now();

        fetch(url, {
            cache: "no-store",
            headers: { "Accept": "application/json", "Cache-Control": "no-cache" }
        })
            .then(function (response) {
                return response.ok ? response.json() : null;
            })
            .then(function (data) {
                if (!data) {
                    return;
                }
                log("received", data);

                var price = String(data.price);
                var status = String(data.status);

                // First poll establishes the baseline for this page load.
                if (knownPrice === null) {
                    knownPrice = price;
                    knownStatus = status;
                    priceEl.textContent = Number(price).toLocaleString() + " ILS";
                    return;
                }

                if (price !== knownPrice || status !== knownStatus) {
                    log("changed", knownPrice, "->", price, "| reloading");
                    reloading = true;

                    // Show the new price immediately, flash it, then reload so
                    // the minimum, history and actions all refresh together.
                    priceEl.textContent = Number(price).toLocaleString() + " ILS";
                    priceEl.classList.add("price-flash");

                    setTimeout(function () {
                        window.location.reload();
                    }, 900);
                }
            })
            .catch(function (error) {
                log("request failed", error);        // keep polling regardless
            });
    }

    refresh();
    setInterval(refresh, INTERVAL_MS);
})();
