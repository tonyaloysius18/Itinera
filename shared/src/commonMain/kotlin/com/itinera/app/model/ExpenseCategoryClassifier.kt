package com.itinera.app.model

private data class CategoryRule(val category: ExpenseCategory, val pattern: Regex)

private fun rule(category: ExpenseCategory, keyword: String) =
    CategoryRule(category, Regex("\\b" + Regex.escape(keyword) + "\\b"))

/**
 * Best-effort keyword rules so an expense gets a sensible category without the
 * user having to pick one by hand (there's no category picker in AddExpenseScreen).
 * Order matters: the first matching rule wins, so put more telling words first
 * within a category if you add more.
 */
private val CATEGORY_RULES: List<CategoryRule> =
    listOf(
        "breakfast", "lunch", "dinner", "brunch", "meal", "food", "restaurant", "cafe",
        "coffee", "snack", "snacks", "grocery", "groceries", "supermarket", "bar", "drinks",
        "drink", "pizza", "burger", "sushi", "taco", "dessert", "bakery", "pub", "wine", "beer",
        "buffet", "takeout", "diner", "juice", "water", "cake"
    ).map { rule(ExpenseCategory.FOOD, it) } +
        listOf(
            "hotel", "hostel", "airbnb", "motel", "accommodation", "resort", "lodge", "lodging",
            "room", "stay", "guesthouse", "bnb", "camping", "campsite", "apartment",
        ).map { rule(ExpenseCategory.ACCOMMODATION, it) } +
        listOf(
            "car rental", "rental car", "bike rental", "scooter rental",
            "taxi", "uber", "lyft", "flight", "flights", "airfare", "train", "bus", "metro",
            "subway", "tram", "ferry", "boat", "cab", "gas", "fuel", "parking", "toll",
            "transport", "transportation", "ride", "airport", "transfer", "scooter",
        ).map { rule(ExpenseCategory.TRANSPORT, it) } +
        listOf(
            "ticket", "tickets", "tour", "museum", "activity", "activities", "excursion",
            "entrance", "entry", "show", "concert", "hike", "hiking", "cruise", "spa", "zoo",
            "aquarium", "attraction", "lesson", "adventure", "safari", "pass"
        ).map { rule(ExpenseCategory.ACTIVITIES, it) } +
        listOf(
            "shopping", "souvenir", "souvenirs", "gift", "gifts", "clothes", "clothing", "shoes",
            "mall", "store", "boutique", "shop", "market",
        ).map { rule(ExpenseCategory.SHOPPING, it) }

/**
 * Guesses an [ExpenseCategory] from a free-text expense description, e.g.
 * "Breakfast at hotel" -> FOOD. Falls back to [ExpenseCategory.OTHER] when
 * nothing matches.
 */
fun classifyExpenseCategory(description: String): ExpenseCategory {
    if (description.isBlank()) return ExpenseCategory.OTHER
    val lower = description.lowercase()
    return CATEGORY_RULES.firstOrNull { it.pattern.containsMatchIn(lower) }?.category
        ?: ExpenseCategory.OTHER
}

/**
 * The category to actually display/group by: the stored [Expense.category] if
 * it's been set to something specific, otherwise a best-effort guess from the
 * description. Covers both legacy data and new expenses, since neither ever
 * gets a category from the user directly.
 */
val Expense.effectiveCategory: ExpenseCategory
    get() = if (category != ExpenseCategory.OTHER) category else classifyExpenseCategory(description)
