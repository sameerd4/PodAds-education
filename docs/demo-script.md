# Demo Script (60-90 seconds)

## Opening (5s)
"Hi, I'm [Name], and this is PodAds Lab - an inspectable podcast ad server simulator that shows how ad decisions are made end-to-end."

## The Pipeline (20s)
"Every ad decision follows a 5-step pipeline: Request, Filters, Auction, Serve, and Metrics. Let me show you a live decision."

[Click "Run Once"]

"Here we see a request for a fitness podcast, mid-roll slot. The system sources candidates, runs them through 12 filters - campaign status, geo targeting, budget, pacing - and then scores them in an auction."

## The Auction (15s)
"The auction board shows all candidates with their bid CPM, match score, and pacing multiplier. The final score determines the winner. In this case, Nike wins with a score of $7.20."

[Point to winner]

## Explainability (15s)
"One of the key features is explainability. Let me show you why this ad won."

[Click "Explain Winner"]

"The winner won because of three factors: high bid CPM, strong category match, and good pacing multiplier. The runner-up lost because of a lower final score."

## Observability (10s)
"At the bottom, we see real-time metrics - fill rate, latency, and filter drop-offs. This is what production observability looks like."

## Closing (5s)
"Every decision is deterministic and replayable using a seed. This makes debugging and analysis straightforward. Check out the repo link in the description!"

---

**Total: ~70 seconds**

## Tips
- Keep it conversational
- Don't rush through the pipeline steps
- Emphasize the explainability feature
- Show the metrics strip at the end
- End with a call to action (repo link)


