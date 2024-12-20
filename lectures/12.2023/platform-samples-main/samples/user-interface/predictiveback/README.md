# Predictive Back Samples

Shows different types of predictive back animations, including:

+ Back-to-home
+ Cross-activity
+ Custom cross-activity
+ Cross-fragment animation
+ Custom Progress API animation

## Custom cross-activity

In general, rely on the default cross-activity animation; however, if required use
`overrideActivityTransition` instead of `overridePendingTransition`. Although animation resources are
expected for `overrideActivityTransition`, we strongly recommend to stop using animation and to
instead use animator and androidx transitions for most use cases. For more details see the
[developer documentation](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture).

```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        "..."

        overrideActivityTransition(
            OVERRIDE_TRANSITION_OPEN,
            android.R.anim.fade_in,
            0
        )
    
        overrideActivityTransition(
            OVERRIDE_TRANSITION_CLOSE,
            0,
            android.R.anim.fade_out
        )
    }
```

## Cross-fragment animation

Example code uses navigation component default animations.

```xml
<action
    android:id="..."
    app:destination="..."
    app:enterAnim="@animator/nav_default_enter_anim"
    app:exitAnim="@animator/nav_default_exit_anim"
    app:popEnterAnim="@animator/nav_default_pop_enter_anim"
    app:popExitAnim="@animator/nav_default_pop_exit_anim" />
```

## Custom Progress API animation

The following example using the Progress API follows the
[Predictive Back Design Guidance](https://developer.android.com/design/ui/mobile/guides/patterns/predictive-back).

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    "..."

    val windowWidth = requireActivity().windowManager.currentWindowMetrics.bounds.width()
    val maxXShift = windowWidth / 20

    val predictiveBackCallback = object: OnBackPressedCallback(enabled=false) {

        override fun handleOnBackProgressed(backEvent: BackEventCompat) {
            when (backEvent.swipeEdge) {
                BackEventCompat.EDGE_LEFT ->
                    binding.box.translationX = backEvent.progress * maxXShift
                BackEventCompat.EDGE_RIGHT ->
                    binding.box.translationX = -(backEvent.progress * maxXShift)
            }
            binding.box.scaleX = 1F - (0.1F * backEvent.progress)
            binding.box.scaleY = 1F - (0.1F * backEvent.progress)
        }

        override fun handleOnBackPressed() {
            // your back handling logic
        }

        override fun handleOnBackCancelled() {
            binding.box.scaleX = 1F
            binding.box.scaleY = 1F
            binding.box.translationX = 0F
        }
    }
    
    requireActivity().onBackPressedDispatcher.addCallback(
        this.viewLifecycleOwner,
        predictiveBackCallback
    )
}
```

## Custom AndroidX Transition
For more details see the
[developer documentation](https://developer.android.com/about/versions/14/features/predictive-back#androidx-transitions).

```kotlin
class MyFragment : Fragment() {

    val transitionSet = TransitionSet().apply {
        addTransition(Fade(Fade.MODE_OUT))
        addTransition(ChangeBounds())
        addTransition(Fade(Fade.MODE_IN))
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(enabled = false) {

            var controller: TransitionSeekController? = null

            override fun handleOnBackStarted(backEvent: BackEvent) {
                // Create the transition
                controller = TransitionManager.controlDelayedTransition(
                    // textContainer is a FrameLayout containing the shortText and longText TextViews
                    binding.textContainer,
                    transitionSet
                )
                changeTextVisibility(ShowText.SHORT)
            }

            override fun handleOnBackProgressed(backEvent: BackEvent) {
                // Play the transition as the user swipes back
                if (controller?.isReady == true) {
                    controller?.currentFraction = backEvent.progress
                }
            }

            override fun handleOnBackPressed() {
                // Finish playing the transition when the user commits back
                controller?.animateToEnd()
                this.isEnabled = false
            }

            override fun handleOnBackCancelled() {
                // If the user cancels the back gesture, reset the state
                transition(ShowText.LONG)
            }
        }

        binding.shortText.setOnClickListener {
            transition(ShowText.LONG)
            callback.isEnabled = true
        }

        this.requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    private fun transition(showText: ShowText) {
        TransitionManager.beginDelayedTransition(
            binding.textContainer,
            transitionSet
        )
        changeTextVisibility(showText)
    }

    enum class ShowText { SHORT, LONG }
    private fun changeTextVisibility(showText: ShowText) {
        when (showText) {
            ShowText.SHORT -> {
                binding.shortText.isVisible = true
                binding.longText.isVisible = false
            }
            ShowText.LONG -> {
                binding.shortText.isVisible = false
                binding.longText.isVisible = true
            }
        }
    }
}
```

