package com.example.model

import androidx.compose.ui.graphics.Color

enum class GameColor(
    val displayName: String,
    val primaryColor: Color,
    val lightColor: Color,
    val darkColor: Color,
    val metalSpecular: Color
) {
    RED(
        displayName = "Red",
        primaryColor = Color(0xFFE53935),
        lightColor = Color(0xFFFF6F60),
        darkColor = Color(0xFFAB000D),
        metalSpecular = Color(0xFFFFCDD2)
    ),
    YELLOW(
        displayName = "Yellow",
        primaryColor = Color(0xFFFFB300),
        lightColor = Color(0xFFFFE54C),
        darkColor = Color(0xFFC68400),
        metalSpecular = Color(0xFFFFF9C4)
    ),
    BLUE(
        displayName = "Blue",
        primaryColor = Color(0xFF1E88E5),
        lightColor = Color(0xFF6AB7FF),
        darkColor = Color(0xFF005CB2),
        metalSpecular = Color(0xFFBBDEFB)
    ),
    GREEN(
        displayName = "Green",
        primaryColor = Color(0xFF43A047),
        lightColor = Color(0xFF76D275),
        darkColor = Color(0xFF00701A),
        metalSpecular = Color(0xFFC8E6C9)
    ),
    ORANGE(
        displayName = "Orange",
        primaryColor = Color(0xFFFB8C00),
        lightColor = Color(0xFFFFBD45),
        darkColor = Color(0xFFC25E00),
        metalSpecular = Color(0xFFFFE0B2)
    ),
    PURPLE(
        displayName = "Purple",
        primaryColor = Color(0xFF8E24AA),
        lightColor = Color(0xFFC158DC),
        darkColor = Color(0xFF5C007A),
        metalSpecular = Color(0xFFE1BEE7)
    ),
    PINK(
        displayName = "Pink",
        primaryColor = Color(0xFFD81B60),
        lightColor = Color(0xFFFF5C8D),
        darkColor = Color(0xFFA00037),
        metalSpecular = Color(0xFFF8BBD0)
    ),
    CYAN(
        displayName = "Cyan",
        primaryColor = Color(0xFF00ACC1),
        lightColor = Color(0xFF5DDEF4),
        darkColor = Color(0xFF007C91),
        metalSpecular = Color(0xFFB2EBF2)
    ),
    TEAL(
        displayName = "Teal",
        primaryColor = Color(0xFF00897B),
        lightColor = Color(0xFF4EBAAA),
        darkColor = Color(0xFF005B4F),
        metalSpecular = Color(0xFFB2DFDB)
    ),
    MAGENTA(
        displayName = "Magenta",
        primaryColor = Color(0xFFC2185B),
        lightColor = Color(0xFFFA5788),
        darkColor = Color(0xFF8C0032),
        metalSpecular = Color(0xFFF48FB1)
    ),
    LIME(
        displayName = "Lime",
        primaryColor = Color(0xFF7CB342),
        lightColor = Color(0xFFAEE571),
        darkColor = Color(0xFF4B830D),
        metalSpecular = Color(0xFFDCEDC8)
    ),
    GOLD(
        displayName = "Gold",
        primaryColor = Color(0xFFF59E0B),
        lightColor = Color(0xFFFCD34D),
        darkColor = Color(0xFFB45309),
        metalSpecular = Color(0xFFFEF3C7)
    );

    companion object {
        fun fromIndex(index: Int): GameColor {
            val values = entries.toTypedArray()
            return values[index % values.size]
        }
    }
}
