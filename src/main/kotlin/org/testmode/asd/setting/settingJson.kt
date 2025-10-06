package org.testmode.asd.setting

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.File

fun overwriteJson(jsonFile: File): Boolean {
    return try {
        val gson = GsonBuilder().setPrettyPrinting().create()

        // leaderboard.content 배열 정의
        val leaderboardContent = JsonArray().apply {
            add("{coler.green}player_name : {player_name}")
            add("{coler.blue}서버에 오신걸 환영합니다!")
        }

        // leaderboard 객체 정의
        val leaderboard = JsonObject().apply {
            addProperty("title", "{coler.green}привет, мир!")
            addProperty("money_line", 1)
            addProperty("money_line_content", "{coler.green}money : {money}원")
            add("content", leaderboardContent)
        }

        // userShop 객체 정의
        val userShop = JsonObject().apply {
            addProperty("main_display_name", "상점 {page}")
            addProperty("description_title", "상점 사용법")
            addProperty(
                "description_message",
                "사고싶은 아이템을 클릭하여 구매할수 있습니다.\n/상점 등록 <가격> 명령어로 아이템을 상점에 등록 할수 있습니다."
            )
            addProperty("use_Item_return_cycle", true)
            addProperty("Item_return_cycle", 14)
            addProperty("use_max_item_count", true)
            addProperty("max_item_count", 20)
        }

        // 루트 JSON 정의
        val root = JsonObject().apply {
            addProperty("fun_cool_sexy_message", false)
            addProperty("use_leaderboard", true)
            add("leaderboard", leaderboard)
            addProperty("use_userShop", true)
            add("userShop", userShop)
        }

        // 파일 덮어쓰기
        jsonFile.writeText(gson.toJson(root))
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}