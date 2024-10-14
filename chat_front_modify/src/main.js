import { createApp } from 'vue'
import App from './App.vue'
import router from './router';
import axios from 'axios';

// [앱 생성 실시]
const app = createApp(App)
// [라우터 사용 설정]
app.use(router);
// [axios / http 통신]
app.provide('$axios', axios);

app.mount('#app');


