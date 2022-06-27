#ifndef __SIM900A_H
#define __SIM900A_H
#include "sys.h" 


#define COMMUNITE_ERROR       -1 
#define NO_SIM_CARD_ERROR     -2
#define SIM_CARD_NO_REG_ERROR -3
#define CALL_ERROR			  -4
#define AT_CPMS_ERROR		  -5
#define AT_CMGF_ERROR		  -6
#define AT_CSCS_ERROR		  -7
#define AT_CMGS_ERROR         -8
#define END_CHAR_ERROR		  -9
#define AT_CSMP_ERROR		  -10

#define AT_DLST_OK_ERROR      -21  //从-40开始的是属于 SIM900A 模块新添加的错误代码
#define AT_DLST_CALL_ERROR    -22
#define MESSAGE_ERROR    -22

#define MESSAGE_MODE_SET_ERROR    -37
#define MESSAGE_SET_UCS2_ERROR    -38
#define SET_MESSAGE_TEXT_MODE_ERROR    -39
#define CHINESE_MESSAGE_ERROR    -40

extern char *phone_num;
/*************	本地函数声明	**************/
void CLR_Buf(void);     //清除串口4接收缓存
u8 Wait_CREG(u8 query_times);    //等待模块注册成功
u8 Find(char *a);
int simCard_test(void);
int call_phone_num(void);
int send_message_num(char *message);

int send_message_chinese(char *message);
int send_chinese_text_message(char *content);
void sim900a_unigbk_exchange(u8 *src,u8 *dst,u8 mode);

#endif

