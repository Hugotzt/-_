#include "GA6_module.h"
#include "uart4.h"
#include "sys.h"
#include "delay.h"  
//#include "includes.h"
//#include "common.h"
#include "oled.h"

char *phone_num = "ATD13777092608;"; //拨打电话，修改这里可以修改拨打的电话。

//char *phone_num = "ATD17764775363;"; //拨打电话，修改这里可以修改拨打的电话。
char *phone_num_message = "AT+CMGS=\"13777092608\""; //拨打电话，修改这里可以修改拨打的电话。

/*************  本地变量声明	**************/
char Uart4_Buf[Buf4_Max];//串口2接收缓存
u8 First_Int = 0;
char error_result[20];




void UART4_IRQHandler(void)                	
{
	u8 Res=0;
#if SYSTEM_SUPPORT_OS 		//如果SYSTEM_SUPPORT_OS为真，则需要支持OS.
	OSIntEnter();    
#endif	
	if(USART_GetITStatus(UART4, USART_IT_RXNE) != RESET) //接收到数据
	{	 
		Res = USART_ReceiveData(UART4);
		Uart4_Buf[First_Int] = Res;  	  //将接收到的字符串存到缓存中
		First_Int++;                	  //缓存指针向后移动
		if(First_Int > Buf4_Max)       	  //如果缓存满,将缓存指针指向缓存的首地址
		{
			First_Int = 0;
		} 
	} 
	if(USART_GetFlagStatus(UART4,USART_FLAG_ORE) != RESET) // 检查 ORE 标志
  {
      USART_ClearFlag(UART4,USART_FLAG_ORE);
      USART_ReceiveData(UART4);
  }
	
#if SYSTEM_SUPPORT_OS 	//如果SYSTEM_SUPPORT_OS为真，则需要支持OS.
	OSIntExit();  											 
#endif		
} 



void CLR_Buf(void)
{
	u16 k;
	for(k=0;k<Buf4_Max;k++)      //将缓存内容清零
	{
		Uart4_Buf[k] = 0x00;
	}
    First_Int = 0;              //接收字符串的起始存储位置
}
/***************************************************************
注：当然你可以返回其他值，来确定到底是哪一步发送指令出现失败了。
****************************************************************/
int send_text_message(char *content)
{
	u8 ret;
	char end_char[2];
	
	end_char[0] = 0x1A;//结束字符
	end_char[1] = '\0';
	
//	//设置存储位置
//	ret = UART4_Send_AT_Command("AT+CPMS=\"SM\",\"ME\",\"SM\"","OK",3,100);
//	if(ret == 0)
//	{
//		return AT_CPMS_ERROR;
//	}
	
	ret = UART4_Send_AT_Command("AT+CMGF=1","OK",3,50);//配置为TEXT模式
	if(ret == 0)
	{
		return AT_CMGF_ERROR;
	}
	
	ret = UART4_Send_AT_Command("AT+CSCS=\"GSM\"","OK",3,50);//设置字符格式
	if(ret == 0)
	{
		return AT_CSCS_ERROR;
	}
	
	ret = UART4_Send_AT_Command(phone_num_message,">",3,50);//输入收信人的电话号码
	if(ret == 0)
	{
		return AT_CMGS_ERROR;
	}
	
	UART4_SendString(content); 
	ret = UART4_Send_AT_Command_End(end_char,"OK",1,350);//发送结束符，等待返回ok,等待5S发一次，因为短信成功发送的状态时间比较长
	if(ret == 0)
	{
		return END_CHAR_ERROR;
	}
	
	return 1;
}
int send_message_num(char *message) 
{
	int ret;
	
	ret = UART4_Send_AT_Command("AT","OK",3,50);//测试通信是否成功
	if(ret == 0)
	{
		return COMMUNITE_ERROR;
	}
	
	ret = UART4_Send_AT_Command("AT+CPIN?","READY",2,50);//查询卡是否插上
	if(ret == 0)
	{
		return NO_SIM_CARD_ERROR;
	}
	
	ret = Wait_CREG(3);//查询卡是否注册到网络
	if(ret == 0)
	{	
		return SIM_CARD_NO_REG_ERROR;
	}
	
	ret = send_text_message(message);//发送TEXT短信
	if(ret != 1)
	{		
		return MESSAGE_ERROR; 
	}
	
	return 1;
}

