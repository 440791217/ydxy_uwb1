package org.ydxy.uwb.http;

import com.alibaba.fastjson.JSONObject;

public class HttpResponse {
    public static JSONObject getResponse(JSONObject jsonObject){
        jsonObject.put("code",200);

        return jsonObject;
    }
}
