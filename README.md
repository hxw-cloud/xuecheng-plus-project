# xuecheng-plus-project

学成在线项目是一个B2B2C的在线教育平台，培训机构入驻平台后可以发布课程供用户在线学习。项目包括门户、个人学习中心、教学机构管理平台、运营平台、社交系统、系统管理6部分。



# 项目介绍



---

## 项目架构



项目采用前后端分离的技术架构，使用Spring Cloud技术栈构建微服务，数据库采用MySQL，还使用了Nacos、Spring Cloud Gateway、Redis、RabbitMQ、Elasticsearch、XXL-Job等技术。



## 使用的微服务技术



所有微服务基于Spring Boot构建，分为控制层、模型层、业务层。



1. 控制层

   >  提供与前端的HTTP接口实现。
   >
   >  Spring MVC、Spring Security Oauth2 、Swagger

2. 模型层

   > 提供PO类、DTO类统一管理。

3. 业务层

   > 包括业务Service与MyBatis Mapper。
   >
   > 使用MyBatis-Plus框架实现Mapper开发。
   >
   > 业务Service调用Mapper完成数据持久化。
   >
   > 全部bean被Spring进行管理。
   >
   > 基于Spring进行本地数据库事务控制。
   >
   > 使用XXL-JOB完成任务调度。
   >
   > RabbitTemplate和消息队列通信。
   >
   > 搜索服务基于Elasticsearch构建。

4. 配置文件由Nacos统一管理。

5. 微服务远程调用使用Feign实现。

6. 服务注册中心使用Nacos实现。

7. 使用Spring Cloud Gateway实现网关统一路由。



