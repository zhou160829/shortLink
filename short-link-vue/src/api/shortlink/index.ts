import request from "../../utils/request.ts"
import {LoginData, ResponseData, UserLoginResponseData} from "@/api/shortlink/type.ts";

enum API {
    //用户登录接口
    USER_LOGIN_URL = '/user/login',

    USER_LOGIN_OUT_URL = '/user/loginOut',
}

export const userLogin =(data: LoginData) => request.post<any, UserLoginResponseData>(API.USER_LOGIN_URL, data)


export const loginOut =() => request.get<any, ResponseData>(API.USER_LOGIN_OUT_URL)