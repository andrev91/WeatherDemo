package com.example.adventure.util

import androidx.annotation.DrawableRes
import com.example.adventure.R

object WeatherIconMapper {

    /**
     * Maps AccuWeather Icon ID to a local mipmap resource ID
     *
     * @param weatherIconId AccuWeather Icon ID
     * @return Local mipmap resource ID
     */
    @DrawableRes
    fun getIconResource(weatherIconId : Int) : Int {
        return when (weatherIconId) {
            1 -> R.mipmap.sunny_white_background
            2 -> R.mipmap.rainy_white_background
            else -> 0
        }
    }

}