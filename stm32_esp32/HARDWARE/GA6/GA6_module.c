#include "GA6_module.h"
#include "uart4.h"
#include "sys.h"
#include "delay.h"  
//#include "includes.h"
//#include "common.h"
#include "oled.h"

char *phone_num = "ATD13777092608;"; //����绰���޸���������޸Ĳ���ĵ绰��

//char *phone_num = "ATD17764775363;"; //����绰���޸���������޸Ĳ���ĵ绰��
char *phone_num_message = "AT+CMGS=\"13777092608\""; //����绰���޸���������޸Ĳ���ĵ绰��

/*************  ���ر�������	**************/
char Uart4_Buf[Buf4_Max];//����2���ջ���
u8 First_Int = 0;
char error_result[20];




void UART4_IRQHandler(void)                	
{
	u8 Res=0;
#if SYSTEM_SUPPORT_OS 		//���SYSTEM_SUPPORT_OSΪ�棬����Ҫ֧��OS.
	OSIntEnter();    
#endif	
	if(USART_GetITStatus(UART4, USART_IT_RXNE) != RESET) //���յ�����
	{	 
		Res = USART_ReceiveData(UART4);
		Uart4_Buf[First_Int] = Res;  	  //�����յ����ַ����浽������
		First_Int++;                	  //����ָ������ƶ�
		if(First_Int > Buf4_Max)       	  //���������,������ָ��ָ�򻺴���׵�ַ
		{
			First_Int = 0;
		} 
	} 
	if(USART_GetFlagStatus(UART4,USART_FLAG_ORE) != RESET) // ��� ORE ��־
  {
      USART_ClearFlag(UART4,USART_FLAG_ORE);
      USART_ReceiveData(UART4);
  }
	
#if SYSTEM_SUPPORT_OS 	//���SYSTEM_SUPPORT_OSΪ�棬����Ҫ֧��OS.
	OSIntExit();  											 
#endif		
} 



void CLR_Buf(void)
{
	u16 k;
	for(k=0;k<Buf4_Max;k++)      //��������������
	{
		Uart4_Buf[k] = 0x00;
	}
    First_Int = 0;              //�����ַ�������ʼ�洢λ��
}
/***************************************************************
ע����Ȼ����Է�������ֵ����ȷ����������һ������ָ�����ʧ���ˡ�
****************************************************************/
int send_text_message(char *content)
{
	u8 ret;
	char end_char[2];
	
	end_char[0] = 0x1A;//�����ַ�
	end_char[1] = '\0';
	
//	//���ô洢λ��
//	ret = UART4_Send_AT_Command("AT+CPMS=\"SM\",\"ME\",\"SM\"","OK",3,100);
//	if(ret == 0)
//	{
//		return AT_CPMS_ERROR;
//	}
	
	ret = UART4_Send_AT_Command("AT+CMGF=1","OK",3,50);//����ΪTEXTģʽ
	if(ret == 0)
	{
		return AT_CMGF_ERROR;
	}
	
	ret = UART4_Send_AT_Command("AT+CSCS=\"GSM\"","OK",3,50);//�����ַ���ʽ
	if(ret == 0)
	{
		return AT_CSCS_ERROR;
	}
	
	ret = UART4_Send_AT_Command(phone_num_message,">",3,50);//���������˵ĵ绰����
	if(ret == 0)
	{
		return AT_CMGS_ERROR;
	}
	
	UART4_SendString(content); 
	ret = UART4_Send_AT_Command_End(end_char,"OK",1,350);//���ͽ��������ȴ�����ok,�ȴ�5S��һ�Σ���Ϊ���ųɹ����͵�״̬ʱ��Ƚϳ�
	if(ret == 0)
	{
		return END_CHAR_ERROR;
	}
	
	return 1;
}
int send_message_num(char *message) 
{
	int ret;
	
	ret = UART4_Send_AT_Command("AT","OK",3,50);//����ͨ���Ƿ�ɹ�
	if(ret == 0)
	{
		return COMMUNITE_ERROR;
	}
	
	ret = UART4_Send_AT_Command("AT+CPIN?","READY",2,50);//��ѯ���Ƿ����
	if(ret == 0)
	{
		return NO_SIM_CARD_ERROR;
	}
	
	ret = Wait_CREG(3);//��ѯ���Ƿ�ע�ᵽ����
	if(ret == 0)
	{	
		return SIM_CARD_NO_REG_ERROR;
	}
	
	ret = send_text_message(message);//����TEXT����
	if(ret != 1)
	{		
		return MESSAGE_ERROR; 
	}
	
	return 1;
}

int call_phone_num(void) 
{
	int ret;
	
	ret = UART4_Send_AT_Command("AT","OK",3,50);//����ͨ���Ƿ�ɹ�
	OLED_ShowNum(10,2,ret,3,16);   
	if(ret == 0)
	{
		return COMMUNITE_ERROR;
	}
	
	ret = UART4_Send_AT_Command("AT+CPIN?","READY",2,50);//��ѯ���Ƿ����
	if(ret == 0)
	{
		return NO_SIM_CARD_ERROR;
	}
	
	ret = Wait_CREG(3);//��ѯ���Ƿ�ע�ᵽ����
	if(ret == 0)
	{
		return SIM_CARD_NO_REG_ERROR;
	}
	
	
	ret = UART4_Send_AT_Command(phone_num,"OK",2,50);//����
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
	ret = UART4_Send_AT_Command("AT+DLST","OK",3,50);//�ز� �������OK ˵��ָ��ͳɹ� ���������ں���
	
	if(ret == 0)
	{
		return AT_DLST_OK_ERROR;
	}
	for(i = 0;i < 20;i++)   //�ٵȴ�2��  ��Ȼ���ŵ�ʱ��Ĵ�����Ϣû��������
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
* ������ : Wait_CREG
* ����   : �ȴ�ģ��ע��ɹ�
* ����   : 
* ���   : 
* ����   : 
* ע��   : 
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

		UART4_Send_Command("AT+CREG?");  //���Ͳ�ѯ�Ƿ�ע������ĺ���
		for(i = 0;i < 20;i++)   
		{
			delay_ms(100);
		}

        //������ҵ�" +CREG: 1,1"˵�����ɹ�ע�ᣬ���ҷ������������forѭ��
		for(k=0;k<Buf4_Max;k++)      			
		{
			if((Uart4_Buf[k] == '+')&&(Uart4_Buf[k+1] == 'C')&&(Uart4_Buf[k+2] == 'R')&&(Uart4_Buf[k+3] == 'E')&&(Uart4_Buf[k+4] == 'G')&&(Uart4_Buf[k+5] == ':'))
			{
					 
				if((Uart4_Buf[k+7] == '0')&&((Uart4_Buf[k+9] == '1')||(Uart4_Buf[k+9] == '5')))
				{
					i = 1;
					return 1;  //����鵽��ֱ�ӷ��� ��ѯ�����Ļ� �����ٲ�ѯ �ܹ���ѯ query_times ���
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
* ������ : Find
* ����   : �жϻ������Ƿ���ָ�����ַ���
* ����   : 
* ���   : 
* ����   : unsigned char:1 �ҵ�ָ���ַ���0 δ�ҵ�ָ���ַ� 
* ע��   : 
*******************************************************************************/

u8 Find(char *a)
{ 
	if(strstr(Uart4_Buf, a)!=NULL) //Uart4_Buf���շ������ݵ�buffer strstr�����ǿ⺯����������������ж�Uart4_Buf�����Ƿ���a�ַ���
	{
		return 1;
	}	
	else
	{
		return 0;
	}
		
}






