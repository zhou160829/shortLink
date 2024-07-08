//定义用户相关的仓库
import {defineStore} from "pinia";
import {GET_TOKEN, REMOVE_TOKEN, SET_TOKEN} from "@/utils/user.ts";
import {LoginData, UserLoginResponseData} from "@/api/shortlink/type.ts";
import {userLogin} from "@/api/shortlink/login.ts";
import {UserState} from "@/store/modules/interface";

const useUserStore = defineStore('User', {
    state: (): UserState => {
        return {
            userInfo: JSON.parse(GET_TOKEN() as string) || {}
        }
    },
    actions: {
        //用户手机号码登录方法
        async userLogin(loginData: LoginData) {
            //登录请求
            let result: UserLoginResponseData = await userLogin(loginData);
            if (result.code == 200) {
                this.userInfo = result.data;
                //本地存储持久化存储用户信息
                SET_TOKEN(JSON.stringify(this.userInfo));
                //返回一个成功的Promise
                return 'ok';
            } else {
                return Promise.reject(new Error(result.message));
            }
        },
        //退出登录方法
        logout() {
            //清空仓库的数据
            this.userInfo = {nickname: '', token: ''};
            //清空本地存储的数据
            REMOVE_TOKEN();
        },

    },
    getters: {}
});

export default useUserStore;