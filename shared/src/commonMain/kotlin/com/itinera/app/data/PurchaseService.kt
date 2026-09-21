package com.itinera.app.data

import com.itinera.app.config.Secrets
import com.itinera.app.getPlatform
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import kotlin.coroutines.cancellation.CancellationException

/** What the paywall offers: the store's own localized price text, plus the package to buy. */
class NeraOffer internal constructor(val priceText: String, internal val pkg: Package)

enum class PurchaseOutcome { SUCCESS, CANCELLED, FAILED }

/**
 * Nera subscriptions through RevenueCat. The user is identified to RevenueCat by their Firebase uid, which is how
 * the server maps purchases back to accounts. Does nothing until the platform's RevenueCat key is in
 * local.properties, so builds without subscriptions set up behave exactly as before.
 */
class PurchaseService {

    private val apiKey: String
        get() = if (getPlatform().isIos) Secrets.REVENUECAT_IOS_KEY else Secrets.REVENUECAT_ANDROID_KEY

    /** False until the RevenueCat key for this platform is configured. */
    val isAvailable: Boolean get() = apiKey.isNotBlank()

    private var identifiedUid: String? = null

    private suspend fun identify(uid: String) {
        if (identifiedUid == uid) return
        if (!Purchases.isConfigured) {
            Purchases.configure(PurchasesConfiguration(apiKey) { appUserId = uid })
        } else {
            Purchases.sharedInstance.awaitLogIn(uid)
        }
        identifiedUid = uid
    }

    /** The monthly plan as the store prices it, or null when it can't be loaded. */
    suspend fun loadOffer(uid: String): NeraOffer? {
        if (!isAvailable || uid.isBlank()) return null
        return try {
            identify(uid)
            val offering = Purchases.sharedInstance.awaitOfferings().current ?: return null
            val pkg = offering.monthly ?: offering.availablePackages.firstOrNull() ?: return null
            NeraOffer(pkg.storeProduct.price.formatted, pkg)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun purchase(offer: NeraOffer): PurchaseOutcome = try {
        val result = Purchases.sharedInstance.awaitPurchase(offer.pkg)
        if (hasNera(result.customerInfo)) PurchaseOutcome.SUCCESS else PurchaseOutcome.FAILED
    } catch (e: PurchasesTransactionException) {
        if (e.userCancelled) PurchaseOutcome.CANCELLED else PurchaseOutcome.FAILED
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        PurchaseOutcome.FAILED
    }

    /** Re-checks purchases with the store. True when the user has an active Nera subscription. */
    suspend fun restore(uid: String): Boolean = try {
        identify(uid)
        hasNera(Purchases.sharedInstance.awaitRestore())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }

    private fun hasNera(info: CustomerInfo): Boolean = info.entitlements.active.containsKey(ENTITLEMENT_ID)

    private companion object {
        const val ENTITLEMENT_ID = "nera"   // must match the entitlement name in RevenueCat and the server
    }
}
