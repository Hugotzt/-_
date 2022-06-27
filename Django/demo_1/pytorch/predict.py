import json
import torch
import matplotlib.pyplot as plt

from PIL import Image
from torchvision import transforms
from demo_1.pytorch.model_v3 import mobilenet_v3_large

plt.rcParams['font.sans-serif'] = ['SimHei']  
plt.rcParams['axes.unicode_minus'] = False  

device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")

def prdict(img_path):
    print('开始预测')
    #数据
    data_transform = transforms.Compose(
        [transforms.Resize(256),
            transforms.CenterCrop(224),
            transforms.ToTensor(),
            transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])])
    img = Image.open(img_path)
    # 数据与预处理
    img = data_transform(img)
    # 组合为3*1的维度
    img = torch.unsqueeze(img, dim=0)

    # 加载数据的json 
    json_path = 'demo_1/pytorch/class_indices.json'
    json_file = open(json_path, "r")
    class_indict = json.load(json_file)

    # 模型
    model = mobilenet_v3_large(num_classes=28).to(device)
    model_weight_path = "demo_1/pytorch/MobileNetV3.pth"
    model.load_state_dict(torch.load(model_weight_path, map_location=device))
    # 预测
    model.eval()
    # 停止预测
    with torch.no_grad():
        #  得到返回结果
        output = torch.squeeze(model(img.to(device))).cpu()
        predict = torch.softmax(output, dim=0)
        predict_cla = torch.argmax(predict).numpy()

    print_res =  "fileName:{}class:{}".format(img_path,class_indict[str(predict_cla)])
    # predict[predict_cla].numpy()
    print(print_res)
    return class_indict[str(predict_cla)]
    

