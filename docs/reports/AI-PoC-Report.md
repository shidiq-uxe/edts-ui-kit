# AI Implementation Report: Promotion Detail and Search

**Audience:** Design manager with previous engineering experience\
**Updated:** 8 October 2026 · **PoC evidence:** 1 October 2026\
**Project:** Klik Indomaret Android PoC, `edts-ui-kit`\
**Presentation:** [AI-PoC-Report.pptx](AI-PoC-Report.pptx)

## Summary

AI implemented two connected Android flows from the supplied Figma designs: **promotion detail** and **promotion search**. The working Android demo includes product quantities, reward eligibility, search results and recovery, animated feedback, and restoration of search and cart state after an app restart.

The work used the existing Android project and UI kit. Humans supplied the designs, technical constraints and implementation approval; AI performed the incremental implementation, repairs and validation.

## Time comparison

| Work | Recorded AI task time |
|---|---:|
| Promotion detail | Approximately 1h 31m |
| Promotion search | Approximately 1h 12m |
| **Combined** | **Approximately 2h 43m** |

**Traditional estimate:** approximately three working days, supplied by the developer.

AI time is the sum of the recorded planning and implementation runs, including builds, checks and repairs. It excludes gaps between sessions, unrecorded human preparation/review, creation of the existing designs and UI kit, and this report. The traditional figure is an estimate, so these figures do not establish a measured productivity gain.

## Model and pricing

- **Model:** GPT-6.1 Sol.
- **Pricing:** the developer reports a temporary subsidy. Its rate and expiry have not been verified, so no per-prototype cost is claimed.

## What was implemented

| Flow | Coverage |
|---|---|
| **Promotion detail** | Banner and terms, product grid, quantity controls, qualifying totals, one-time/repeatable gift eligibility and selection, compact progress on scroll, floating cart, product/gift/cart sheets, sort/filter and column switching. |
| **Promotion search** | Native query editing and scope suggestions; Hadiah Gratis and Tebus Murah results; shared cart and reward state; loading, empty and error/retry states; local general-search handoff and website intent; query and shopping-state restoration. |

### Interaction behavior

- **Tap:** update quantities, open product/reward/cart sheets, submit a search or retry a failed query.
- **Scroll:** browse products and show the compact detail summary when the reward preview leaves view. Search summary and cart remain pinned.
- **Long tap:** native text selection/copy/paste in search; product inspection and demo scenario selection. Quantity controls do not repeat while held.
- **Back:** close the keyboard and return through the flow while retaining shopping state.

### Reuse and layout

The demo uses **ConstraintLayout XML** for responsive Android layouts, **ViewBinding** to connect the UI to code, and **RecyclerView** for product lists. It reuses the existing search bar, quantity controls, badges, progress bars and bottom sheets. Product tiles, reward summaries and shopping state are reused across the two flows.

Layouts adapt to screen width while allowing text to wrap and keeping artwork proportions. At text scales above 140%, products use one column. Original control dimensions and touch areas are preserved. Accessibility labels are screen-reader descriptions, not tooltips.

## Motion details

These timings are PoC implementation choices; the inspected Figma sections did not provide authored motion specifications.

| Motion | Timing | Behavior |
|---|---|---|
| Quantity control | 300 ms width transition; 150 ms internal fades | Expands/collapses between 32 and 90 dp; quantity updates immediately. |
| Reward progress | 450 ms, cubic easing `(0.2, 0.8, 0.2, 1)` | Retargets from the displayed value, including decreases. |
| Reward message | 160 ms fade with 4 dp movement | Replaces the previous message and cancels its animation. |
| Compact summary | Enter: 160 ms / 4 dp; exit: 120 ms | Appears at the scroll boundary; a small threshold buffer prevents flicker. |
| Cart entrance | 240 ms fade with 16 dp movement | Runs when the cart first becomes visible. |
| Cart feedback | Scale 1 → 1.12 → 1; 100 + 120 ms | Pulses on quantity changes, replacing any active pulse. |
| Repeat reward milestone | Scale 1 → 1.12 → 1; 120 + 140 ms | Runs when a new reward interval is earned. |
| Search transition | 160 ms fade-in | Runs when the search mode changes. |
| Sheets and keyboard | Existing UI-kit / Android transitions | Uses the existing sheet and native keyboard behavior. |

With the system animator scale set to zero, custom motion and the loading spinner are disabled; state and labels still update immediately.

## Validation

Implementation checks recorded on **1 October 2026**:

- Debug build, app unit tests and lint completed successfully.
- **14 app tests passed**, including **10 PoC state tests**; lint reported **0 errors and 293 warnings**.
- Pixel 4 / Android API 29 emulator checks covered portrait widths of 320, 360, approximately 393 and 600 dp, including 200% text scale and reduced motion. Widths use Android’s density-independent layout units (dp).
- Query, results, cart and eligibility were checked after the app process restarted.
- Screen-reader labels were checked in the UI structure.

These results are from the implementation session; app checks were not rerun for this report edit.

## Scope and remaining checks

This is a local PoC using sample data and demo navigation. Backend integration, checkout and redemption charging are outside its scope. Before a production handoff, confirm business rules and complete live TalkBack/Switch Access testing, wider device coverage and animation performance measurements.

## References

- [Promotion detail design](https://www.figma.com/design/Wd140rqS27zbhDNDSpy9PB/0.2-Klik-Indomaret---Promotion--Grocery-?node-id=28576-21090) · [Search design](https://www.figma.com/design/Wd140rqS27zbhDNDSpy9PB/0.2-Klik-Indomaret---Promotion--Grocery-?node-id=54442-186207)
- [Detail implementation guide](../ParagonFairDemo.md) · [Search implementation guide](../PromoSearchDemo.md)
