# -*- coding: utf-8 -*-
"""
# @file name  : cifar10_dataset.py
# @author     : TingsongYu https://github.com/TingsongYu
# @date       : 2020-02-29
# @brief      : 构建cifar10 dataset
"""
import os
from PIL import Image
from matplotlib.font_manager import json_load
from torch.utils.data import Dataset
import json

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

class InsectDataset(Dataset):
    def __init__(self, ann_dir, transform=None):
        assert (os.path.exists(ann_dir)), "ann_dir:{} 不存在！".format(ann_dir)
        self.ann_dir = ann_dir           # 图片信息及路径
        self._get_img_info()             # 整合所有图片信息
        self.transform = transform       # 图像增强

    def __getitem__(self, index):
        fn,label = self.img_info[index]
        img = Image.open(fn).convert('RGB')
        if self.transform is not None:
            img = self.transform(img)

        return img,label

    def __len__(self):
        if len(self.img_info) == 0:
            raise Exception("未获取任何图片路径，请检查dataset及文件路径！")
        return len(self.img_info)

    def _get_img_info(self):
        # 获取所有文件名
        with open(self.ann_dir,'r',encoding='GBK') as f:
            data = json.load(f)
        data = data['data']
        self.img_info=[]
        for sur in data:
            path_img = [(sur['name'],sur['label'])]
            self.img_info.extend(path_img)
        
            