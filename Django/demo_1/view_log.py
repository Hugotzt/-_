import json
import threading
import paho.mqtt.client as mqtt

from django.shortcuts import render,HttpResponse
from demo_1.models import sub, card

username = '000'
password = '000'
host = '192.168.44.1'
port = 1883

# 线程 (开启用户反馈)
class CThread(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)  #重写父类方法
        self.topic = '/android' 
    # 连接后事件
    def on_connect(self, client, userdata, flags, respons_code):
        if respons_code == 0:
            # 连接成功
            print('Connection Succeed!')
        else:
            # 连接失败并显示错误代码
            print('Connect Error status {0}'.format(respons_code))
        # 订阅信息
        client.subscribe(topic=self.topic,qos=0)
        
    # 接收到数据后事件
    def on_message(self, client, userdata, msg):
        # 打印订阅消息主题
        print("topic", msg.topic)
        # 打印消息数据
        jsondata=json.loads(msg.payload)
        print("msg payload", jsondata)
        # 数据库操作
        if msg.topic == self.topic:
            # 写去数据库
            print('theme:{}\nsug:{}\nphone:{}\ntime:{}'.format(
                jsondata['theme'], 
                jsondata['sug'], 
                jsondata['phone'], 
                jsondata['time']))
            sub.objects.create(theme = jsondata['theme'],
            sug = jsondata['sug'], 
            phone = jsondata['phone'], 
            time= jsondata['time'])
            
    # 接口函数  
    def mqttstart(self):
        # 建立连接对象
        client = mqtt.Client()
        # 设置账号密码
        client.username_pw_set(username=username, password=password)
        # 注册事件（回调函数）
        client.on_connect = self.on_connect
        # 连接到服务器
        client.connect(host=host, port=port, keepalive=60)
        # 接收订阅事件
        client.on_message = self.on_message
        # 阻塞状态
        client.loop_forever()
        
    def run(self):
        self.mqttstart()

# 返回登录页面
def main(request):
    a = CThread()
    a.start()
    return render(request,'main.html')

# 展示登录
def show_log(request):
    return render(request,'log.html')

# 响应登录
def log(request):
    card_id = request.GET['card_id']
    try:
        user = card.objects.get(card_id=card_id)
        return render(request,'view_function.html')
    except:
        return HttpResponse('此卡未绑定')
    
# 展示注册
def show_register(request):
    return render(request,'register.html') 

# 响应注册
def register(request):
    name = request.GET['name']
    card_id = request.GET['card_id']
    card.objects.create(card_id = card_id,name=name)
    return render(request,'log.html')


