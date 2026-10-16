# Promotion search Activity prototype

## Run and demonstrate

Branch: `experimental/paragon-fair-demo`. Build the `app` debug variant and open **Paragon Fair · Promo Detail Demo** from the sample app menu.

Long press **Detail Promo** (also exposed as the accessibility action **Pilih skenario demo**) and choose:

- **Search · Hadiah Gratis · Rp50.000 di keranjang**: search `Kahf`; two products appear. Add Cooling Power once: two cart items, Rp100.000, one eligible gift. Back to detail retains both quantities.
- **Search · Tebus Murah · Rp50.000 di keranjang**: search `Goreng`; four products appear. All fixture products qualify toward repeatable Rp50.000 intervals.
- **Search · simulasi gagal sekali lalu coba lagi**: submit `Kahf`; a separate connection-failure presentation appears. **Coba Lagi** succeeds with the same query and cart.

In either promotion, submit `Kurma` for an empty result without a website banner. Tap **Ganti Kata Kunci** to restore the query and keyboard. Clear and submit `Rokok` for the website-banner variant. The banner launches `https://www.klikindomaret.com` without inventing query parameters.

The second suggestion opens a local **Pencarian Klik Indomaret** handoff showing the retained query and a website action. A production general-search destination was not supplied in this section.

## Reference and fidelity decisions

