# weather-mcp

这是一个基于Spring AI实现的MCP (Model Context Protocol) 服务器项目，主要功能是通过爬取中国天气网获取实时天气数据。该项目可以作为AI模型和天气数据之间的桥梁，让AI模型能够查询中国的天气信息。

## 项目架构

本项目使用了以下技术栈：
- Spring Boot: 构建微服务的基础框架
- Spring AI: 提供与AI模型交互的能力
- Jsoup: 用于爬取网页数据
- Jackson: 处理JSON数据的序列化/反序列化

## 工具介绍

### 1. `getWeatherByCityAndDays`
```java
@Tool(description = "通过城市和天数查询未来的天气，最多7天，城市名用汉字，如合肥、广州")
public List<Map<String, String>> getWeatherByCityAndDays(String cityName, int days)
```


**功能**: 根据提供的城市名称和天数（最多7天），返回未来指定天数内的天气信息。

**参数**:
- `cityName`: 城市名称（中文）
- `days`: 需要查询的天数（最大为7）

**返回值**: 包含每天天气信息的列表，每个元素是一个`Map`，包含日期(`date`)、天气(`weather`)、温度(`temp`)和风向(`wind`)等键。

### 2. `getProvinceCities`
```java
@Tool(description = "查询省份的所有城市，请传入省份的拼音，如anhui")
public List<String> getProvinceCities(String provincePinyin)
```


**功能**: 根据提供的省份拼音，返回该省份下的所有城市名称。

**参数**:
- `provincePinyin`: 省份的拼音（例如"anhui"）

**返回值**: 包含城市名称的字符串列表。

### 3. `getWeatherForAllCitiesInProvince`
```java
@Tool(description = "查询省份所有城市的天气，请传入省份的拼音，如anhui")
public Map<String, List<Map<String, String>>> getWeatherForAllCitiesInProvince(String provincePinyin)
```


**功能**: 根据提供的省份拼音，返回该省份下所有城市的天气信息。

**参数**:
- `provincePinyin`: 省份的拼音（例如"anhui"）

**返回值**: 一个`Map`，其中键是城市名称，值是该城市的天气信息列表，每个元素是一个包含日期(`date`)、天气(`weather`)、温度(`temp`)和风向(`wind`)等键的`Map`。

## 使用说明

### 启动项目

确保你已经安装了JDK 21，并且配置好了环境变量。然后运行以下命令来启动应用：

```bash
./mvnw spring-boot:run
```


或者如果你已经打包好了jar文件，可以直接运行：

```bash
java -jar target/weather-mcp-0.0.1-SNAPSHOT.jar
```


### 访问接口

启动后，你可以通过HTTP请求访问这些工具提供的API端点。默认情况下，服务会在`http://localhost:8081`上运行。

## 贡献指南

欢迎贡献代码或提出问题！如果你有任何建议或发现bug，请提交Issue或Pull Request。

