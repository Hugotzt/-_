#ifndef GA6_MODULE_
#define GA6_MODULE_
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
#define AT_DLST_OK_ERROR      -40  //从-40开始的是属于 GA6 模块新添加的错误代码
#define AT_DLST_CALL_ERROR    -41
#define MESSAGE_ERROR    -42
extern char *phone_num;
/*************	本地函数声明	**************/
void CLR_Buf(void);     //清除串口4接收缓存
u8 Wait_CREG(u8 query_times);    //等待模块注册成功
u8 Find(char *a);
int call_phone_num(void);
int send_message_num(char *message);
#endif