Source: [PPP Fair Detail – Search, 54442:186207](https://www.figma.com/design/Wd140rqS27zbhDNDSpy9PB/0.2-Klik-Indomaret---Promotion--Grocery-?node-id=54442-186207).

Inspected the four found/empty flows, individual editing, scope, result, added-product and empty/banner frames. Primary references: `54442:194283`, `54442:194304`, `54442:194331`, `54442:194394`, `54442:194408`, `54442:183499` and `54442:183520`.

The Figma service returned no authored motion nodes. Timings, loading/error states and the general-search handoff are prototype proposals. Actual keyboard, status/navigation bars and browser presentation belong to Android and vary by device.

The website banner is explicitly enabled for the local `Rokok` fixture. It is not a two-failure counter or a production eligibility rule. The empty-state recommendation copy is preserved; no recommendation products are invented.

Tebus Murah source inconsistencies are retained/documented rather than treated as business specifications:

- The Aceh card shows Rp10.000, a crossed-out Rp6.400 and a 50% label. These are literal visual fixture values; the label is not calculated from these inconsistent prices.
- Its initial search frame shows a Rp50.000 cart and asks for another Rp50.000 despite the declared repeatable Rp50.000 threshold. This prototype calculates one eligible redemption at Rp50.000 and shows the earned interval. Its meter/message therefore differ from that frame.
- The seed cart uses an identifiable Emina asset already in the demo. The seed item is outside the Goreng results. Redemption selection simulates eligibility/selection; charging Rp5.000 and checkout are not implemented.
- The Tebus detail return uses a minimal existing detail composition, hiding the Paragon hero/gift preview. This search implementation does not claim to reproduce an additional Tebus detail frame.

## Reusable composition

| Part | Location / responsibility |
|---|---|
| Search toolbar | Existing `activity_paragon_fair_demo.xml`; kit `SearchBar`, back action and native IME |
| Scope suggestions | `item_fair_search_scope.xml`; two full-width `BaseAdapter` rows |
| Search state | `PromoSearchState.kt`; separate draft/submitted queries, modes and request generation |
| Shopping state | Existing `PromoDemoState.kt`; quantities, totals, qualification and benefit claims |
| Result header | Existing `item_fair_section.xml`; promotion description, count and selected columns |
| Tiles | Existing `item_fair_product.xml`; square image, Stepper, contextual highlights and badges |
| Benefit summary | Existing `view_fair_progress.xml`, pinned throughout submitted search |
| Empty / filtered / error | `item_fair_search_empty.xml`; native text and button, optional original illustration/banner |
| Loading | `item_fair_search_loading.xml`; brief local pending state, separate from zero results |
| Footer | Existing filter/sort and cart composition; hidden while editing |

All compositions are local to `app`; no production `uikit` API or implementation changes were needed. ViewBinding configures the kit SearchBar's existing internal icon views for accessibility. No new dependencies were added for search.

## State and navigation contract

Modes: `DETAIL → EDITING → LOADING → RESULTS / EMPTY / ERROR`.

- Text edits update only the draft. Submit normalizes whitespace; matching is case-insensitive and supports multiple words.
- Search returns one catalog entry per name because the original detail fixture repeats products. Stable product IDs remain the keys for quantity changes and list diffs.
- Cart totals and qualification always use the full shopping model, including products outside the query. Filtering/sorting never changes quantities.
- Local latency is 220 ms. A new edit, submission, scenario or exit cancels pending work; generation checks reject stale completion.
- IME Search and the first scope row submit within the promotion. Empty submission retains editing.
- Clear empties the draft and keeps focus/keyboard. Editing hides benefit/footer controls and removes those regions from traversal.
- System Back hides the keyboard first; after editing an existing submission, the next Back restores that submission. Toolbar Back returns directly to detail and restores its scroll position.
- Result sort/filter/columns reuse the existing sheets. A filter that hides every match has its own **Hapus Filter** recovery.
- Draft, submitted query, mode, banner flag, promotion, threshold, quantities, claims, filters/sort and column preference are saved in the Activity Bundle. Keyboard focus is restored only for editing.
- Empty, failure and loading states do not show stale product cards. Empty/failure hide filter/sort while retaining the cart.

## Click, scroll and long tap

| Element | Click | Scroll / long tap |
|---|---|---|
| Query | Enter editing; native input and clear | Native long-tap selection/copy/paste |
| Scope row | Promotion submit or general handoff | Scrolls above IME; long tap has the same outcome as tap |
| Product | Existing inspection sheet | Scrolls with grid; long tap opens the same sheet |
| Stepper | Immediate shared quantity update; bounded by stock | No hold-to-repeat |
| Ganti Kata Kunci | Retain submitted query and request IME | Scrolls with empty content; no additional long-tap action |
| Website banner | Browser intent to the displayed website | Scrolls with empty content |
| Benefit / cart | Existing local benefit/cart sheets | Pinned in submitted search |
| Sort/filter/columns | Update presentation; preserve shopping model | Footer remains anchored; nearest grid position restored |

## Motion and layout constraints

- Search content crossfades for 160 ms when its mode changes. Suggestions are updated through list diffs without per-character entrance cascades.
- Quantity uses the kit's existing 300 ms Stepper transition. Shopping state changes immediately.
- Benefit progress uses the existing 450 ms animator, retargeted from its displayed value; message feedback is 160 ms with 4 dp translation.
- Cart entrance remains 240 ms / 16 dp; quantity feedback uses the existing small icon pulse.
- Sheet motion comes from `BottomSheetTray`; keyboard motion comes from Android. No fabricated keyboard artwork is used.
- System animator duration zero disables custom motion and indeterminate loading imagery. State and labels remain usable.

ConstraintLayout remains the baseline. Search/empty margins are 16 dp, suggestion icons 24 dp, empty illustration 94 dp, website banner ratio 328:118, product art 1:1 and existing grid gutters are preserved. Widths use match constraints; text-led content wraps. Insets use the greater of IME/system bottom insets and preserve side/top system insets; the Activity uses `adjustResize`.

The toolbar/input and quantity controls remain 32 dp, display icons 16 dp, empty recovery button 28 dp, benefit action 76 × 32 dp, filter/sort pill 194 × 40 dp and cart 40 dp. No TouchDelegate or invisible target expansion is used.

Above 140% text scale, products become one column, product names wrap, empty-state text stacks below its illustration, and the fixed footer uses its existing compact presentation. The 28 dp recovery control retains its full label at a compact 12 dp visual text size to avoid clipping while preserving the approved rectangle. Accessibility still reads the complete label.

## Accessibility

Labels are content descriptions/TalkBack semantics, not tooltips. Clear is **Hapus kata kunci**; scope rows include query and destination; native EditText editing remains available. Result counts are announced on completion. Benefit announcements occur on eligibility/claim changes, not every animation frame. Product quantities include product and current count. Headings are marked, decorative images excluded, recovery/banner/clear actions expose button roles, and selected column controls expose selection.

Requested small targets are preserved; this is not a claim of blanket minimum-target compliance. Live TalkBack/Switch Access and measured animation frame-time profiling remain production review work.

## Asset audit

| Asset | Source / rendered slot |
|---|---|
| `fair_search_empty.png` | Original Figma fill from `54442:194409`; 94 × 94 dp, fitCenter |
| `fair_search_website.png` | Export of banner node `54442:194417`; 328:118 constrained ratio |
| `fair_search_bundling.png`, `fair_search_percent.png` | Original Figma SVG assets, rasterized at four-times density; 12 dp highlight slots |
| Kahf and noodle artwork | Existing original exported product assets; square image slots |
| Search/cart/grid/filter/gift/points/stamps | Existing kit/demo assets in their existing slots |

No temporary Figma URLs remain in the search code. The website banner is a bitmap with embedded text; its accessible destination label is separate native content.

## Validation

Build gate: `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`. Six new state tests cover exact catalog matching/shared cart, stale request rejection, draft/submitted separation, error/retry, explicit banner policy and repeatable redemption qualification. Existing shopping-state tests remain included. Lint warnings for demo strings and reflective adapter resources remain; no lint error is accepted.

Final build/check pass on 1 October 2026 succeeded: 14 app unit tests, zero failures/errors/skips; lint reported 0 errors and 293 warnings. `git diff --check` passed.

Manual verification used only the Pixel 4 API 29 emulator:

| Portrait viewport | Text / motion | Verified |
|---|---|---|
| 360 dp | 100%, motion enabled | Both promotions; suggestions/native IME; exact two/four matches; quantity/cart/eligibility; empty variants; retry; sort/filter; two/three columns |
| 320 dp | 200%, animator scale zero | Wrapped/stacked empty state, one-column products, usable native editing, new submission resets to the result header |
| Approximately 393 dp | 100% | Result grid, query/cart restoration across configuration recreation |
| 600 dp | 100%, animator scale zero | Two-column portrait layout, retained quantities/cart/eligibility |

Additional checks: rapid repeated additions retain every quantity change; the seed cart remains present outside the query; global scope retains the query in its local handoff; filter-empty recovery restores matches; Back restores detail with the shopping model intact. Website tap launched the browser intent; loading the website itself was not verified because Chrome displayed its first-run screen.

Background process recreation was verified after Home and `am kill`: the process ID changed, and the Kahf query, two results, two cart items/Rp100.000 and gift eligibility restored. New-query scrolling waits for the asynchronous child-adapter diffs to commit before resetting to the header.

Review captures:

- [Scope suggestions, 360 dp](images/promo-search/suggestions-360.png)
- [Gift results, 360 dp](images/promo-search/gift-results-360.png)
- [Gift eligibility, 360 dp](images/promo-search/gift-eligible-360.png)
- [Empty without banner, 360 dp](images/promo-search/empty-kurma-360.png)
- [Website empty state, 360 dp](images/promo-search/empty-website-360.png)
- [Redemption results, 360 dp](images/promo-search/redemption-results-360.png)
- [Failure/retry, 360 dp](images/promo-search/retry-360.png)
- [Empty, 320 dp / 200% text](images/promo-search/empty-320-large-text.png)
- [Results, 320 dp / 200% text](images/promo-search/results-320-large-text.png)
- [Results, approximately 393 dp](images/promo-search/results-393.png)
- [Results, 600 dp portrait](images/promo-search/results-600.png)

Accessibility descriptions/roles were inspected in the UI hierarchy; live TalkBack/Switch Access, newer Android/gesture-inset behavior and measured frame-time profiling were not tested. The emulator's size, density, font and animator settings are restored after QA. The Activity is non-exported and opens through the sample app menu. The PoC implementation is on the `experimental/paragon-fair-demo` branch.
