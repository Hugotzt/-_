from django.db import models

class sub(models.Model):
    id = models.BigAutoField(primary_key=True)  # 主键
    theme = models.CharField(max_length=20,null=False)    # 主题
    sug = models.CharField(max_length=200,null=False)      # 建议
    phone = models.CharField(max_length=11,null=False)    # 手机
    time = models.CharField(max_length=50,null=False)     # 时间

    class Meta:
        db_table =  'sub'

class insect(models.Model):
    id = models.BigAutoField(primary_key=True)         # 主键
    place = models.CharField(max_length=50,null=False) # 预测来源
    label = models.CharField(max_length=50,null=False) # 标签
    publishertime = models.CharField (max_length=50,null=False) # 时间
    picture = models.BinaryField(null=False)           # 图片
    
    class Meta:
        db_table =  'insect'

class card(models.Model):
    card_id = models.CharField(primary_key=True,max_length=10)
    name = models.CharField(max_length=10,null=False)
    
    class Meta:
        db_table = 'card'


class admin_insect(models.Model):
    id = models.BigAutoField(primary_key=True)         # 主键
    place = models.CharField(max_length=50,null=False) # 预测来源
    label = models.CharField(max_length=50,null=False) # 标签
    publishertime = models.CharField (max_length=50,null=False) # 时间
    imgpath = models.CharField(max_length=100,null=False)          # 图片路径
    class Meta:
        db_table =  'admin_insect'