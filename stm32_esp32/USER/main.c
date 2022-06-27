#include "led.h"
#include "delay.h"
#include "key.h"
#include "sys.h"
#include "usart.h"
#include "uart4.h"
#include "sim900a.h"
#include "rc522.h"

 int main(void)
 {		
	int abcc;
	delay_init();	    	 //延时函数初始化	  
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2); //设置NVIC中断分组2:2位抢占优先级，2位响应优先级
	uart_init(115200);	 //串口初始化为115200
 	uart4_init(115200);
	LED_Init();			     //LED端口初始化
	KEY_Init();          //初始化与按键连接的硬件接口
	RC522_Init();
	 
//	delay_ms(300);
//	while(!simCard_test())
//		 delay_ms(150);
//	printf("开始拨打电话or发短信\r\n");

////	abcc=call_phone_num(); status
//	 
////	 send_message_num("hello world!");
//	 //6D4B 8BD5 77ED 4FE1 
//	 
//	 
//	 abcc=send_message_chinese("6D4B8BD577ED4FE1"); //测试短信
//	
//	
//	
//	 printf("最后结果：%d",abcc);
 	while(1)
	{
		RC522_Handel();
		LED1=0;
		delay_ms(1000);
		LED1=1;
		delay_ms(1000);
	}	 
 }

