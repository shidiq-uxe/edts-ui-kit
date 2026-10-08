# Paragon Fair Activity prototype

## Open and demonstrate

Branch: `experimental/paragon-fair-demo`, created from fetched `origin/development` at `819aca2`.

Build/run the `app` debug variant. On the sample app home menu, select **Paragon Fair · Promo Detail Demo**. Long press **Detail Promo** to open the scenario selector. The title also exposes an accessibility long-click action named **Pilih skenario demo**.

Suggested presentation:

1. Add Emina Water Barrier Balancing Gel once: quantity 1, Rp50.000, halfway to a gift.
2. Add again: quantity 2, Rp100.000, one gift eligible. Scroll until the gift preview leaves the viewport; the compact summary appears beneath the toolbar.
3. Tap **Lihat**, choose a gift, then dismiss the sheet. The summary reports one gift selected.
4. Reduce quantity below the threshold. Gift claims that exceed eligibility are removed; return to zero to hide the cart.
5. Choose **Berlaku kelipatan · 1 dari 2 hadiah** using the scenario selector. This uses Rp200.000 qualifying spend, two earned gifts, one selected gift.
6. Try brand filtering, sorting, two/three columns, product inspection and the cart summary.
7. Choose quota exhausted, fair limit, first transaction only, or all products unpublished to show the corresponding states.

## Source and decisions