int call_phone_num(void) 
{
	int ret;
	
	ret = UART4_Send_AT_Command("AT","OK",3,50);//测试通信是否成功
	OLED_ShowNum(10,2,ret,3,16);   
	if(ret == 0)
	{
		return COMMUNITE_ERROR;
	}
	
	ret = UART4_Send_AT_Command("AT+CPIN?","READY",2,50);//查询卡是否插上
	if(ret == 0)
	{
		return NO_SIM_CARD_ERROR;
	}
	
	ret = Wait_CREG(3);//查询卡是否注册到网络
	if(ret == 0)
	{
		return SIM_CARD_NO_REG_ERROR;
	}
	
	
	ret = UART4_Send_AT_Command(phone_num,"OK",2,50);//拨号
	if(ret == 0)
	{
		return CALL_ERROR;
	}
	
	return 1;
}

int redail_phone_num(void)
{
	int ret;
	int i;
	ret = UART4_Send_AT_Command("AT+DLST","OK",3,50);//重拨 如果返回OK 说明指令发送成功 不代表正在呼叫
	
	if(ret == 0)
	{
		return AT_DLST_OK_ERROR;
	}
	for(i = 0;i < 20;i++)   //再等待2秒  不然拨号的时候的串口信息没法收上来
	{
		delay_ms(100);
	} 
    if(Find("\"CALL\",1") == 0)
    {
        return AT_DLST_CALL_ERROR;
    }
    
    return 1;
}


/*******************************************************************************
* 函数名 : Wait_CREG
* 描述   : 等待模块注册成功
* 输入   : 
* 输出   : 
* 返回   : 
* 注意   : 
*******************************************************************************/
u8 Wait_CREG(u8 query_times)
{
	u8 i;
	u8 k;
	u8 j;
	i = 0;
	CLR_Buf();
	while(i == 0)        			
	{

		UART4_Send_Command("AT+CREG?");  //发送查询是否注册网络的函数
		for(i = 0;i < 20;i++)   
		{
			delay_ms(100);
		}

        //如果查找到" +CREG: 1,1"说明卡成功注册，查找方法就是下面的for循环
		for(k=0;k<Buf4_Max;k++)      			
		{
			if((Uart4_Buf[k] == '+')&&(Uart4_Buf[k+1] == 'C')&&(Uart4_Buf[k+2] == 'R')&&(Uart4_Buf[k+3] == 'E')&&(Uart4_Buf[k+4] == 'G')&&(Uart4_Buf[k+5] == ':'))
			{
					 
				if((Uart4_Buf[k+7] == '0')&&((Uart4_Buf[k+9] == '1')||(Uart4_Buf[k+9] == '5')))
				{
					i = 1;
					return 1;  //如果查到就直接返回 查询不到的话 继续再查询 总共查询 query_times 多次
				}
				
			}
		}
		j++;
		if(j > query_times)
		{
			return 0;
		}
		
	}
	return 0;
}

/*******************************************************************************
* 函数名 : Find
* 描述   : 判断缓存中是否含有指定的字符串
* 输入   : 
* 输出   : 
* 返回   : unsigned char:1 找到指定字符，0 未找到指定字符 
* 注意   : 
*******************************************************************************/

u8 Find(char *a)
{ 
	if(strstr(Uart4_Buf, a)!=NULL) //Uart4_Buf接收返回数据的buffer strstr函数是库函数，这个函数可以判定Uart4_Buf里面是否又a字符串
	{
		return 1;
	}	
	else
	{
		return 0;
	}
		
}






