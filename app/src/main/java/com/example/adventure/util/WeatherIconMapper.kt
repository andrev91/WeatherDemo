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
    fun getIconResource(weatherIconId : Int, isDaytime : Boolean) : Int {
        if (isDaytime) {
            return when (weatherIconId) {
                1 -> R.mipmap.sunny_white_background
                2 -> R.mipmap.sunny_white_background
                3 -> R.mipmap.sunny_white_background
                //4 -> R.mipmap.cloudy
                //5 -> R.mipmap.hazy_sunshine
                //6 -> R.mipmap.cloudy
                //7 -> R.mipmap.cloudy
                //8 -> R.mipmap.overcast
                else -> 0
            }
        } else {
            return when (weatherIconId) {
                //1 -> R.mipmap.clear
                //2 -> R.mipmap.clear
                //3 -> R.mipmap.cloudy
                //4 -> R.mipmap.cloudy
                //5 -> R.mipmap.hazy_moonlight
                //6 -> R.mipmap.cloudy
                //7 -> R.mipmap.cloudy
                //8 -> R.mipmap.overcast
                else -> 0
            }
        }
    }

}