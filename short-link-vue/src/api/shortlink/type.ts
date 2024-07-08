//定义详情模块数据ts类型
export interface ResponseData {
    code: number,
    message: string,
    ok: boolean
}


//用户登录接口需要携带参数类型
export interface LoginData {
    username: string,
    password: string
}

//登录接口返回用户信息数据
export interface UserInfo {
    nickname: string,
    token: string
}

//登录接口返回的数据的ts类型
export interface UserLoginResponseData extends ResponseData {
    data: UserInfo
}