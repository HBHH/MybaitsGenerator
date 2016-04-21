# MybaitsGenerator
MybaitsGenerator

Mybaits配置文件生成工具

该工具生成 DAO、service、serviceImp、xml文件

使用方法

注意：生成DAO、service、serviceImp、xml 的package路径是不能自动建立要先手动建立否则不能生成

database.properties  修改数据库连接信息（暂时支持mysql，其他数据库稍稍改造一下就可以了）

DataBaseUtilRepair  工具主要类

String modelPackage = "com.test";    model类包路径

String daoPackage = "com.test";      dao类路径

String daoImplPackage = "com.test";  daoImpl类路径

String servicePackage = "com.test";  service类路径

String serviceImplPackage = "com.test"; serviceImpl类路径

String tableNames = "xds_boxer_starwish_info"; 表名

String filterStr = "xds_"; 去除前缀 

boolean[] fileBooleanArr = new boolean[] { true, true, true, true, false, false };

true 表示生成文件，false 反之

fileBooleanArr[0]    model   

fileBooleanArr[1]    xml 

fileBooleanArr[2]    dao 

fileBooleanArr[3]    daoimpl 

fileBooleanArr[4]    service 

fileBooleanArr[5]    serviceImpl 
                                         
                                         

   
   
