import numpy as np
import PIL.Image
import matplotlib.pyplot as plt
from io import BytesIO
import os
import datetime

from django.shortcuts import render,HttpResponse
from demo_1.models import sub , insect, admin_insect
from demo_1.pytorch.predict import prdict

plt.rcParams['font.sans-serif'] = ['SimHei']  # 用来正常显示中文标签
plt.rcParams['axes.unicode_minus'] = False  # 用来正常显示负号

BASE_DIR = os.path.join('static','images')

def draw_time(result,file_path):
    # 时间作为x
    x = result.keys()
    # 每天所检测的昆虫数
    y = result.values()

    plt.figure(figsize=(15,8),dpi=150)
    plt.plot(x,y,linestyle='--') 
    plt.xlabel('时间')
    plt.ylabel('昆虫数量')
    plt.title('每日害虫数量')
    plt.savefig(file_path)

def draw_insect(result,file_path):
    name = list(result.keys())    
    y = result.values()
    plt.figure(figsize=(15,8),dpi=150)
    plt.bar(range(len(y)),y,tick_label=name)
    plt.xlabel('昆虫')
    plt.ylabel('昆虫数量')
    plt.title('昆虫数量总计')
    plt.savefig(file_path)

def view(request):
    return render(request,'view_function.html')

def show_prd(request):
    return render(request, 'prd.html')

def show_user(request):
    data_list  = list(insect.objects.all().values())
    
    new_list = []
    for data in data_list:
        file_name = os.path.join('image_db',str(data['id']) + '.jpg')
        
        img_path = os.path.join(BASE_DIR,file_name)
        file_img = BytesIO(data['picture'])
        img=PIL.Image.open(file_img)
        data['imgpath'] = file_name
        new_list.append(data)

        with open(img_path, 'wb') as f:
            img.save(f)
    data_list = new_list
    return render(request, 'results.html',locals())

def show_admin(request):
    data_list  = list(admin_insect.objects.all().values())
     
    return render(request, 'results.html',locals())


def show_analysis(request):
    user_data = insect.objects.all()
    admin_data = admin_insect.objects.all()
    user_list = [ [data.label,data.publishertime] for data in user_data]
    admin_list = [ [data.label,data.publishertime] for data in admin_data]  
    user_list.extend(admin_list)

    # 时间与数量
    datetime = np.array(user_list)[:,1]
    result_time = {}
    for data in datetime:
        if data in result_time.keys():
            result_time[data] += 1
        else: 
            result_time[data] = 1 
    time_path = os.path.join(BASE_DIR,'image_ana','time.png')
    draw_time(result_time,time_path)
    
    # 昆虫与数量
    lables = np.array(user_list)[:,0]
    result_labels = {}
    for label in lables:
        if label in result_labels.keys():
            result_labels[label] += 1 
        else:
            result_labels[label]  = 1
    in_path = os.path.join(BASE_DIR,'image_ana','insect.png')
    draw_insect(result_labels,in_path)

    images = {'time':time_path,'insect':in_path}
    return render(request, 'analysis.html',images)

def show_fb(request):
    data_list = sub.objects.all() 
    return render(request,'fb.html',locals())
    
def prd(request):
   
        img = request.FILES.get('img')
        # 获取图片的全文件名
        img_name = img.name
        print(img_name)
        # 截取文件后缀和文件名
        mobile = os.path.splitext(img_name)[0]
        ext = os.path.splitext(img_name)[1]
        # 重定义文件名
        file_name = f'insect-{mobile}{ext}'
        # 从配置文件中载入图片保存路径
        path = os.path.join('image_uploads', file_name)
        img_path = os.path.join(BASE_DIR, path)
        
        # 写入文件
        with open(img_path, 'ab') as f:
            # 如果上传的图片非常大，就通过chunks()方法分割成多个片段来上传
            for chunk in img.chunks():
                f.write(chunk) 
        
        label = prdict(img_path)
        publishertime = datetime.datetime.now().strftime('%Y-%m-%d')
        # 保存至数据库
        admin_insect.objects.create(place='admin',label=label,publishertime=publishertime,imgpath=path)
        message = '预测成功,结果为{}\n保存至数据库'.format(label)
        return HttpResponse(message)
    # except:
    #     message = '失败，请重新载入图片'
    #     return HttpResponse(message) 

