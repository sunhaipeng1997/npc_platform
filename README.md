## 母婴产品推广系统服务端

为卖母婴产品的生产商开发的产品推广系统的后端部分。前端是H5网页，与微信公众号结合使用。前端使用Vue和Bootstrap框架，后端使用SpringBoot + Jpa，数据库使用mysql，项目构建工具为Maven。

### 系统架构

整个后端系统是Maven多模块项目，各个子模块及其功能如下：

1. server：表现层和业务层。表现层分为两个package（controller和api）分别处理来自后台管理前端和用户前端的请求。
2. repository：持久层。
3. domain：全局实体类。
4. util：全局工具类