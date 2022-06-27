import machine    
from machine import Pin
from machine import UART
import network
import io
import time                      
import json
import _thread                   
from simple import MQTTClient 

#初始化串口收发功能
uart = UART(2, baudrate=115200, bits=8, parity=0, rx=19, tx=18, timeout=10)      

Server = '172.17.140.121'        #MQTT服务器IP
CLIENT_ID = 'hugo_eps32'              #协调器ID    
port = 1883                      #端口号
topic_set = '/card'                #发送的主题
topic_get = 'sendsensor'                #接收的主题
username = '000'                #mqtt登录用户名
password = '000'             #mqtt登录密码
SSID = '123456'                #热点
PASSWORD = '1751493913tao'      #热点密码
led = Pin(2, Pin.OUT, value=0)#控制灯
#全局的参数
global state 
global ZigBee_Message       #zigbee串口发送过来的信息
global wlan                  #进入互联网
global mqtt                #mqtt的成员

#接收到服务器发送过来的信息
def sub_cb(topic,msg):
  print(topic, msg)
  s = msg.decode('utf-8','ignore')           
  parsed = eval(s)                      
  
  if 'beep' in parsed:
    orc = parsed['beep']                  
    if orc == 'on': 
      s = b'\x01\x02\x01'
    if orc == 'off':
      s = b'\x01\x02\x00'
    print(s)
    uart.write(s)
  
  if 'led_1_1' in parsed:
    orc = parsed['led_1_1']                  
    if orc == 'on': 
      s = b'\x01\x01\x01\x01'
    if orc == 'off':
      s = b'\x01\x01\x01\x00'
    print(s)
    uart.write(s)


  if 'led_1_2' in parsed:
    orc = parsed['led_1_2']                 
    if orc == 'on': 
      s = b'\x01\x01\x02\x01'
    if orc == 'off':
      s = b'\x01\x01\x02\x00'
    print(s)
    uart.write(s)
    
  if 'led_2_1' in parsed:
    orc = parsed['led_2_1']                 
    if orc == 'on': 
      s = b'\x02\x01\x01\x01'
    if orc == 'off':
      s = b'\x02\x01\x01\x00'
    print(s)
    uart.write(s)
    
  if 'led_2_2' in parsed:
    orc = parsed['led_2_2']                 
    if orc == 'on': 
      s = b'\x02\x01\x02\x01'
    if orc == 'off':
      s = b'\x02\x01\x02\x00'
    print(s)
    uart.write(s)

 #进入互联网
def connectWifi(ssid,passwd):
  wlan=network.WLAN(network.STA_IF)         #创建网络
  wlan.active(True)                         #打开网络
  wlan.disconnect()                         #先断开已连接的wifi
  wlan.connect(ssid,passwd)                 #连接wifi 进入互联网
  while(wlan.ifconfig()[0]=='0.0.0.0'):     #连接失败 重新寻找连接
    time.sleep(1)
 
# 串口接受消息
def threadPublish():  
  while True: 
    if(uart.any()):                             #如果接收到信息
      try:
        card = str(uart.read(),'utf-8')       #读取信息 
      except:
        time.sleep(1)
      finally:  


        print("card_id:",card)
        mqtt.publish(topic_set,str(card))           #发送主题以及信息
        time.sleep(2)                            
    else:
      print('no message')                       #没有收到信息 打印在控制面板
      time.sleep(2)                       

def threadSort():
  while True:
    mqtt.wait_msg()                         
    time.sleep(1)

#主程序
connectWifi(SSID,PASSWORD)
mqtt = MQTTClient(CLIENT_ID,Server,port,username,password)  
mqtt.set_callback(sub_cb)                    #设置回调函数
mqtt.connect()                               #连接mqtt服务器
mqtt.subscribe(topic_get)                    #订阅主题
print("连接到服务器:%s\n接收订阅主题为:%s\n发送订阅主题为:%s\n" % (Server, topic_get,topic_set))
_thread.start_new_thread(threadSort,())      #接收mqtt服务器数据
_thread.start_new_thread(threadPublish,())  









