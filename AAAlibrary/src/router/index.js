import { createRouter, createWebHistory } from 'vue-router'
import LayoutVue from '../views/LayoutVue.vue'
import LoginVue from '../views/LoginVue.vue'
import BookResourcesVue from '../views/book/BookResourcesVue.vue'
//暂时没有的组建代码注释掉 
import BookRecordsVue from '../views/book/BookRecordsVue.vue'
import UserAvatarVUe from '../views/user/UserAvatarVUe.vue'
import UserResetPasswordVue from '../views/user/UserResetPasswordVue.vue'
import UserManage from '../views/user/UserManage.vue' 

import UserInfoVue from '../views/user/UserInfoVue.vue'
// import UserManageVue from '../views/user/UserManageVue.vue'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    //login组建命名冲突 改成LoginVue
    { path: '/login', component: LoginVue },
    {
      path: '/',
      component: LayoutVue,  //首页布局父组件
      redirect: '/book/resources',
      // 子路由
      children: [
        { path: '/book/resources', component: BookResourcesVue },
        { path: '/book/records', component: BookRecordsVue },
        { path: '/user/info', component: UserInfoVue },
        { path: '/user/avatar', component: UserAvatarVUe },
        { path: '/user/password', component: UserResetPasswordVue },
        { path: '/user/manage', component: UserManage },
        // { path: '/user/logout', component: Login },
        // // 用户管理组件
        // { path: '/user/manage', component: UserManageVue }
      ]
    },
    //添加一个测试路由
    { path: '/test', component:() => import('../views/TestVue.vue')},
  ]
})
 
export default router