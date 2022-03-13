package ru.cities.game.util

import android.os.Build
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity

fun setStatusBar(
    activity: FragmentActivity,
    appearance: Int,
    statusBarBackgroundColor: Int,
    isAppearanceLight: Boolean
) {
    val fragmentWindow = activity.window
    val decorFragmentWindow = fragmentWindow.decorView
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        fragmentWindow.statusBarColor = activity.getColor(statusBarBackgroundColor)
    WindowInsetsControllerCompat(
        fragmentWindow,
        decorFragmentWindow
    ).isAppearanceLightStatusBars = isAppearanceLight
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        decorFragmentWindow.windowInsetsController?.setSystemBarsAppearance(
            appearance,
            APPEARANCE_LIGHT_STATUS_BARS
        )
}
