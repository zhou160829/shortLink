<script setup lang="ts">
//引入路由器
import {useRouter} from "vue-router";
import useUserStore from "@/store/modules/user";
import {ApiTwoTone,QuestionCircleFilled, UserOutlined} from '@ant-design/icons-vue';

let userStore = useUserStore();

let $router = useRouter();
const goHome = () => {
  //编程式导航跳转到首页
  $router.push({path: "/home"});
};
//退出登录按钮的回调
const logout = () => {
  //通知pinia仓库清除用户相关的信息
  userStore.logout();
  //编程式导航路由跳转到首页
  $router.push({path: "/home"});
};

//点击顶部下拉菜单进行路由跳转
const goUser = (path: string) => {
  $router.push({path: path});
};
</script>

<template>
  <div class="top">
    <div class="content">
      <!-- 左侧 -->
      <div class="left" @click="goHome">
        <ApiTwoTone />
        <p>My短链</p>
      </div>
      <div class="right">
        <QuestionCircleFilled />
        <p class="help">帮助中心</p>
        <!-- 如果有用户信息展示用户信息 -->
        <a-dropdown>
          <a class="ant-dropdown-link" @click.prevent>
            <UserOutlined/>
            {{ userStore.userInfo.realName }}
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="goUser">个人中心</a-menu-item>
              <a-menu-item @click="logout">退出登录</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </div>
  </div>
</template>

<style scoped>
.top {
  z-index: 999;
  width: 100%;
  height: 70px;

  display: flex;
  justify-content: center;

  .content {
    width: 100%;
    height: 70px;
    background: white;
    display: flex;
    justify-content: space-between;

    .left {
      font-size: 30px;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-left: 30px;

      p {
        font-size: 20px;
        color: #55a6fe;
        margin-bottom: 0;
      }
    }

    .right {
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      color: #55a6fe;

      .help {
        margin-right: 30px;
        margin-bottom: 0;
      }

      .ant-dropdown-link {
        margin-right: 10px;
        font-size: 20px;
      }
    }
  }
}
</style>