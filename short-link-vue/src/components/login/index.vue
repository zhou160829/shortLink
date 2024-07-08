<template>
  <div id="components-form-demo-normal-login">
  <a-form
      :model="formState"
      name="normal_login"
      class="login-form"
      @finish="onFinish"
      @finishFailed="onFinishFailed"
  >
    <a-form-item
        label="Username"
        name="username"
        :rules="[{ required: true, message: 'Please input your username!' }]"
    >
      <a-input v-model:value="formState.username">
        <template #prefix>
          <UserOutlined class="site-form-item-icon" />
        </template>
      </a-input>
    </a-form-item>

    <a-form-item
        label="Password"
        name="password"
        :rules="[{ required: true, message: 'Please input your password!' }]"
    >
      <a-input-password v-model:value="formState.password">
        <template #prefix>
          <LockOutlined class="site-form-item-icon" />
        </template>
      </a-input-password>
    </a-form-item>

    <a-form-item>
      <a-form-item name="remember" no-style>
        <a-checkbox v-model:checked="formState.remember">Remember me</a-checkbox>
      </a-form-item>
      <a class="login-form-forgot" href="">Forgot password</a>
    </a-form-item>

    <a-form-item>
      <a-button :disabled="disabled" type="primary" html-type="submit" class="login-form-button">
        Log in
      </a-button>
      Or
      <a href="">register now!</a>
    </a-form-item>
  </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive, computed } from 'vue';
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import {useRoute, useRouter} from "vue-router";
import {message} from 'ant-design-vue';

import useUserStore from "@/store/modules/user";
let userStore = useUserStore();

const [messageApi] = message.useMessage();

let $route = useRoute()
let $router = useRouter()
interface FormState {
  username: string;
  password: string;
  remember: boolean;
}
const formState = reactive<FormState>({
  username: '',
  password: '',
  remember: true,
});
const onFinish = async () => {
  try {
    //第一步 调用接口进行登录，返回token字符串
    await userStore.userLogin(formState)
    //获取url的query参数
    let redirect = $route.query.redirect;
    if (redirect) {
      $router.push(redirect as string);
    } else {
      $router.push('/home');
    }
  } catch (error) {
    messageApi.error('出现错误');
  }
};

const onFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo);
};
const disabled = computed(() => {
  return !(formState.username && formState.password);
});
</script>
<style scoped>
#components-form-demo-normal-login {
  text-align: center;
  display: grid;
  place-items: center;
  height: 100vh; /* 或其他高度 */
}

#components-form-demo-normal-login .login-form {
  width: 50%; /* 或固定宽度 */
  margin-left: auto;
  margin-right: auto;
  max-width: 300px;
}
#components-form-demo-normal-login .login-form-forgot {
  float: right;
}
#components-form-demo-normal-login .login-form-button {
  width: 100%;
}
</style>
