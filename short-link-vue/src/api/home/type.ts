export interface ResponseData {
    code: number,
    message: string,
    ok: boolean
}


//登录接口返回用户信息数据
export interface Group {
    id: string,
    name: string,
    createUserName: string,
    createTime: string,
}

//登录接口返回的数据的ts类型
export interface GroupResponseData extends ResponseData {
    data: Group[]
}