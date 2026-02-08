package com.vamsi.worldcountriesinformation.core.common.testing

/**
 * Semantic test tags for UI testing.
 *
 * These tags are used with the testTag modifier in Compose components
 * to enable reliable element identification in UI tests.
 *
 * Usage in Composable:
 * ```
 * Text(
 *     text = "Countries",
 *     modifier = Modifier.testTag(TestTags.Countries.SCREEN)
 * )
 * ```
 *
 * Usage in Test:
 * ```
 * composeTestRule
 *     .onNodeWithTag(TestTags.Countries.SCREEN)
 *     .assertIsDisplayed()
 * ```
 */
object TestTags {

    // Countries Screen
    object Countries {
        const val SCREEN = "countries_screen"
        const val SEARCH_FIELD = "countries_search_field"
        const val SEARCH_CLEAR_BUTTON = "countries_search_clear"
        const val SEARCH_BACK_BUTTON = "countries_search_back"
        const val COUNTRIES_LIST = "countries_list"
        const val SETTINGS_BUTTON = "settings_button"
        const val CLEAR_FILTERS_BUTTON = "clear_filters_button"
        const val LOADING_INDICATOR = "countries_loading"
        const val ERROR_STATE = "countries_error"
        const val ERROR_RETRY_BUTTON = "countries_error_retry"
        const val EMPTY_STATE = "countries_empty"
        const val EMPTY_SEARCH_STATE = "countries_empty_search"
        const val SORT_BUTTON = "sort_button"
        const val SORT_MENU = "sort_menu"
        const val RECENTLY_VIEWED_SECTION = "recently_viewed_section"
        const val REGION_FILTERS = "region_filters"
        const val ALPHABET_INDEX = "alphabet_index"

        fun countryCard(code: String) = "country_card_$code"
        fun favoriteButton(code: String) = "favorite_button_$code"
        fun regionFilter(region: String) = "region_filter_$region"
        fun sortOption(order: String) = "sort_option_$order"
    }

    // Country Details Screen
    object CountryDetails {
        const val SCREEN = "country_details_screen"
        const val BACK_BUTTON = "details_back_button"
        const val COUNTRY_NAME = "country_name"
        const val COUNTRY_FLAG = "country_flag"
        const val SHARE_BUTTON = "share_button"
        const val FAVORITE_BUTTON = "details_favorite_button"
        const val REFRESH_BUTTON = "details_refresh_button"
        const val MAP_CARD = "map_card"
        const val OPEN_IN_MAPS_BUTTON = "open_in_maps_button"
        const val LOADING_INDICATOR = "details_loading"
        const val ERROR_STATE = "details_error"
        const val ERROR_RETRY_BUTTON = "details_error_retry"
        const val NEARBY_COUNTRIES_SECTION = "nearby_countries_section"
        const val COUNTRY_INFO_SECTION = "country_info_section"

        fun detailItem(label: String) = "detail_item_$label"
        fun nearbyCountryCard(code: String) = "nearby_country_$code"
    }

    // Settings Screen
    object Settings {
        const val SCREEN = "settings_screen"
        const val BACK_BUTTON = "settings_back_button"
        const val CACHE_POLICY_SECTION = "cache_policy_section"
        const val OFFLINE_MODE_SECTION = "offline_mode_section"
        const val OFFLINE_MODE_SWITCH = "offline_mode_switch"
        const val CACHE_STATS_SECTION = "cache_stats_section"
        const val CLEAR_CACHE_BUTTON = "clear_cache_button"
        const val CLEAR_CACHE_DIALOG = "clear_cache_dialog"
        const val CLEAR_CACHE_CONFIRM = "clear_cache_confirm"
        const val CLEAR_CACHE_CANCEL = "clear_cache_cancel"
        const val ABOUT_SECTION = "about_section"

        fun cachePolicyOption(policy: String) = "cache_policy_$policy"
    }
}
