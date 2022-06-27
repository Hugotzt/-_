"""python_django URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/4.0/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""

from django.contrib import admin
from django.urls import path
from demo_1 import view_log, view_function
from python_django.settings import MEDIA_ROOT,MEDIA_URL
from django.conf.urls import url # 设置主页



urlpatterns = [
    path('admin/', admin.site.urls),
    url(r'^$', view_log.main),          # 设置主页

    path('showlog/',view_log.show_log,name='show_log'),
    path('log/',view_log.log,name='log'),
    
    path('showregister/',view_log.show_register,name='show_register'),
    path('register/',view_log.register,name='register'),

    path('view/',view_function.view,name='view'),
    
    path('showprd/',view_function.show_prd,name='show_prd'),
    path('prd/',view_function.prd,name='prd'),

    path('showresults/',view_function.show_user,name='show_results'),
    path('showuser/',view_function.show_user,name='user'),
    path('showadim/',view_function.show_admin,name='admin'),
    
    path('showanalysis/',view_function.show_analysis,name='show_analysis'),
    
    path('showfb/',view_function.show_fb,name='show_fb'),
]
