<script setup lang="ts">
import {h, reactive} from 'vue';
import {AppstoreOutlined, CalendarOutlined, MailOutlined, SettingOutlined,} from '@ant-design/icons-vue';
import type {MenuMode, MenuTheme} from 'ant-design-vue';
import {ItemType} from 'ant-design-vue';

import Bottom from "./bottom/index.vue";
import Top from "./top/index.vue";

const state = reactive({
  mode: 'inline' as MenuMode,
  theme: 'light' as MenuTheme,
  selectedKeys: ['1'],
  openKeys: ['sub1'],
});

function getItem(
    label: string,
    key: string,
    icon?: any,
    children?: ItemType[],
    type?: 'group',
): ItemType {
  return {
    key,
    icon,
    children,
    label,
    type,
  } as ItemType;
}

const items: ItemType[] = reactive([
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

</script>

<template>
  <div class="container">
    <a-layout>
      <a-layout-header class="headerStyle">
        <Top/>
      </a-layout-header>

      <a-layout>
        <a-layout-sider class="sideStyle">
          <a-menu
              v-model:openKeys="state.openKeys"
              v-model:selectedKeys="state.selectedKeys"
              style="width: 256px;height:100vh"
              :mode="state.mode"
              :items="items"
              :theme="state.theme"
          ></a-menu>
        </a-layout-sider>

        <div class="content">
          <a-layout-content class="contentStyle">
            <router-view/>
          </a-layout-content>
        </div>

      </a-layout>

      <a-layout-footer class="footerStyle">
        <Bottom/>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;

  .content {
    margin-top: 70px;
    width: 1200px;
    min-height: 700px;
  }
}

.headerStyle {
  text-align: center;
  height: 70px;
  padding-inline: 0px;
  line-height: 70px;
}

.contentStyle {
  text-align: center;
  min-height: 120px;
  line-height: 120px;
}

.sideStyle {
  text-align: center;
  line-height: 120px;
  min-width: 300px;
}

.footerStyle {
  text-align: center;
}
</style>