package com.vamsi.worldcountriesinformation.domain.preferences

import java.util.concurrent.TimeUnit

/** How long cached country data counts as fresh before the app checks the sources again. */
enum class RefreshInterval(val millis: Long) {
    DAILY(TimeUnit.DAYS.toMillis(1)),
    WEEKLY(TimeUnit.DAYS.toMillis(7)),
    MONTHLY(TimeUnit.DAYS.toMillis(30)),
}
