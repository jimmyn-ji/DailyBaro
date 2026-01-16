<template>
  <div id="app" class="global-bg">
    <router-view></router-view>
  </div>
</template>

<style>
body,
#app,
.el-container,
.el-main,
.el-aside,
.el-menu,
.el-menu-item,
.el-submenu__title,
.el-header,
.el-footer {
  background: transparent !important;
  box-shadow: none !important;
}
.global-bg {
  min-height: 100vh;
  background: url('/static/imgs/bg.jpg') no-repeat center center fixed;
  background-size: cover;
  background-color: transparent !important;
}
.el-input {
  width: 180px;
}
:deep(.el-input__wrapper) {
  border: none;
}
</style> 