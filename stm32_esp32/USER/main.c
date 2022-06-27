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
	delay_init();	    	 //��ʱ������ʼ��	  
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2); //����NVIC�жϷ���2:2λ��ռ���ȼ���2λ��Ӧ���ȼ�
	uart_init(115200);	 //���ڳ�ʼ��Ϊ115200
 	uart4_init(115200);
	LED_Init();			     //LED�˿ڳ�ʼ��
	KEY_Init();          //��ʼ���밴�����ӵ�Ӳ���ӿ�
	RC522_Init();
	 
//	delay_ms(300);
//	while(!simCard_test())
//		 delay_ms(150);
//	printf("��ʼ����绰or������\r\n");

////	abcc=call_phone_num(); status
//	 
////	 send_message_num("hello world!");
//	 //6D4B 8BD5 77ED 4FE1 
//	 
//	 
//	 abcc=send_message_chinese("6D4B8BD577ED4FE1"); //���Զ���
//	
//	
//	
//	 printf("�������%d",abcc);
 	while(1)
	{
		RC522_Handel();
		LED1=0;
		delay_ms(1000);
		LED1=1;
		delay_ms(1000);
	}	 
 }

