/*
 * Live price updates for the item page.
 *
 * Polls the lightweight /api/live/{itemId} endpoint every few seconds and
 * refreshes the displayed price, bid count and status without a manual reload.
 * The values it shows are produced by the same bidding flow that publishes the
 * JMS events, so the page always reflects the server's current state.
 */
(function () {
    "use strict";

    var priceEl = document.getElementById("livePrice");
    if (!priceEl) {
        return;                       // not on the item page
    }

    var itemId = priceEl.getAttribute("data-item-id");
    var countEl = document.getElementById("liveBidCount");
    var statusEl = document.getElementById("liveStatus");
    var base = priceEl.getAttribute("data-api-base") || "";
    var lastPrice = null;

    function refresh() {
        fetch(base + "/api/live/" + itemId, { headers: { "Accept": "application/json" } })
            .then(function (r) { return r.ok ? r.json() : null; })
            .then(function (data) {
                if (!data) {
                    return;
                }
                var shown = Number(data.price).toLocaleString() + " ILS";
                if (lastPrice !== null && data.price !== lastPrice) {
                    priceEl.classList.add("price-flash");
                    setTimeout(function () { priceEl.classList.remove("price-flash"); }, 1200);
                }
                lastPrice = data.price;
                priceEl.textContent = shown;
                if (countEl) {
                    countEl.textContent = data.bidCount + " bids";
                }
                if (statusEl) {
                    statusEl.textContent = "Status: " + data.status;
                }
            })
            .catch(function () { /* transient network error: try again next tick */ });
    }

    refresh();
    setInterval(refresh, 5000);
})();
