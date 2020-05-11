## Bloom Filter 使用及部署指南

<img src="https://i.loli.net/2020/05/06/w3SM7mYArahZCud.png" alt="avatar" style="zoom: 50%;" />



## 部署说明



### 一 、配置信息

项目基于Spring Boot开发，主要配置文件有：

|     文件名      |        功能描述        |
| :-------------: | :--------------------: |
| redis-conf.yml  | Redisson客户端连接配置 |
| application.yml |  Spring Boot系统配置   |
|   log4j2.xml    |        日志配置        |



#### 1. Redisson客户端连接配置

详情配置，详见[集群模式](https://github.com/redisson/redisson/wiki/2.-%E9%85%8D%E7%BD%AE%E6%96%B9%E6%B3%95#24-%E9%9B%86%E7%BE%A4%E6%A8%A1%E5%BC%8F) 。

Redis Cluster 部署节点信息如下图

<img src="https://i.loli.net/2020/05/06/WHjRicF9wACbk5B.png" style="zoom: 150%;" />

redis实例均部署在各节点 `/usr/local/redis` 目录下，每个节点部署两个实例，端口号分别为`7000`和`7001`。实例`7000`集群配置文件位于 `/usr/local/redis/cluster/7000/redis.conf` ， 实例`7001`同理。`/usr/local/redis/start.sh`为Redis启动文件，执行该文件会启动这两个Redis实例。

#### 2. Spring Boot系统配置

主要配置项

- `spring.datasource.driver-class-name`
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

配置MySQL连接信息。

项目成功启动后会自动创建`user` 、`user_roles`、`role` 分别表示权限管理的用户表、用户角色表、角色表。



### 二 、其他

由于项目基于Spring Boot开发，所以可使用Spring Boot启动方式启动。

例如，使用`mvn install ` 生成 Jar 包后，运行如下脚本启动：

```shell
jar_name=`ls -l | gawk '/jar$/{print $9}'`
$JAVA_HOME/bin/java  \
-Xms1g  \
-Xmx1g  \
-XX:+UseConcMarkSweepGC  \
-Dserver.port=7018 \
-jar $jar_name  > m.log &
```



## 使用说明



### 一 、权限管理模块

权限管理模块基于**Spring Security**和 [JWT](https://www.ruanyifeng.com/blog/2018/07/json_web_token-tutorial.html)实现。

#### 1. 帐号注册

用户使用Bloom Filter之前需要先调用`/authentication/register`注册帐号，接口调用方法如下

|             接口             |  方法  |                            请求体                            |
| :--------------------------: | :----: | :----------------------------------------------------------: |
| ``/authentication/register`` | `post` | `Json` 字符串，<br>其中包含如下字段 : <br>`username`字符串字段、`password`字符串字段、`roles` `JSON`对象数组字段。<br>所有字段均不能为空。`roles` `JSON`对象数组字段表示需要申请的角色权限。目前项目中使用到的角色权限如下表。 |



角色表如下

| id（数字类型） | name（字符串类型） |    描述    |
| :------------: | :----------------: | :--------: |
|       1        |    ROLE_NORMAL     |  普通角色  |
|       2        |     ROLE_ADMIN     | 管理员角色 |



如，注册普通角色权限帐号

<img src="https://i.loli.net/2020/05/06/m4bXP9JZ1fj3OkL.png" alt="avatar"  />



注册成功后，返回信息如下

<img src="https://i.loli.net/2020/05/06/Of3EHkK4jzaLVp1.png" alt="avatar"  />



#### 2. 申请 Token

Bloom Filter 所有接口调用均需要携带有相应角色权限的Token， 例如 `/contains` 接口需要普通用户权限， 如不携带普通权限Token调用则会返回如下信息

<img src="https://i.loli.net/2020/05/06/AiSaN4sjpfICrTt.png" alt="avatar"  />

用户注册帐号成功后，可调用`/authentication/login`接口申请Token， 接口如下



|          接口           |  方法  |                       请求体                       |
| :---------------------: | :----: | :------------------------------------------------: |
| `/authentication/login` | `post` | 表单数据需要填写`username`用户名以及`password`密码 |

如使用刚才注册的普通角色权限用户名申请Token

<img src="https://i.loli.net/2020/05/06/ansdDHpmqKk9Sot.png" alt="avatar"  />

成功申请到Token后，之后调用Bloom Filter接口时，需要添加`Authorization`请求头，其值为`Bearer`拼接上刚才申请的Token。如下

<img src="https://i.loli.net/2020/05/06/YMKcy28baLE74JG.png" alt="avatar"  />

 

### 二 、Bloom Filter 功能模块

该模块实现Bloom Filter主要功能，Bloom Filter分Topic，不同Topic下各个条目互不影响。Topic必须以`$s`、`$m`或者`$l`结束，用以表示待创建Bloom Filter预期规模， 解释如下

| 结束字符串 | 预期插入条目数 <br>expected Insertions | 错误概率<br>false probability | `Redis`预计占用内存 |
| :--------: | :------------------------------------: | :---------------------------: | :-----------------: |
|    `$s`    |               10_000_000               |            0.0001             |        22.9M        |
|    `$m`    |               50_000_000               |            0.00001            |        142.8        |
|    `$l`    |              100_000_000               |            0.00001            |       285.7M        |

**在满足需求情况下尽量选择小规模，节省`Redis`内存**。

该模块下各个接口返回返回统一`Json` 对象， 对象各字段说明如下

|   字段名    |  字段类型  |                             描述                             |
| :---------: | :--------: | :----------------------------------------------------------: |
|  `message`  |   字符串   |                           返回信息                           |
|  `status`   |    数字    | 状态码，自定义如下状态码<br>`401` 参数错误、`402` 请求Topic不存在、<br>`407` 请求Topic已经存在、`1`请求成功 |
| `timestamp` |   字符串   |                           请求时间                           |
|   `data`    | 由接口确定 |    接口调用返回值，接口调用成功（`status`为1时）时才有值     |
|   `total`   |    数字    |     如果`data`为数组，表示`data`数组长度，<br>否则无意义     |
|   `info`    |   字符串   |                         接口返回说明                         |

 

#### （一） 创建Bloom Filter

<table border="0" cellspacing="1" style="text-align:center">
    <tr>
        <th>接口</th>
        <td> /create </td>
    </tr>
	<tr>
        <th>方法</th>
        <td> post </td>
    </tr>
    <tr>
        <th>描述</th>
        <td>通过调用此接口创建Bloom Filter</th>
    </tr>
	<tr>
        <th>注意事项</th>
        <td>在使用Bloom Filter功能之前必须先创建</th>
    </tr>
	<tr>
        <th>需要解色权限</th>
        <td>ROLE_ADMIN</th>
    </tr>
	<tr>
        <td> <b>参数 </b></td>
        <td>
        <table style="text-align:center">
            <tr>
                <th>名称</th>
                <th>类型</th>
                <th>是否必须</th>
                <th>描述</th>
            </tr>
            <tr>
                <td>topic</td>
                <td>字符串</td>
                <td>是</td>
                <td>需要创建Bloom Filter的Topic</td>
            </tr>
        </table>
        </td>
    </tr>
	<tr>
        <td><b>可能返回的自定义状态码</b></td>
        <td>1 、401 、407</th>
    </tr>
	<tr>
        <td><b>请求成功时data字段说明</b></td>
        <td>
        <table style="text-align:center">
            <tr>
                <th>类型</th>
                <th>描述</th>
            </tr>
            <tr>
                <td>逻辑值（true 或 false）</td>
                <td>为true时表示创建成功<br>为false时表示创建失败</td>
            </tr>
        </table>
        </td>
    </tr>
</table>



**所有实例可能会用到如下两个用户**

- `geek` 用户， 该用户权限为 `ROLE_NORMAL` 和 `ROLE_ADMIN`， Token为`eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnZWVrIiwiY3JlYXRlZCI6MTU4ODgxNzI3NzQyNCwiZXhwIjoyMDIwODE3Mjc3fQ.IpmyNSIZqGk8i6lVKByjZJ9cGr3GO75oD5C67Qdfwibbjq5kFFID1RnpET6hPZLEN-iLCre92KwxkwH9KYsKNA`
- `nicholas` 用户，该用户权限为 `ROLE_NORMAL`  ， Token为`eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuaWNob2xhcyIsImNyZWF0ZWQiOjE1ODg4MTcwODE5MjgsImV4cCI6MjAyMDgxNzA4MX0.HOY6SvQFcPEi0mv_RrV7G9rBhMFzKp9BDt4eR-bXy5JMRkXSGXQAY5PUEgSgqigu3xxo9Oa6B3DskoA5Ajg-nw`

##### 实例 1 

`nicholas` 用户创建 Topic 为`twitter$s` 的Bloom Filter（该Bloom Filter之前不存在）

<img src="https://i.loli.net/2020/05/07/Q3t1LwS4IPNCOZV.png" alt="avatar"  />

**说明**：`nicholas` 用户不具备 `ROLE_ADMIN`权限。



##### 实例 2

`geek` 用户创建 Topic 为`twitter$s` 的Bloom Filter（该Bloom Filter之前不存在）

<img src="https://i.loli.net/2020/05/07/o2w3eWtuEhRBisL.png" alt="avatar"  />

**说明**：创建成功，创建成功时`info`字段返回已经创建的Bloom Filter描述`Json`字符串，该字符串各字段说明如下

|         字段名         | 字段类型 |            描述            |
| :--------------------: | :------: | :------------------------: |
|  **falseProbability**  |   数字   |          错误概率          |
|   **hashIterations**   |   数字   | 每个条目使用的哈希迭代次数 |
|        **size**        |  字符串  |        内存占用大小        |
|        **name**        |  字符串  |          Topic名           |
|       **count**        |   数字   |  已经插入条目数（估计值）  |
|       **exists**       |  逻辑值  |          是否存在          |
| **expectedInsertions** |   数字   |       预期插入条目数       |

##### 实例 3

`geek` 用户再次创建 Topic 为`twitter$s` 的Bloom Filter

<img src="https://i.loli.net/2020/05/07/y7kUwfBgJZbjOIQ.png" alt="avatar"  />

**说明**： Topic 为`twitter$s` 的Bloom Filter已经存在。



#### （二）查看某个Topic 的Bloom Filter详情

<table border="0" cellspacing="1" style="text-align:center">
    <tr>
        <th>接口</th>
        <td> /info </td>
    </tr>
	<tr>
        <th>方法</th>
        <td> post </td>
    </tr>
    <tr>
        <th>描述</th>
        <td>通过调用此接口查看某Topic下的Bloom Filter详情</th>
    </tr>
	<tr>
        <th>注意事项</th>
        <td>调用此接口前确保相应Topic下的Bloom Filter存在</th>
    </tr>
	<tr>
        <th>需要解色权限</th>
        <td>ROLE_NORMAL</th>
    </tr>
	<tr>
        <td> <b>参数 </b></td>
        <td>
        <table style="text-align:center">
            <tr>
                <th>名称</th>
                <th>类型</th>
                <th>是否必须</th>
                <th>描述</th>
            </tr>
            <tr>
                <td>topic</td>
                <td>字符串</td>
                <td>是</td>
                <td>需要查看的Bloom Filter的Topic</td>
            </tr>
        </table>
        </td>
    </tr>
	<tr>
        <td><b>可能返回的自定义状态码</b></td>
        <td>1 、401 、402</th>
    </tr>
	<tr>
        <td><b>请求成功时data字段说明</b></td>
        <td>
        <table style="text-align:center">
            <tr>
                <th>类型</th>
                <th>描述</th>
            </tr>
            <tr>
                <td>字符串</td>
                <td>描述该Bloom Filter详情的字符串</td>
            </tr>
        </table>
        </td>
    </tr>
</table>

##### 实例 4

`nicholas`用户调用`info` 接口查看Topic 为 `facebook$s`的Bloom Filter.

<img src="https://i.loli.net/2020/05/11/Y9cHnZIXw7jGvWu.png" alt="avatar"  />

**说明**： Topic 为`facebook$s` 的Bloom Filter不存在。

##### 实例 5 

`nicholas`用户调用`infoAll` 接口查看Topic 为 `facebook$s`的Bloom Filter.

<img src="https://i.loli.net/2020/05/11/vVXQguTr4zKUh7Y.png" alt="avatar"  />

**说明**： 请求成功， `data` 字段返回该Bloom Filter的详细信息。



#### （三）查看所有存在的Bloom Filter详情

<table border="0" cellspacing="1" style="text-align:center">
    <tr>
        <th>接口</th>
        <td> /infoAll </td>
    </tr>
	<tr>
        <th>方法</th>
        <td> post </td>
    </tr>
    <tr>
        <th>描述</th>
        <td>查看系统所有存在的Bloom Filter详情</th>
    </tr>
	<tr>
        <th>需要解色权限</th>
        <td>ROLE_NORMAL</th>
    </tr>
	<tr>
        <td><b>可能返回的自定义状态码</b></td>
        <td>1</th>
    </tr>
	<tr>
        <td><b>请求成功时data字段说明</b></td>
        <td>
        <table style="text-align:center">
            <tr>
                <th>类型</th>
                <th>描述</th>
            </tr>
            <tr>
                <td>字符串数组</td>
                <td>数组的每一项为一个Bloom Filter描述</td>
            </tr>
        </table>
        </td>
    </tr>
</table>

##### 实例 6

`nicholas`用户请求`info` 接口查看系统所有存在的Bloom Filter

<img src="https://i.loli.net/2020/05/11/z6xCF7cShrilogq.png" alt="avatar"  />

**说明** ： 系统中存在Topic 分别为`twitter$s` 和`geeks$s` 的Bloom Filter，返回数组中`data`数组第一项为关于Topic为`twitter$s`  的Bloom Filter的描述， 第二项为关于Topic为`geeks$s`   的Bloom Filter的描述。



#### （四）删除Bloom Filter

<table border="0" cellspacing="1" style="text-align:center">
    <tr>
        <th>接口</th>
        <td> /del </td>
    </tr>
	<tr>
        <th>方法</th>
        <td> post </td>
    </tr>
    <tr>
        <th>描述</th>
        <td>通过调用此接口删除某Topic下的Bloom Filter</th>
    </tr>
	<tr>
        <th>注意事项</th>
        <td>调用此接口前确保相应Topic下的Bloom Filter存在</th>
    </tr>
	<tr>
        <th>需要解色权限</th>
        <td>ROLE_ADMIN</th>
    </tr>
	<tr>
        <td> <b>参数 </b></td>
        <td>
        <table style="text-align:center">
            <tr>
                <th>名称</th>
                <th>类型</th>
                <th>是否必须</th>
                <th>描述</th>
            </tr>
            <tr>
                <td>topic</td>
                <td>字符串</td>
                <td>是</td>
                <td>需要创建Bloom Filter的Topic</td>
            </tr>
        </table>
        </td>
    </tr>
	<tr>
        <td><b>可能返回的自定义状态码</b></td>
        <td>1 、401 、402</th>
    </tr>
	<tr>
        <td><b>请求成功时data字段说明</b></td>
        <td>
        <table style="text-align:center">
            <tr>
                <th>类型</th>
                <th>描述</th>
            </tr>
            <tr>
                <td>逻辑值（true 或 false）</td>
                <td>为true时表示删除成功<br>为false时表示删除失败</td>
            </tr>
        </table>
        </td>
    </tr>
</table>

##### 实例 7

通过调用 `infoAll` 接口返回系统中存在Topic分别为`twitter$s` 和`geeks$s`的Bloom Filter， 现`nicholas`用户请求`del` 接口删除Topic为`twitter$s` 的Bloom Filter.

<img src="https://i.loli.net/2020/05/11/qLWsphQv8PIV5H4.png" alt="avatar"  />

**说明** ： `nicholas`用户无`ROLE_ADMIN` 权限。

##### 实例 8

`geek`用户请求`del` 接口删除Topic为`twitter$s` 的Bloom Filter.

<img src="https://i.loli.net/2020/05/11/bvhuIGTSnP5koVs.png" alt="avatar"  />

**说明** ： Topic 为`twitter$s` 的Bloom Filter 删除成功， 再次调用 `infoAll` 接口， 发现系统中只存在Topic为`geeks$s`的Bloom Filter。

#### （五）向Bloom Filter中添加条目

<table border="0" cellspacing="1" style="text-align:center">
    <tr>
        <th>接口</th>
        <td> /put </td>
    </tr>
	<tr>
        <th>方法</th>
        <td> post </td>
    </tr>
    <tr>
        <th>描述</th>
        <td>通过调用此接口向某Topic下的Bloom Filter添加条目</th>
    </tr>
	<tr>
        <th>注意事项</th>
        <td>调用此接口前确保相应Topic下的Bloom Filter存在</th>
    </tr>
	<tr>
        <th>需要解色权限</th>
        <td>ROLE_NORMAL</th>
    </tr>
	<tr>
        <td> <b>参数 </b></td>
        <td>
        <table style="text-align:center">
            <tr>
                <th>名称</th>
                <th>类型</th>
                <th>是否必须</th>
                <th>描述</th>
            </tr>
            <tr>
                <td>topic</td>
                <td>字符串</td>
                <td>是</td>
                <td>需要添加条目Bloom Filter的Topic</td>
            </tr>
            <tr>
                <td>url</td>
                <td>字符串</td>
                <td>是</td>
                <td>需要添加的条目</td>
            </tr>
        </table>
        </td>
    </tr>
	<tr>
        <td><b>可能返回的自定义状态码</b></td>
        <td>1 、401 、402</th>
    </tr>
	<tr>
        <td><b>请求成功时data字段说明</b></td>
        <td>
        <table style="text-align:center">
            <tr>
                <th>类型</th>
                <th>描述</th>
            </tr>
            <tr>
                <td>逻辑值（true 或 false）</td>
                <td>为true时表示删添加成功<br>否则表示该条目在相应Bloom Filter中已经存在</td>
            </tr>
        </table>
        </td>
    </tr>
</table>



##### 实例 9

用户`nicholas`调用/put接口，向Toipc为`geeks$s`的Bloom Filter中添加空字符串，或者不填写参数`url`.

<img src="https://i.loli.net/2020/05/11/Z1qxQNeUP4hBVpJ.png" alt="avatar"  />

**说明** ： 请求失败， 参数`url`不能为空。



##### 实例 10

用户`nicholas`调用/put接口，向Toipc为`geeks$s`的Bloom Filter中添加`"www.baidu.com"` 条目。

<img src="https://i.loli.net/2020/05/11/C63lGt4Xdz5EhUO.png" alt="avatar"  />

**说明** ： 添加成功。



#### （六）判断某Bloom Filter中是否包含某条目

<table border="0" cellspacing="1" style="text-align:center">
    <tr>
        <th>接口</th>
        <td> /contains </td>
    </tr>
	<tr>
        <th>方法</th>
        <td> post </td>
    </tr>
    <tr>
        <th>描述</th>
        <td>通过调用此接口判断某Topic下的Bloom Filter是否包含某条目</th>
    </tr>
	<tr>
        <th>注意事项</th>
        <td>调用此接口前确保相应Topic下的Bloom Filter存在</th>
    </tr>
	<tr>
        <th>需要解色权限</th>
        <td>ROLE_NORMAL</th>
    </tr>
	<tr>
        <td> <b>参数 </b></td>
        <td>
        <table style="text-align:center">
            <tr>
                <th>名称</th>
                <th>类型</th>
                <th>是否必须</th>
                <th>描述</th>
            </tr>
            <tr>
                <td>topic</td>
                <td>字符串</td>
                <td>是</td>
                <td>需要添加条目Bloom Filter的Topic</td>
            </tr>
            <tr>
                <td>url</td>
                <td>字符串</td>
                <td>是</td>
                <td>需要添加的条目</td>
            </tr>
        </table>
        </td>
    </tr>
	<tr>
        <td><b>可能返回的自定义状态码</b></td>
        <td>1 、401 、402</th>
    </tr>
	<tr>
        <td><b>请求成功时data字段说明</b></td>
        <td>
        <table style="text-align:center">
            <tr>
                <th>类型</th>
                <th>描述</th>
            </tr>
            <tr>
                <td>逻辑值（true 或 false）</td>
                <td>为true时相应Bloom Filter包含该条目<br>否则表示不包含</td>
            </tr>
        </table>
        </td>
    </tr>
</table>



##### 实例 11

用户`nicholas`调用/contains接口，判断Toipc为`geeks$s`的Bloom Filter中是否包含`"www.baidu.com"` 条目。

<img src="https://i.loli.net/2020/05/11/KrS1TGBgZyvJAIu.png" alt="avatar"  />

**说明** ： 请求成功， 因为向Topic为`geeks$s`的Bloom Filter中已经添加了`"www.baidu.com"` 条目， 所以存在。



##### 实例 12

用户`nicholas`调用/contains接口，判断Toipc为`geeks$s`的Bloom Filter中是否包含`"www.google.com"` 条目。

<img src="https://i.loli.net/2020/05/11/ZnjBygPD1527IuC.png" alt="avatar"  />

**说明** ： 请求成功， 因为未向Topic为`geeks$s`的Bloom Filter中已经添加了`"www.google.com"` 条目， 所以不存在。