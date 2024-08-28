# ******前端项目******

## 启动项目
1. 设置npm源： ```npm config set registry https://registry.anpm.alibaba-inc.com/ ```
2. 安装依赖。在项目根目录下运行： ```npm install --registry=https://registry.anpm.alibaba-inc.com/ ```
3. 运行或打包：
    - npm run dev  启动本地开发环境
    - npm run build 打包项目到java项目目录
    - 在my-check 下用maven 打包为my-check.jar
    - 在有安装java1.8的环境下 java -jar my-check.jar 会自动打开浏览器跳转至应用页
