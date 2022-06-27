#include "uart4.h"
#include "sys.h"
 #include "delay.h"  

////////////////////////////////////////////////////////////////////////////////// 	 
//如果使用ucos,则包括下面的头文件即可.
#if SYSTEM_SUPPORT_OS
#include "includes.h"					//ucos 使用	  
#endif


void uart4_init(u32 bound)
{
  //GPIO端口设置
    GPIO_InitTypeDef GPIO_InitStructure;
    USART_InitTypeDef USART_InitStructure;
    NVIC_InitTypeDef NVIC_InitStructure;
   
    RCC_APB1PeriphClockCmd(RCC_APB1Periph_UART4,ENABLE); //使能UART4时钟
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOC,ENABLE); //使能GPIOC时钟
//UART4_TX   GPIOC.10
    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10; //PC10接RXD
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP; //复用推挽输出
    GPIO_Init(GPIOC, &GPIO_InitStructure);//初始化GPIOC.10
    
  //UART4_RX   GPIOC.11初始化0
    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11;//PC11接TXD
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;//浮空输入
    GPIO_Init(GPIOC, &GPIO_InitStructure);//初始化GPIOC.11  
  //Uart4 NVIC 配置
    NVIC_InitStructure.NVIC_IRQChannel = UART4_IRQn ;
   NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority=3 ;//抢占优先级3
   NVIC_InitStructure.NVIC_IRQChannelSubPriority = 3;  //子优先级3
   NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;   //IRQ通道使能
   NVIC_Init(&NVIC_InitStructure); //根据指定的参数初始化VIC寄存器
   
   //UART 初始化设置
   USART_InitStructure.USART_BaudRate = bound;//串口波特率
   USART_InitStructure.USART_WordLength = USART_WordLength_8b;//字长为8位数据格式
   USART_InitStructure.USART_StopBits = USART_StopBits_1;//一个停止位
   USART_InitStructure.USART_Parity = USART_Parity_No;//无奇偶校验位
   USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;//无硬件数据流控制
   USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx; //收发模式
   USART_Init(UART4, &USART_InitStructure); //初始化串口4
   USART_ITConfig(UART4, USART_IT_RXNE, ENABLE);//开启串口接受中断
   USART_Cmd(UART4, ENABLE);                    //使能串口4
}

void UART4_SendString(char* s)
{
	while(*s)//检测字符串结束符
	{
		while(USART_GetFlagStatus(UART4, USART_FLAG_TC)==RESET); 
		USART_SendData(UART4 ,*s++);//发送当前字符
	}
}

extern void CLR_Buf(void);

void UART4_Send_Command(char* s)
{
	CLR_Buf(); //清空接收数据的buffer
	UART4_SendString(s); //发出字符串
	UART4_SendString("\r\n"); //再自动发送 \r\n两个字符
}

/*******************************************************************************
* 函数名 : Second_AT_Command
* 描述   : 发送AT指令函数
* 输入   : 发送数据的指针、希望收到的应答、发送等待时间(单位：20ms)
* 输出   : 
* 返回   : 
* 注意   : 
*******************************************************************************/
extern u8 Find(char *a);

u8 UART4_Send_AT_Command(char *b,char *a,u8 wait_time,u16 interval_time)         
{
	u8 i;
	i = 0;
	while(i < wait_time)    //如果没有找到 就继续再发一次指令 再进行查找目标字符串                
	{
		UART4_Send_Command(b);//串口2发送 b 字符串 他会自动发送\r\n  相当于发送了一个指令
		for(i = 0;i < 20;i++)   
		{
			delay_ms(interval_time); //等待一定时间 传50的话就是 50*20ms = 1秒
		}
		
		if(Find(a))            //查找需要应答的字符串 a
		{
			return 1;
		}
		i++;
	}
	
	return 0;
}

void UART4_Send_Command_END(char* s)
{
	CLR_Buf(); //清空接收数据的buffer
	UART4_SendString(s); //发出字符串
}

u8 UART4_Send_AT_Command_End(char *b,char *a,u8 wait_time,u32 interval_time)         
{
	u8 i;
	i = 0;
	while(i < wait_time)    //如果没有找到 就继续再发一次指令 再进行查找目标字符串                
	{
		UART4_Send_Command_END(b);//串口4发送 b 字符串 这里不发送\r\n 
		delay_ms(interval_time); //等待一定时间 传50的话就是 50*20ms = 1秒
		if(Find(a))            //查找需要应答的字符串 a
		{
			return 1;
		}
		i++;
	}
	
	return 0;
}