Source: [Figma section 28576:21090](https://www.figma.com/design/Wd140rqS27zbhDNDSpy9PB/0.2-Klik-Indomaret---Promotion--Grocery-?node-id=28576-21090).

Inspected baseline `28576:38478`, eligible/claimed frames, scroll snapshots, repeatable reward variants, quota/limit/first-transaction restrictions, and zero products. The baseline is 360 px wide. The toolbar, banner, full progress/gift summary, promo terms, product grid and floating footer establish the page order.

No authored motion nodes were returned for the inspected section. Animation timings and long-press behaviors below are prototype proposals. Product detail, filter, gift and cart sheets are local demo interfaces; their layouts are not claimed as Figma destinations. The later [promotion-search implementation](PromoSearchDemo.md) follows the supplied search section and documents its local general-search handoff.

The repeatable reference contains conflicting copy and amounts: a repeatable label with “Tidak Berlaku Kelipatan” in body copy, and “1 dari 2” gifts alongside Rp100.000. The prototype uses consistent repeatable copy and Rp200.000 for two gifts. The first-transaction fixture retains three unrelated cart items totaling Rp999.999; those do not qualify toward the promotion.

## Reusable composition

| Part | Implementation | Reuse and responsibilities |
|---|---|---|
| Page shell | `activity_paragon_fair_demo.xml` | ConstraintLayout toolbar, scrolling body, overlay summary and footer; system-bar insets come from the sample Activity base |
| Expanded header | `item_fair_header.xml` | Banner ratio, quota/fair badges, gift preview, title and terms; content height follows text |
| Reward meter | `view_fair_progress.xml` | Shared expanded/compact presentation; existing `LinearProgressBar`, external milestone badge, status message, optional gift action |
| Product section | `item_fair_section.xml` | Count and selected two/three-column preference |
| Product tile | `item_fair_product.xml` | Square image, existing `StepperButton`, `KlikTextBadge` highlight, price/discount/promotion and reward strip |
| Local options sheet | `sheet_fair_options.xml`, `item_fair_option.xml` | Existing `BottomSheetTray`; bounded, scrollable options with optional images and selected states |
| State | `PromoDemoState.kt` | Integer rupiah totals, qualifying subtotal, earned/selected gifts, quantity bounds and availability; independent of animation |
| Adapters | Activity's `BaseAdapter` instances in `ConcatAdapter` | Detail header, search scope, section, products, empty/error and loading share one RecyclerView; non-product rows span all columns |

This is composition in the sample `app` module. No production `uikit` component implementation is changed. The app explicitly depends on [RecyclerView 1.4.0](https://developer.android.com/jetpack/androidx/releases/recyclerview#1.4.0), providing ConcatAdapter and restoration policies.

## Click, scroll and long press

| Element | Click/tap | Scroll | Long press / accessibility action |
|---|---|---|---|
| Plus/minus | Update the current state immediately; clamp 0–99; update totals and eligibility | Remain anchored to each product image | No hold-to-repeat; avoids accidental bulk quantity changes |
| Product | Show local inspection sheet, with add/remove actions when available | Recycled by the grid; image remains square | Same inspection sheet, with ordinary tap alternative |
| Gift preview / Lihat | Inspect available gifts; select only when eligibility remains | Preview scrolls away; compact Lihat remains available | Preview opens the same selector |
| Progress | Reflect qualifying spend and selected gifts | Compact form appears after the preview passes the top boundary, with 4 dp hysteresis | No separate gesture |
| Terms/info | Show promo details and fixture period | Follow the expanded header | No separate gesture |
| Sort/filter | Local option sheet; preserve quantities/claims while updating visible products | Floating above cart | No separate gesture |
| Column controls | Choose two/three columns; preserve model state | Scroll position managed by RecyclerView | No separate gesture |
| Cart | Local summary, product inspection and available gift action | Anchored above system navigation; list reserves footer height | No separate gesture |
| Toolbar title | Normal heading | Remains visible | Scenario selector, also exposed as an accessibility action |
| Back/search/share | Back navigation; promotion search entry; Android share chooser | Remain in toolbar | No separate gesture |

## Motion contract

| Change | Motion | Constraint |
|---|---|---|
| Quantity 0 ↔ positive | Existing stepper width 32 ↔ 90 dp, 300 ms; internal fades 150 ms | Reuses kit animation and unchanged hit regions |
| Qualifying progress | 450 ms, cubic easing `(0.2, 0.8, 0.2, 1)` | Cancel/retarget from displayed value, including decreases; a single Activity animator feeds both meters |
| Status message | 160 ms fade with 4 dp translation | Latest state replaces text immediately |
| Compact summary | 160 ms entrance, 120 ms exit; 4 dp entrance translation | No repeated reveal animation while state remains unchanged |
| Cart first appearance | 240 ms fade/16 dp translation | Only when cart changes from empty to nonempty |
| Cart feedback | Icon scale 1 → 1.12 → 1, 100/120 ms | Previous pulse is cancelled |
| New repeat milestone | Scale 1 → 1.12 → 1, 120/140 ms | Only on newly earned gift count |
| Sheets | Existing BottomSheetTray transitions | System animator scale disables custom motion |

Reduced motion follows Android's system animator-duration setting. It does not add an independent in-app preference. Progress and messages update immediately when motion is disabled. Framework animators handle the stepper's disabled-animation setting.

## Portrait layout constraints

- ConstraintLayout is the XML baseline throughout. Widths use start/end constraints and 0 dp match constraints; text-led sections use wrap content.
- Banner retains 360:128 ratio. Product art retains 1:1 ratio. Header side margins are 16 dp; grid margins/gutters produce 162 dp tiles at the 360 dp baseline.
- Default presentation uses two columns; the display toggle offers three. Promotion tags constrain their width to the tile; barriers allow wrapped discount content to grow without overlapping the following tag.
- Above 140% font scale, products use one column, product names can wrap fully, badges stack and term values appear below their labels. Text line heights scale in sp.
- Control rectangles are preserved: toolbar icons 32 dp, display icons 16 dp, stepper 32 dp height, compact Lihat 76 × 32 dp, filter/sort pill 194 × 40 dp, cart 40 dp height. No TouchDelegate or invisible hit-area expansion is introduced.
- To preserve these rectangles at large text, footer controls display icons and the cart displays its total; their complete descriptions remain available to accessibility services. Small milestone, overflow and quantity numerals retain compact visual sizing. The toolbar title may ellipsize.
- Expanded summary height may grow with status text; compact summary height may also grow with larger text. Both use the same state.
- The floating footer can overlap visible content while scrolling, matching the reference. Bottom list padding lets the final item scroll fully above it.
- This prototype targets portrait phone layouts. “Any portrait device” is a responsive implementation intent, not a claim of verification on every size, Android version, foldable or tablet.

## Accessibility and fidelity limits

Labels mean TalkBack/content descriptions, not visible tooltips. Plus/minus describe product and current quantity. Cart describes count and total. Headings are marked; product cards and options expose concise descriptions; redundant adapter containers/decorative images are excluded. Progress exposes qualifying spend and claims, and announces eligibility changes. Inactive promo controls cannot change quantities or claim gifts.

The requested small touch areas are preserved. This means the demo does not satisfy a blanket 48 dp minimum target requirement; it should not be described as fully accessibility certified. Figma's colors and faded unavailable-product treatment are preserved; contrast compliance has not been independently certified. A real TalkBack/Switch Access pass remains necessary before production acceptance.

Banner, product art and reward illustrations come from the supplied Figma file. Vector icons were rasterized from original exports; the composed point/stamp illustrations were exported from their original components. Existing matching kit icons/components are reused. The five-gift preview includes the reference's +2 indicator, but only the three identifiable gift products are selectable in this fixture.

The prototype uses local, Indonesian fixture content, eight product entries with repeated artwork, identical Rp50.000 prices, a historical 2025 period from the reference, and no backend. Search now uses the [promotion search flow](PromoSearchDemo.md) with native editing and local catalog matching; cart remains a summary without checkout. Localization, server validation, pagination, real quota updates, gift inventory and production navigation are follow-up work. Search includes a deterministic local failure/retry fixture. Open sheets are transient presentation; saved Activity state restores the shopping model, filter, sort and column preference.

## Validation

Build and checks: `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` using Android Studio's bundled JDK. Four state tests cover one-time/repeatable rewards, invalidated claims, unavailable promos and unrelated cart spend. Lint reports warnings for local fixture strings, Indonesian quantity strings and resources inflated reflectively by BaseAdapter; passing lint is not an accessibility certification.

Final Gradle pass succeeded. The four state tests reported zero failures/errors/skips. `git diff --check` passed.

The original promo-detail validation used the Pixel 4 emulator (Android 10/API 29), with density overrides producing 360 dp and 320 dp portrait widths; the initial default-width pass was approximately 393 dp. Verified product add/expanded stepper, cart totals, threshold eligibility, gift selection, repeatable partial claims, compact summary on scroll, three-column alignment/tag wrapping, Kahf filter (four products), name sorting, quota-exhausted treatment and zero products with no stale cards/cart. Accessibility labels were inspected through the UI hierarchy. Configuration recreation retained the demo and column preference; 200% text and animator scale zero were visually reviewed. That initial pass did not test process-death restoration, live TalkBack/Switch Access, frame-time measurements or newer Android/inset variations. The later [search validation](PromoSearchDemo.md#validation) includes background process recreation and additional portrait widths.

Review captures:

- [360 dp initial state](images/paragon-fair/start-360.png)
- [360 dp three-column products and compact summary](images/paragon-fair/three-columns-360.png)
- [320 dp, 200% text, expanded header](images/paragon-fair/large-text-320.png)

The emulator's density, font scale and animator scale were restored after validation. The Activity remains registered as non-exported and is reached through the sample app menu. The PoC implementation is on the `experimental/paragon-fair-demo` branch.
