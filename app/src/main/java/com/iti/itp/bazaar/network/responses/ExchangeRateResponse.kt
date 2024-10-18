package com.iti.itp.bazaar.network.responses

data class ExchangeRateResponse(
    val result: String,
    val documentation: String,
    val terms_of_use: String,
    val time_last_update_unix: Long,
    val time_last_update_utc: String,
    val time_next_update_unix: Long,
    val time_next_update_utc: String,
    val base_code: String,
    val target_code: String,
    val conversion_rate: Double // da hdrhabo fel 3omla elli 3andi (EGP , USD)
)
