package com.example.englishflashcard.data.api

import com.example.englishflashcard.model.CreatedByInfo
import com.example.englishflashcard.model.CreatedByProfile
import com.google.gson.*
import java.lang.reflect.Type

// Lớp tùy chỉnh Deserializer để chuyển đổi dữ liệu JSON thành object CreatedByInfo
// Giải quyết các trường hợp JSON có thể là chuỗi, đối tượng hoặc null
class CreatedByInfoDeserializer : JsonDeserializer<CreatedByInfo> {
    // Phương thức override để xử lý việc deserialize từ JSON sang CreatedByInfo
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): CreatedByInfo {
        // Kiểm tra nếu json là null hoặc giá trị null JSON, trả về object CreatedByInfo rỗng
        if (json == null || json.isJsonNull) return CreatedByInfo()
        
        // Bắt đầu block try-catch để xử lý các lỗi có thể xảy ra trong quá trình deserialize
        return try {
            // Trường hợp 1: JSON là một giá trị nguyên thủy (primitive) - ví dụ: chuỗi ID
            if (json.isJsonPrimitive) {
                // Tạo CreatedByInfo chỉ với _id là giá trị chuỗi từ JSON
                CreatedByInfo(_id = json.asString)
            } 
            // Trường hợp 2: JSON là một đối tượng (object) - ví dụ: { _id, username, email, profile }
            else if (json.isJsonObject) {
                // Chuyển json thành JsonObject để truy cập các trường
                val obj = json.asJsonObject
                // Tạo CreatedByInfo từ các trường trong object JSON
                CreatedByInfo(
                    // Lấy giá trị _id nếu tồn tại, ngược lại lấy chuỗi rỗng
                    _id = if (obj.has("_id")) obj.get("_id").asString else "",
                    // Lấy giá trị username nếu tồn tại, ngược lại lấy chuỗi rỗng
                    username = if (obj.has("username")) obj.get("username").asString else "",
                    // Lấy giá trị email nếu tồn tại, ngược lại lấy chuỗi rỗng
                    email = if (obj.has("email")) obj.get("email").asString else "",
                    // Xử lý trường profile (nested object)
                    profile = if (obj.has("profile") && obj.get("profile").isJsonObject) {
                        // Lấy đối tượng profile từ JSON
                        val p = obj.getAsJsonObject("profile")
                        // Tạo CreatedByProfile với các trường từ nested object
                        CreatedByProfile(
                            // Lấy fullName nếu tồn tại, ngược lại lấy chuỗi rỗng
                            fullName = if (p.has("fullName")) p.get("fullName").asString else "",
                            // Lấy avatarUrl nếu tồn tại, ngược lại lấy chuỗi rỗng
                            avatarUrl = if (p.has("avatarUrl")) p.get("avatarUrl").asString else "",
                            // Lấy bio nếu tồn tại, ngược lại lấy chuỗi rỗng
                            bio = if (p.has("bio")) p.get("bio").asString else ""
                        )
                    } else {
                        // Nếu không có profile hoặc không phải object, tạo CreatedByProfile rỗng
                        CreatedByProfile()
                    }
                )
            } else {
                // Trường hợp 3: JSON không phải primitive cũng không phải object, trả về object rỗng
                CreatedByInfo()
            }
        } catch (e: Exception) {
            // Nếu xảy ra lỗi trong quá trình deserialize, in stack trace để debug
            e.printStackTrace()
            // Trả về object CreatedByInfo rỗng để tránh crash
            CreatedByInfo()
        }
    }
}
