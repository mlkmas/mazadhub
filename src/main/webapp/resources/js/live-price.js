/*
 * Live price updates for the item page.
 *
 * Polls the lightweight /api/live/{itemId} endpoint every few seconds and
 * refreshes the displayed price, bid count and status without a manual reload.
 *
 * Two things matter for correctness here:
 *  - the request must NOT be served from the browser cache, or the page would
 *    keep re-displaying the price it already had (a cached response still
 *    reports HTTP 200, which makes this failure look like success);
 *  - the poll keeps running even if one request fails, so a brief network
 *    hiccup does not stop the updates permanently.
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
    var countEl = document.getElementById("liveBidCount");
    var statusEl = document.getElementById("liveStatus");
    var base = priceEl.getAttribute("data-api-base") || "";
    var lastPrice = null;

    function log() {
        if (DEBUG && window.console) {
            console.log.apply(console, ["[live-price]"].concat([].slice.call(arguments)));
        }
    }

    function refresh() {
        // Cache-buster in the URL AND no-store, so we always read the server's
        // current value rather than a cached copy.
        var url = base + "/api/live/" + itemId + "?t=" + Date.now();

        fetch(url, {
            cache: "no-store",
            headers: {
                "Accept": "application/json",
                "Cache-Control": "no-cache"
            }
        })
            .then(function (response) {
                if (!response.ok) {
                    log("HTTP", response.status);
                    return null;
                }
                return response.json();
            })
            .then(function (data) {
                if (!data) {
                    return;
                }
                log("received", data);

                var priceText = Number(data.price).toLocaleString() + " ILS";

                if (lastPrice !== null && String(data.price) !== String(lastPrice)) {
                    log("price changed", lastPrice, "->", data.price);
                    priceEl.classList.remove("price-flash");
                    void priceEl.offsetWidth;          // restart the CSS animation
                    priceEl.classList.add("price-flash");
                    setTimeout(function () {
                        priceEl.classList.remove("price-flash");
                    }, 1200);
                }

                lastPrice = data.price;
                priceEl.textContent = priceText;

                if (countEl) {
                    countEl.textContent = data.bidCount + " bids";
                }
                if (statusEl) {
                    statusEl.textContent = "Status: " + data.status;
                }
            })
            .catch(function (error) {
                log("request failed", error);          // keep polling regardless
            });
    }

    refresh();
    setInterval(refresh, INTERVAL_MS);
})();
