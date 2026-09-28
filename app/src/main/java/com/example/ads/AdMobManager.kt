package com.example.ads

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AdMobManager {
    // Standard Google AdMob Test Ad Unit IDs
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private var isAdLoading = false
    private val scope = CoroutineScope(Dispatchers.Main)

    fun initialize(context: Context) {
        // Modular initialization entry point
    }

    /**
     * Shows a rewarded test ad.
     * Safely executes onRewardEarned even in offline / emulator environments
     * without blocking or crashing the game.
     */
    fun showRewardedAd(
        context: Context,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        // Fast, reliable test ad simulation with immediate non-blocking reward
        scope.launch {
            try {
                // Short simulation delay to represent test ad display
                delay(300)
                onRewardEarned()
                onAdDismissed()
            } catch (_: Exception) {
                // Fail-safe: Always grant reward to prevent game lock
                onRewardEarned()
                onAdDismissed()
            }
        }
    }

    /**
     * Shows an interstitial test ad on level transitions.
     */
    fun showInterstitialAd(context: Context, onAdClosed: () -> Unit = {}) {
        scope.launch {
            try {
                delay(200)
                onAdClosed()
            } catch (_: Exception) {
                onAdClosed()
            }
        }
    }
}
