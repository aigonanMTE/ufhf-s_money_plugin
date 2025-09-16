package org.testmode.asd.setting

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.File

fun overwriteJson(jsonFile: File):Boolean {
    try {
        val gson = GsonBuilder().setPrettyPrinting().create()

        // 새 JSON 구조 정의
        val leaderboardContent = JsonArray().apply {
            add("player_name : {player_name}")
            add("money : {money}")
            add("서버에 오신걸 환영합니다!")
        }

        val leaderboard = JsonObject().apply {
            addProperty("title", "привет, мир!")
            add("content", leaderboardContent)
        }

        val itemReturnCycleUnit = JsonObject().apply {
            addProperty("day", true)
            addProperty("hour", false)
            addProperty("minute", false)
        }

        val userShop = JsonObject().apply {
            addProperty("description_title", "상점 사용법")
            addProperty(
                "description_message",
                "사고싶은 아이템을 클릭하여 구매할수 있습니다.\n/상점 등록 <가격> 명령어로 아이템을 상점에 등록 할수 있습니다."
            )
            addProperty("Item_return_cycle", 14)
            add("Item_return_cycle_unit", itemReturnCycleUnit)
            addProperty("use_max_item_count", true)
            addProperty("max_item_count", 20)
        }

        val root = JsonObject().apply {
            addProperty("fun_cool_sexy_message", false)
            addProperty("use_leaderboard", true)
            add("leaderboard", leaderboard)
            addProperty("use_userShop", true)
            add("userShop", userShop)
        }

        // 🔥 기존 내용은 지우고 새로운 JSON으로 덮어쓰기
        jsonFile.writeText(gson.toJson(root))
    }catch (e:Exception){
        return false
    }
    return true
}
