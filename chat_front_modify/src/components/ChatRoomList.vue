<template>
    <div>
    <h1>{{ user.name }}'s Chat Rooms</h1>

    <!-- 채팅방 생성 입력폼 -->
    <div class="input-group mb-3">
      <input v-model="receiver" type="text" id="receiver" class="form-control" placeholder="채팅방 이름 입력">
      <div class="input-group-append">
        <button class="btn btn-primary" @click="createRoom">채팅방 생성</button>
      </div>
    </div>

    <ul v-if="rooms && Array.isArray(rooms) && rooms.length > 0">
        <li v-for="room in rooms" :key="room.roomId">       
        <p class="room_list" v-if="room.sender != user.name" @click="setSelectedRoom(room)">{{ room.sender }}</p>
        <p class="room_list" v-else @click="setSelectedRoom(room)">{{ room.receiver }}</p>
        </li>
    </ul>
      <p v-else>No chat rooms available.</p>
      <!-- <ul v-if="rooms.length > 0">
        <li v-for="room in rooms" :key="room.roomId">
            <p class="room_list" @click.stop="setSelectedRoom(room)">
            {{ room.sender !== user.name ? room.sender : room.receiver }}
            </p>
        </li>
        </ul>
        <p v-else>No chat rooms available.</p> -->
    </div>

    <!-- 채팅방 상세조회 -->
    <div>
        <div class="container">
            <!-- 채팅방 제목에 상대방 이름 표시 -->
            <h3>{{ '채팅방: ' + (selectedRoom.sender === user ? selectedRoom.receiver : selectedRoom.sender) }}</h3>

            <div id="messageArea" class="border" style="height: 400px; overflow-y: scroll;">
            <!-- 메시지 목록을 불러오기 -->
            <div v-for="(message, index) in messages" :key="index">
                <div :class="message.sender === user ? 'my-message' : 'their-message'">
                <strong>{{ message.sender }}</strong>: {{ message.message }}
                <!-- 본인의 메시지일 경우 읽음 여부 표시 -->
                <span v-if="message.sender === user && message.readYn" class="read-status">읽음</span>
                <!-- 내가 추가한 생성시각 -->
                <span>{{ message.createdAt }}</span>
                </div>
            </div>
            </div>

            <div class="form-group">
            <input type="text" v-model="messageInput" class="form-control" placeholder="메시지 입력" @keydown.enter="sendMessage" />
            </div>
            <button class="btn btn-primary" @click="sendMessage">전송</button>
        </div>
    </div>    

  </template>
  
  <script>
  import { ref, onMounted, reactive, onBeforeUnmount } from 'vue';
  import axios from 'axios';
  import {useRouter} from 'vue-router';
  import SockJS from 'sockjs-client';
  import Stomp from 'stompjs';

  export default {
    setup() {
        /* chatRoomList */
        const rooms = reactive([]);  // 반응형 상태로 채팅방 리스트 저장
        const user = reactive({name: ''}); 
        const receiver = ref('');  // 채팅방 상대방 입력 필드
        const sender = ref('');

        /* chatRoomDetail */
        const stompClient = ref(null);
        const roomId = ref('');
        const messages = reactive([]);  // 메시지 목록
        const messageInput = ref('');  // 메시지 입력 필드
        const selectedRoom = ref(''); // 선택된 
 
      // 로그인한 사용자의 JWT token
      const token = 'eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTVEFOTDIiLCJzdWIiOiJKV1QgVG9rZW4iLCJpZCI6NCwibG9naW5JZCI6InRlc3Q1QGdtYWlsLmNvbSIsIm5hdGlvbmFsaXR5Ijoic2VvdWwiLCJzZXgiOiJGRU1BTEUiLCJwb2ludCI6MCwibmlja25hbWUiOiLqsIDsp4DrgqgiLCJsYW5ndWFnZSI6IktPUkVBTiIsImF1dGhvcml0aWVzIjoiUk9MRV9NRU1CRVIiLCJpYXQiOjE3Mjg5MDIxMDIsImV4cCI6MTcyODkzMjEwMn0.GJ2zPKUU207OeqWs3apf3_8-QQ1zYE1dgM13IYCal4A';  
      // 서버에서 데이터를 가져오는 함수
      const fetchChatRooms = async () => {
        try {
            console.log("Fetching chat rooms...");
            const response = await axios.get('http://localhost:8080/api/v1/chat', {
            headers: {
                Authorization: `Bearer ${token}`,
            }
        });
        rooms.splice(0, rooms.length, ...response.data.rooms);
        user.name = response.data.user;    // 사용자 이름 저장
        } catch (error) {
          console.error("Error fetching chat rooms:", error);
        }
      };
      
    // 새로운 채팅방을 생성하는 함수
    const createRoom = async () => {
        if (!receiver.value) {
            alert("상대방 이름을 입력해주세요.");
            return;
        }
        try {
            const response = await axios.post('http://localhost:8080/api/v1/chat', {
            sender: user.name,   // 로그인한 사용자 (채팅방 생성자)
            receiver: receiver.value  // 입력된 상대방 이름
            }, {
            headers: {
                Authorization: `Bearer ${token}`,
            }
            });
            alert(`${receiver.value}와의 채팅방이 생성되었습니다.`);
            fetchChatRooms();  // 채팅방 목록을 다시 가져와서 업데이트
        } catch (error) {
            console.error("채팅방 생성 실패", error);
            alert("채팅방 생성에 실패했습니다. 다시 시도해주세요.");
        }
    };


    /* roomId 설정 함수 */
    const setSelectedRoom = (room) => {
        fetchChatRoomDetail(room); // 선택한 채팅방의 상세 내용을 불러오는 함수 호출
        connect();
    };

    /* WebSocket을 통해 서버에 연결 */
    const connect = () => {
    //   const socket = new SockJS('/ws-stomp');
    const socket = new SockJS('http://localhost:8080/ws-stomp');
    stompClient.value = Stomp.over(socket);


      stompClient.value.connect({}, (frame) => {
        console.log('Connected: ' + frame);
        stompClient.value.subscribe('/sub/' + roomId, (message) => {
          showMessage(JSON.parse(message.body));
        });
      }, (error) => {
        console.log('Connection error: ', error);
      });
    };
    
    /* 메시지를 불러오는 함수 */
    const fetchChatRoomDetail = async (room) => {
        if (!room) {
            console.error("room is not set.");
            return;
        }

        try {
            console.log("Fetching chat rooms...");
            console.log("roomId: " + room.roomId);
            const response = await axios.get(`http://localhost:8080/api/v1/chat/${room.roomId}`, {
                headers: {
                Authorization: `Bearer ${token}`,
                } 
            });

            console.log(response.data);
            console.log(response);
            // user.name = response.data.user;    // 사용자 이름 저장
            selectedRoom.value = response.data.room;
            console.log("Room: " + selectedRoom.value);

            messages.splice(0, messages.length, ...response.data.messages);
            console.log("Messages: " + messages.length);
        } catch (error) {
            console.error("Error fetching chat rooms:", error);
        }
      };

    /* 메시지 보내기 함수 */
    const sendMessage = () => {
      if (!messageInput.value) {
        alert('메시지를 입력해주세요.');
        return;
      }

      const message = {
        roomId: roomId,
        sender: user,
        message: messageInput.value,
        type: 'TALK'
      };

      stompClient.value.send('/pub/chat/message', {}, JSON.stringify(message));
      messageInput.value = '';  // 입력창 비우기
    };

    // 수신된 메시지를 표시하는 함수
    const showMessage = (message) => {
        messages.push(message);
        const messageArea = document.getElementById('messageArea');
        messageArea.scrollTop = messageArea.scrollHeight;  // 스크롤을 가장 아래로
    };

    // 컴포넌트가 마운트될 때 데이터 가져오기
    //   onMounted(fetchChatRooms);
    onMounted(async () => {
        console.log("Component is mounted. Now fetching chat rooms...");
        await fetchChatRooms();
        // connect();  // WebSocket 연결
    });

        // 컴포넌트가 언마운트될 때 WebSocket 연결 해제
        onBeforeUnmount(() => {
        if (stompClient.value) {
            stompClient.value.disconnect();
            console.log('Disconnected');
        }
    });


      return {
        rooms,
        user,
        receiver,
        createRoom,
        messages,
        messageInput,
        sendMessage,
        selectedRoom,
        setSelectedRoom
      };
    }
  }
  </script>
  
  <style scoped>
  ul {
    list-style-type: none;
    padding: 0;
  }
  
  li {
    padding: 8px;
    margin-bottom: 4px;
    background-color: #f0f0f0;
    border-radius: 4px;
  }
  
  h1 {
    font-size: 24px;
    margin-bottom: 16px;
  }
  
  p {
    font-size: 16px;
    color: #888;
  }
  </style>
  