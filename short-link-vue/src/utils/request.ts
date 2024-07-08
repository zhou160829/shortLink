import axios from 'axios';
import useUserStore from '@/store/modules/user';
import {message} from 'ant-design-vue';

const [messageApi] = message.useMessage();
const request = axios.create({
    baseURL: 'http://localhost:9527',//请求的基础路径的设置
    timeout: 5000//超时的时间的设置,超出五秒请求就是失败的
});

//请求拦截器
request.interceptors.request.use((config) => {
    //获取用户仓库
    let userStore = useUserStore();
    if (userStore.userInfo.token) {
        config.headers.token = userStore.userInfo.token;
    }
    return config;
});
//响应拦截器
request.interceptors.response.use((response) => {
    return response.data;
}, (error) => {
    //处理http网络错误
    let status = error.response.status;
    switch (status) {
        case 404:
            //错误提示信息
            messageApi.error('请求失败路径出现问题');
            break;
        case 500 | 501 | 502 | 503 | 504 | 505:
            messageApi.error('出现未知错误，请稍后再试');
            break;
        case 401:
            messageApi.error('参数有误');
            break;
    }
    return Promise.reject(new Error(error.message))
});
//务必对外暴露axios
export default request;


