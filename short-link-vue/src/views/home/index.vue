<script setup lang="ts">
import {h, onMounted, reactive} from 'vue';
import {MailOutlined,} from '@ant-design/icons-vue';
import type {MenuMode, MenuTheme} from 'ant-design-vue';
import {ItemType} from 'ant-design-vue';

import Bottom from "./bottom/index.vue";
import Top from "./top/index.vue";
import {GroupResponseData} from "@/api/home/type.ts";
import {reqGroupList} from "@/api/home";

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

//组件挂载完毕：发一次请求
onMounted(() => {
  getGroupList();
});

const items: ItemType[] = reactive([]);

//获取已有的医院的数据
const getGroupList = async () => {
  let result: GroupResponseData = await reqGroupList();
  if (result.code == 200) {
    let data = result.data;
    // 循环遍历
    for (let i = 0; i < data.length; i++) {
      let item = data[i];
      items.push(getItem(item.name, item.id, h(MailOutlined)));
    }
  }
};


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