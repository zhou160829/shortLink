//本地存储操作用户信息的方法
import cookie from "js-cookie";


export const SET_TOKEN = (userInfo: string) => {
    cookie.set("USERINFO", userInfo, {expires: 1})
}

export const GET_TOKEN = () => {
    return cookie.get('USERINFO') ? cookie.get('USERINFO') : null;
}

//清除本地存储用户相关的数据
export const REMOVE_TOKEN = () => {
    cookie.remove('USERINFO');
}