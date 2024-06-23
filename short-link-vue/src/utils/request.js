import axios from 'axios';
import proxy from '../config/proxy';
import router from '../router';
import {tryRefreshToken} from './refreshToken'
import {message, Modal} from 'ant-design-vue';
import {createVNode} from "vue";
import {ExclamationCircleOutlined} from "@ant-design/icons-vue";
import cookie from "js-cookie";

const [messageApi, contextHolder] = message.useMessage();

const env = import.meta.env.MODE || 'development';
const host = env === 'mock' ? 'https://mock.boxuegu.com/mock/3359' : proxy[env].host; // 如果是mock模式 就不配置host 会走本地Mock拦截
const CODE = {
    LOGIN_TIMEOUT: 1000,
    REQUEST_SUCCESS: 200,
    REQUEST_FOBID: 1001,
};
// 登录异常弹窗处理
let isLogin = true
// 刷新标记
// let refreshing = ref(false)

const instance = axios.create({
    baseURL: host,
    timeout: 5000,
    withCredentials: false,
});

/**
 * 请求携带
 */
instance.interceptors.request.use((config) => {
    const TOKEN = cookie.get("token")
    config.headers = {
        "Content-Type": "application/json",
        "token": TOKEN
    }
    return config
});

instance.defaults.timeout = 2000;

/**
 * cookie重刷
 * @param err
 * @returns {Promise<axios.AxiosResponse<any>|boolean>}
 */
async function refreshToken(err) {
    // 尝试刷新token
    let success = await tryRefreshToken();
    if (success) {
        // refreshing.value = false;
        return instance(err.config);
    }
    // refreshing.value = false;

    Modal.confirm({
        title: '未登录或登录超时！',
        icon: createVNode(ExclamationCircleOutlined),
        content: '点击确定跳转到登录页',
        onOk() {
            router.push('/login')
        }
    });
    return true;
}

function    alertLoginMessage() {
    isLogin = false;
    sessionStorage.removeItem('userInfo');
    sessionStorage.removeItem("token");

    Modal.confirm({
        title: '您的账号登录超时或在其他机器登录，请重新登录或更换账号登录！',
        icon: createVNode(ExclamationCircleOutlined),
        content: '登录超时',
        okText: '重新登录',
        cancelText: '继续浏览',
        onOk() {
            router.push('/login')
        },
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        onCancel() {
        },
    });
}

// const sleep = (delay) => new Promise((resolve) => setTimeout(resolve, delay))
instance.interceptors.response.use(
    async (response) => {
        // 1.获取业务状态码
        let code = response.data.code;
        // 2.业务状态码为200，直接返回
        if (code === CODE.REQUEST_SUCCESS) {
            return response.data;
        }

        // 3.业务状态码为401，代表未登录
        if (code === 10000 && isLogin) {
            isLogin = false;
            alertLoginMessage();
        }

        return response.data;
        /*    // 4.业务状态码为其它，返回异常
            ElMessage({
              message: response.data.msg,
              type: 'error'
            });
            throw new Error(response.data.msg);*/
    },
    async (err) => {
        if (err.code === CODE.LOGIN_TIMEOUT && isLogin) {
            // 登录异常或超时，刷新token
            return refreshToken(err);
        }
        // refreshing = false;
        return Promise.reject(err);
    },
);

export default instance;
