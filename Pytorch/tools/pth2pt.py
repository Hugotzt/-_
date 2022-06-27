import torch
from torch.utils.mobile_optimizer import optimize_for_mobile
from model_v3 import mobilenet_v3_large

model = mobilenet_v3_large(num_classes=28)           # 实例化模型
model.load_state_dict(torch.load("model/MobileNetV3.pth"))  # 将参数载入到模型
model.eval()                                                # 将模型设为验证模式
example = torch.rand(1, 3, 224, 224)                        # 输入样例为224*224
traced_script_module = torch.jit.trace(model, example)      
optimized_traced_model = optimize_for_mobile(traced_script_module)  
optimized_traced_model._save_for_lite_interpreter("model/mtest5.0.pt")
