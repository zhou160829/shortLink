import request from "../../utils/request.ts"
import {LoginData, UserLoginResponseData} from "@/api/shortlink/type.ts";

enum API {
    //用户登录接口
    USER_LOGIN_URL = '/user/login',
}

export const userLogin =(data: LoginData) => request.post<any, UserLoginResponseData>(API.USER_LOGIN_URL, data)