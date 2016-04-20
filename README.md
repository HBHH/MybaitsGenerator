# MybaitsGenerator
MybaitsGenerator
通过工具生成Mybaits DAO、service、serviceImp、xml文件
使用方法
database.properties 修改数据库连接信息

DataBaseUtilRepair  下根据实际情况修改（ DAO、service、serviceImp、xml 首先建立对应包结构）

String modelPackage = "com.test";    model类包路径

String daoPackage = "com.test";      dao类路径

String daoImplPackage = "com.test";  daoImpl类路径

String servicePackage = "com.test";  service类路径

String serviceImplPackage = "com.test"; serviceImpl类路径

String tableNames = "xds_boxer_starwish_info"; 表名

String filterStr = "xds_"; 去除前缀 

// model xml daoImpl dao serviceImpl service
boolean[] fileBooleanArr = new boolean[] { true, true, true, true, false, false };
                                           model  xml   dao  daoimpl service serviceImpl
                                         
                                         

   
   
