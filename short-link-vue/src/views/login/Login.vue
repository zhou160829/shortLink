<script setup>
import {computed, reactive} from 'vue';
import {LockOutlined, UserOutlined} from '@ant-design/icons-vue';
import {useRouter} from "vue-router"
import loginApi from '../../api/login.js';
import cookie from 'js-cookie'

const router = useRouter()
const formState = reactive({
  username: '',
  password: '',
  remember: true,
});

let loginInfo = reactive({
  id: '',
  age: '',
  avatar: '',
  mobile: '',
  nickname: '',
  sex: ''
});

const onFinish = values => {
  //第一步 调用接口进行登录，返回token字符串
  loginApi.userLogins(formState)
      .then(response => {
        //第一个参数cookie名称，第二个参数值，第三个参数作用范围
        cookie.set('token', response.data, {domain: 'localhost'})
        //第四步 调用接口 根据token获取用户信息，为了首页面显示
        loginApi.getLoginUserInfo()
            .then(response => {
              loginInfo = response.data
              //获取返回用户信息，放到cookie里面
              cookie.set('user', loginInfo, {domain: 'localhost'})
            })
        router.push({path: '/Home'})
      })
};
const onFinishFailed = errorInfo => {
  console.log('Failed:', errorInfo);
};
const disabled = computed(() => {
  return !(formState.username && formState.password);
});
</script>

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
            <UserOutlined class="site-form-item-icon"/>
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
            <LockOutlined class="site-form-item-icon"/>
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

<style scoped>
#components-form-demo-normal-login .login-form {
  max-width: 300px;
}

#components-form-demo-normal-login .login-form-forgot {
  float: right;
}

#components-form-demo-normal-login .login-form-button {
  width: 100%;
}
</style>