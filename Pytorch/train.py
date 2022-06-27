import os
import json
import torch
import matplotlib.pyplot as plt
import torch.nn as nn
import torch.optim as optim
from torchvision import transforms, datasets
from torch.utils.data import DataLoader
from tqdm import tqdm
from tools.model_v3 import mobilenet_v3_large

plt.switch_backend('agg')

def plot_line(plt_dict,mode,out_dir):
    plt.plot(plt_dict['epoch'],plt_dict[mode])
    plt.ylabel(mode)
    plt.xlabel('epoch')
    plt.title(mode)
    plt.savefig(os.path.join(out_dir,mode+'.png'))
    plt.close()

# 模型保存路径
save_path = 'model/MobileNetV3.pth'

# 超参
batch_size = 16     
epochs = 120      
n_classes = 28    
lr = 0.001  
nw = 0
milestones = [epochs * 3 // 4, epochs * 7 // 8]  # 学习率下降
# GPU or CUP
device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
print("using {} device.".format(device))
# 预训练模型
model_weight_path = "model/mobilenet_v3.pth"
# 图片路径
image_path = os.path.join("./dataset/images")


# ============================ step 1/5 数据 ============================
data_transform = {
    # 训练部分
    "train": transforms.Compose([transforms.RandomResizedCrop(224),                                     # 随机裁剪图片至224*224*3
            transforms.RandomHorizontalFlip(p=0.5),                             # 水平翻转
            transforms.RandomVerticalFlip(p=0.5),                               # 垂直翻转  
            transforms.RandomRotation(degrees=(0,90)),                          # 旋转  
            transforms.ToTensor(),                                              # 转换为张量的形式
            transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])] # 数据标准化 逐channel的对图像进行标准化
            ),   
    # 评估部分
    "val": transforms.Compose([transforms.Resize(256),
            transforms.CenterCrop(224),
            transforms.ToTensor(),
            transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])]
            ),
                    }

# 数据容器
train_dataset = datasets.ImageFolder(root=os.path.join(image_path, "train"),transform=data_transform["train"])
vali_dataset = datasets.ImageFolder(root=os.path.join(image_path, "val"),transform=data_transform["val"])

# 保存标签
image_list = train_dataset.class_to_idx
cla_dict = dict((val, key) for key, val in image_list.items())
json_str = json.dumps(cla_dict, indent=4)
with open('class_indices.json', 'w') as json_file:
    json_file.write(json_str)

# 加载数据
train_loader = DataLoader(train_dataset,batch_size=batch_size, shuffle=True, num_workers=nw)
validate_loader = DataLoader(vali_dataset,batch_size=batch_size, shuffle=False,num_workers=nw)


train_num = len(train_dataset)
val_num = len(vali_dataset)
print("using {} images for training, {} images for validation.".format(train_num, val_num))

# ============================ step 2/5 模型 ============================ 
net = mobilenet_v3_large(num_classes=n_classes)
pre_weights = torch.load(model_weight_path, map_location=device)

# 清除预训练模型中 分类层的参数(分类数量不一致)
pre_dict = {k: v for k, v in pre_weights.items() if net.state_dict()[k].numel() == v.numel()}
# 重新加载数据
missing_keys, unexpected_keys = net.load_state_dict(pre_dict, strict=False)
# 冻结特征层的参数
for param in net.features.parameters():
    param.requires_grad = False
net.to(device)
# ============================ step 3/5 损失函数 ============================
# 损失函数 交叉熵
loss_function = nn.CrossEntropyLoss()
# ============================ step 4/5 优化器 ============================
params = [p for p in net.parameters() if p.requires_grad]
# optimizer = optim.Adam(params, lr=lr)
optimizer = optim.SGD(params, lr=lr, momentum=0.9, weight_decay=1e-4)  # 选择优化器
# 学习率下降
scheduler = optim.lr_scheduler.MultiStepLR(optimizer, gamma=0.1, milestones=milestones)
# ============================ step 5/5 训练 ============================
best_acc = 0.0
plt_dict = {'epoch':[] ,'loss':[], 'accuracy':[]}

train_steps = len(train_loader)
for epoch in range(epochs):
    # 开启训练
    net.train()
    running_loss = 0.0
    train_bar = tqdm(train_loader)

    for step, data in enumerate(train_bar):
        images, labels = data
        optimizer.zero_grad()
        logits = net(images.to(device))
        loss = loss_function(logits, labels.to(device))
        loss.backward()
        # 更新学习率
        optimizer.step()
        # 加上每一个iteration的loss 计算平均值代表整个epoch的loss
        running_loss += loss.item()
        train_bar.desc = "train epoch[{}/{}] loss:{:.3f}".format(epoch + 1, epochs, loss)

    # 开启预测
    net.eval()
    acc_val = 0.0  # accumulate accurate number / epoch
    with torch.no_grad():
        val_bar = tqdm(validate_loader)
        for val_data in val_bar:
            val_images, val_labels = val_data
            outputs = net(val_images.to(device))
            # loss = loss_function(outputs, test_labels)
            predict_y = torch.max(outputs, dim=1)[1]
            acc_val += torch.eq(predict_y, val_labels.to(device)).sum().item()
            val_bar.desc = "valid epoch[{}/{}]".format(epoch + 1, epochs)
    val_accurate = acc_val / val_num
    print('[epoch %d] train_loss: %.3f  val_accuracy: %.3f' %
            (epoch + 1, running_loss / train_steps, val_accurate))

    plt_dict['epoch'].append(epoch + 1)
    plt_dict['loss'].append(running_loss / train_steps)
    plt_dict['accuracy'].append(val_accurate)
    # loss
    plot_line(plt_dict,'loss','model')
    # accuracy
    plot_line(plt_dict,'accuracy','model')
    if val_accurate > best_acc:
        best_acc = val_accurate
        # 1、只保存权值
        torch.save(net.state_dict(), save_path)
        # 2、保存权值和结构
        # torch.save(net, save_path)

print('Finished Training')
