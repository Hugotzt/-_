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
#define AT_DLST_OK_ERROR      -40  //��-40��ʼ�������� GA6 ģ������ӵĴ������
#define AT_DLST_CALL_ERROR    -41
#define MESSAGE_ERROR    -42
extern char *phone_num;
/*************	���غ�������	**************/
void CLR_Buf(void);     //�������4���ջ���
u8 Wait_CREG(u8 query_times);    //�ȴ�ģ��ע��ɹ�
u8 Find(char *a);
int call_phone_num(void);
int send_message_num(char *message);
#endif

