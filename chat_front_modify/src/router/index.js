import { createRouter, createWebHistory } from 'vue-router';
import ChatRoomList from '../components/ChatRoomList.vue'; // ChatRoomList 컴포넌트 불러오기

const routes = [
  {
    path: '/',
    redirect: '/api/v1/chat'
  },
  {
    path: '/api/v1/chat',
    name: 'ChatRoomList',
    component: ChatRoomList
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
