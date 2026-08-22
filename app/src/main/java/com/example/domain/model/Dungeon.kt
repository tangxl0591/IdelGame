package com.example.domain.model

data class DungeonChapter(
    val id: Int,
    val name: String,
    val minLevel: Int,
    val maxLevel: Int,
    val description: String,
    val iconEmoji: String,
    val backgroundHue: Long
)

object DungeonCatalog {
    val chapters = listOf(
        DungeonChapter(
            id = 1,
            name = "幽暗密林",
            minLevel = 1,
            maxLevel = 50,
            description = "被浓密迷雾笼罩的初生密林，常有野狼、野猪与游荡盗贼出没。",
            iconEmoji = "🌲",
            backgroundHue = 0xFF1B2B1E
        ),
        DungeonChapter(
            id = 2,
            name = "熔岩地窟",
            minLevel = 51,
            maxLevel = 150,
            description = "地下沸腾的岩浆火河与烈焰巨蝎，狂暴的地底生物在火海中咆哮。",
            iconEmoji = "🌋",
            backgroundHue = 0xFF381818
        ),
        DungeonChapter(
            id = 3,
            name = "远古遗迹",
            minLevel = 151,
            maxLevel = 300,
            description = "失落已久的上古帝国残垣，石像鬼与机械魔像守卫着古老的秘密。",
            iconEmoji = "🏛️",
            backgroundHue = 0xFF2B281B
        ),
        DungeonChapter(
            id = 4,
            name = "极寒冰原",
            minLevel = 301,
            maxLevel = 500,
            description = "终年风雪肆虐的万载玄冰荒原，冰霜巨灵与寒冰魔兽盘踞在此。",
            iconEmoji = "❄️",
            backgroundHue = 0xFF142436
        ),
        DungeonChapter(
            id = 5,
            name = "龙王绝岭",
            minLevel = 501,
            maxLevel = 750,
            description = "太古巨龙筑巢的火山穹顶，充斥着灼热龙息与无尽宝藏。",
            iconEmoji = "🐲",
            backgroundHue = 0xFF352014
        ),
        DungeonChapter(
            id = 6,
            name = "虚空裂渊",
            minLevel = 751,
            maxLevel = 1000,
            description = "空间坍塌形成的异次元裂谷，充斥着不可名状的虚空噬魂魔怪。",
            iconEmoji = "🌌",
            backgroundHue = 0xFF221638
        ),
        DungeonChapter(
            id = 7,
            name = "诸神黄昏",
            minLevel = 1001,
            maxLevel = 1300,
            description = "诸神与魔尊大战的上古废墟，残存着堕落炽天使与神圣残响。",
            iconEmoji = "⚡",
            backgroundHue = 0xFF3A2412
        ),
        DungeonChapter(
            id = 8,
            name = "混沌魔渊",
            minLevel = 1301,
            maxLevel = 1600,
            description = "宇宙未开辟前的混沌深渊，毁灭之意充斥天地，魔尊遮天蔽日。",
            iconEmoji = "👹",
            backgroundHue = 0xFF2C102C
        ),
        DungeonChapter(
            id = 9,
            name = "九霄天庭",
            minLevel = 1601,
            maxLevel = 1850,
            description = "漂浮于三十三天之上的古老神庭，仙雾缭绕，天将与神尊镇守四方。",
            iconEmoji = "🏯",
            backgroundHue = 0xFF1C2D3B
        ),
        DungeonChapter(
            id = 10,
            name = "万界创世顶",
            minLevel = 1851,
            maxLevel = 2000,
            description = "万物起源与法则大道的至高极境，唯有超凡入圣的极道至尊方可踏足！",
            iconEmoji = "✨",
            backgroundHue = 0xFF301E3E
        )
    )

    fun getChapterForLevel(level: Int): DungeonChapter {
        val lvl = level.coerceIn(1, 2000)
        return chapters.find { lvl in it.minLevel..it.maxLevel } ?: chapters.last()
    }
}
