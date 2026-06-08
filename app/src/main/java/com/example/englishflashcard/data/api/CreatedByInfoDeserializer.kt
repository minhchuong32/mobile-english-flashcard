package com.example.englishflashcard.data.api

import com.example.englishflashcard.model.CreatedByInfo
import com.example.englishflashcard.model.CreatedByProfile
import com.google.gson.*
import java.lang.reflect.Type

class CreatedByInfoDeserializer : JsonDeserializer<CreatedByInfo> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): CreatedByInfo {
        if (json == null || json.isJsonNull) return CreatedByInfo()
        
        return try {
            if (json.isJsonPrimitive) {
                CreatedByInfo(_id = json.asString)
            } else if (json.isJsonObject) {
                val obj = json.asJsonObject
                CreatedByInfo(
                    _id = if (obj.has("_id")) obj.get("_id").asString else "",
                    username = if (obj.has("username")) obj.get("username").asString else "",
                    email = if (obj.has("email")) obj.get("email").asString else "",
                    profile = if (obj.has("profile") && obj.get("profile").isJsonObject) {
                        val p = obj.getAsJsonObject("profile")
                        CreatedByProfile(
                            fullName = if (p.has("fullName")) p.get("fullName").asString else "",
                            avatarUrl = if (p.has("avatarUrl")) p.get("avatarUrl").asString else "",
                            bio = if (p.has("bio")) p.get("bio").asString else ""
                        )
                    } else CreatedByProfile()
                )
            } else {
                CreatedByInfo()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CreatedByInfo()
        }
    }
}
