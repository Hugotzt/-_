#ifndef __UART4_H
#define __UART4_H

#include "sys.h" 

#define Buf4_Max 	  100 //����2���泤��
#define STABLE_TIMES  50  //�ȴ�ϵͳ�ϵ����ȶ�


void uart4_init(u32 bound);
void UART4_SendString(char* s);
void UART4_Send_Command(char* s);
u8 UART4_Send_AT_Command(char *b,char *a,u8 wait_time,u16 interval_time) ;  

void UART4_Send_Command_END(char* s);

u8 UART4_Send_AT_Command_End(char *b,char *a,u8 wait_time,u32 interval_time) ; 
#endif  /* __UART4_H */


