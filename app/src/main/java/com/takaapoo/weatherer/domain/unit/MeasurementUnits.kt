package com.takaapoo.weatherer.domain.unit

import androidx.annotation.StringRes
import com.takaapoo.weatherer.R

enum class Temperature(
    @StringRes val description: Int,
    val title: String
) {
    CELSIUS(R.string.celsius, "C"),
    FAHRENHEIT(R.string.fahrenheit, "F"),
    KELVIN(R.string.kelvin, "K")
}

enum class Length(
    @StringRes val description: Int,
    val title: String
) {
    SI(R.string.si, "SI"),
    IMPERIAL(R.string.imperial, "Imperial")
}

enum class Pressure(
    @StringRes val description: Int,
    val title: String
) {
    Pa(R.string.pa, "Pa"),
    BAR(R.string.bar, "bar"),
    PSI(R.string.psi, "psi"),
    ATM(R.string.atm, "atm")
}

enum class Speed(
    @StringRes val description: Int,
    val title: String
) {
    KMPH(R.string.kph, "km/h"),
    MPH(R.string.mph, "mph"),
    MPS(R.string.mps, "m/s")
}