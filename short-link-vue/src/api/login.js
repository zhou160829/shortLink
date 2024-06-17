import request from "../utils/request.js"
// 账号登录
export default {
    userLogins(data) {
        return request({
            url: `/user/login`,
            method: "post",
            data
        });
    },

    getLoginUserInfo(){
        return request({
            url: `/user/getLoginUser`,
            method: "get"
        });
    }
}
