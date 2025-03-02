# mianshi-assistant
## 面试助手（mianshi-assistant）是一个前后端分离的辅助面试网站，可以帮助你高效地准备面试
### 后端
基于 Spring Boot + MySQL + Redis + ElasticSearch（分词搜索） + 阿里云对象存储实现。</br>
使用了 Druid 数据库连接池（后台监控） + HotKey（热key自动发现缓存） + Sentinel、Nacos（提升系统安全性） + Satoken（同端登录检测）进行全面优化。</br></br></br>

### 前端
基于 React + Ant Design + Next.js（服务端渲染）。</br>
使用了 ESLint + Prettier + TS 保证代码质量，使用 umi.js 生成请求代码简化开发。</br></br></br>

### 效果展示</br>
#### 主页:</br>
![image](https://wyc-mianshi-assistant.oss-cn-shenzhen.aliyuncs.com/present/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-03-02%20164815.png)
#### 题目列表:</br>
![image](https://wyc-mianshi-assistant.oss-cn-shenzhen.aliyuncs.com/present/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-03-02%20164855.png)
#### 题目详情:</br>
![image](https://wyc-mianshi-assistant.oss-cn-shenzhen.aliyuncs.com/present/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-03-02%20165000.png)
#### 管理页面:</br>
![image](https://wyc-mianshi-assistant.oss-cn-shenzhen.aliyuncs.com/present/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-03-02%20165047.png)</br></br></br>


### 其他</br>
#### 项目相关依赖和说明：
MySQL版本： 8.0.33.0</br>
Redis版本：5.0.14</br>
Nacos Server版本：2.2.0</br>
Elasticsearch + Kibana版本：7.17.27</br>
Hotkey: v0.0.4</br>
其余依赖可以在 pom.xml 中查看
</br></br></br>

### 最后</br>
项目的文档后续还将进一步完善，在项目里新增的功能也会尽快做出相关说明。
