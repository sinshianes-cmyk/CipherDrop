# Reader fix: pieces of a later page showing below the page you are reading

**Symptom:** reading page 9, a part of page 13 or 16 is visible at the bottom; scrolling up and back down
makes it disappear.

**Cause:** each page holder is recycled and reused as you scroll. Its `PageViewModel` load job could still
finish *after* the holder had been recycled/rebound, publishing `Loaded(<old page>)`. The holder then called
`ssiv.setImage(<old page>)`, and because the `Loading` state that follows never clears the image, the old
page stayed on screen in the new page's slot until the new page finished loading. Pages that are only
partly on screen (the one entering at the bottom) made it look like "a part of page 13".

**Fix**
* `PageViewModel`: every bind / retry / recycle bumps a generation; load results are published on the main
  thread and only if their generation is still current. The previous job is cancelled synchronously in
  `onBind` (it used to be cancelled from inside the new job, leaving a gap), and `onRecycle` cancels before
  it resets the state. The network-retry no longer fires for a page the holder has moved on from.
* `BasePageHolder.bind`: when the holder is given a different page, the image view is cleared first.

**Verified:** `tools/reader-race-repro` replays the scenario on a plain JVM. Original code publishes
`Loaded(page13)` into a holder that now shows page 10; the fixed code never does.
Not run on a device.
