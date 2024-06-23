<script setup>
import {h, reactive,} from 'vue';

import {AppstoreOutlined, CalendarOutlined, MailOutlined, SettingOutlined,} from '@ant-design/icons-vue';
import {useRouter} from 'vue-router'

const router = useRouter()
const state = reactive({
  mode: 'inline',
  theme: 'light',
  selectedKeys: ['1'],
  openKeys: ['sub1'],
});

function getItem(label, key, icon, children, type) {
  return {
    key,
    icon,
    children,
    label,
    type,
  };
}

const items = reactive([
  getItem('Navigation One', '1', h(MailOutlined)),
  getItem('Navigation Two', '2', h(CalendarOutlined)),
  getItem('Navigation Two', 'sub1', h(AppstoreOutlined), [
    getItem('Option 3', '3'),
    getItem('Option 4', '4'),
    getItem('Submenu', 'sub1-2', null, [getItem('Option 5', '5'), getItem('Option 6', '6')]),
  ]),
  getItem('Navigation Three', 'sub2', h(SettingOutlined), [
    getItem('Option 7', '7'),
    getItem('Option 8', '8'),
    getItem('Option 9', '9'),
    getItem('Option 10', '10'),
  ]),
]);
const changeMode = checked => {
  state.mode = checked ? 'vertical' : 'inline';
};
const changeTheme = checked => {
  state.theme = checked ? 'dark' : 'light';
};
const clickData = () => {
  console.log(router)
  router.push({path: '/Login'})
}
</script>

<template>
  <div>
    <a-switch :checked="state.mode === 'vertical'" @change="changeMode"/>
    Change Mode
    <span class="ant-divider" style="margin: 0 1em"/>
    <a-switch :checked="state.theme === 'dark'" @change="changeTheme"/>
    Change Theme
    <br/>
    <br/>
    <a-menu
        v-model:openKeys="state.openKeys"
        v-model:selectedKeys="state.selectedKeys"
        style="width: 256px"
        :mode="state.mode"
        :items="items"
        :theme="state.theme"
    ></a-menu>
  </div>
</template>

<style scoped>

</style>