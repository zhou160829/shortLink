import request from "@/utils/request.ts";
import {GroupResponseData} from "@/api/home/type.ts";

enum API {
    //用户登录接口
    GROUP_ADD_URL = '/shortGroup/add',
    GROUP_UPDATE_URL = '/shortGroup/update',
    GROUP_URL = '/shortGroup',
    GROUP_DEL_URL = '/shortGroup/delete',
    GROUP_URL_LIST = '/shortGroup/findList',
}

export const reqGroupList = () => request.get<any, GroupResponseData>(API.GROUP_URL_LIST)