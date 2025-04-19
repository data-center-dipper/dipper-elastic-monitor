#!/usr/bin/env bash

# 获取当前脚本所在目录的绝对路径
BASE_DIR=$(cd "$(dirname "$0")"/..; pwd)

export LANG=zh_CN.UTF-8
export LC_ALL=zh_CN.UTF-8

# 启动/调试/停止/重启
action=$1
# 启动环境,不同环境对应不同jvm参数;如果action为debug,则表示远程调试端口号
environment=$2

# 版本号
version="1.0-SNAPSHOT"
# 配置文件相对路径（相对于当前脚本所在目录）
conf_relative_path="conf/application.properties"

# 默认jvm参数
jvm_xms="256m"
jvm_xmx="256m"
# 远程调试默认端口号
debug_port=7780

# app相关信息
appName="monitor-service-provider" # 确保与你的artifactId一致
runJar="${appName}-${version}.jar"
mainClass="com.dipper.monitor.MonitorApplication" # 确保这是你的主类全限定名

# jar文件所在目录绝对路径
JAR_FOLDER="${BASE_DIR}/jars"
# jar文件绝对路径
JAR_PATH="${JAR_FOLDER}/${runJar}"
# 配置文件绝对路径（基于相对路径计算）
conf_path="${BASE_DIR}/${conf_relative_path}"

# 日志相关目录
LOGS_DIR="${BASE_DIR}/logs"
mkdir -p ${LOGS_DIR}
chmod 777 -R ${LOGS_DIR}
export LOG_PATH="${LOGS_DIR}"

# 设置jvm参数
set_jvm(){
    if [ "${environment}" = "test" ]; then
        jvm_xms="256m"
        jvm_xmx="256m"
    elif [ "${environment}" = "prd" ]; then
        jvm_xms="1024m"
        jvm_xmx="1024m"
    elif [ "${environment}" = "saas" ]; then
        jvm_xms="1024m"
        jvm_xmx="1024m"
    elif [ "${environment}" = "private" ]; then
        jvm_xms="512m"
        jvm_xmx="512m"
    else
        echo "使用默认jvm配置..."
    fi

    if [ "${action}" = "debug" ] && [ ! -z "${environment}" ]; then
        case "${environment}" in
            [1-9][0-9]*)
                debug_port=${environment}
                ;;
            *)
                ;;
        esac
        echo "远程调试端口号: ${debug_port}"
    fi

    # 添加 -Dapp.main.resource 参数
    app_main_resource="-Dapp.main.resource=${JAR_PATH}"

    # 启动命令（使用 java -jar 启动胖包）
  start_cmd="java ${app_main_resource} \
      -Djava.security.egd=file:/dev/./urandom \
      -Xms${jvm_xms} -Xmx${jvm_xmx} \
      -jar ${JAR_PATH} \
      --spring.config.location=${conf_path} \
      --logging.path=${LOG_PATH} \
      --logging.config=${BASE_DIR}/conf/logback.xml"

    # 调试命令（使用 java -jar 启动胖包）
    debug_cmd="java ${app_main_resource} \
        -Djava.security.egd=file:/dev/./urandom \
        -Xms${jvm_xms} -Xmx${jvm_xmx} \
        -Xdebug -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=${debug_port} \
        -jar ${JAR_PATH} \
        --spring.config.location=${conf_path} \
        --logging.path=${LOG_PATH} \
        --logging.config=${BASE_DIR}/conf/logback.xml"
}

# 启动
start(){
    echo "正在启动..."
    echo "Using JAR path: ${JAR_PATH}"
    echo "Using configuration path: ${conf_path}"
    echo "Start command: ${start_cmd}"
    nohup ${start_cmd} > ${LOGS_DIR}/nohup.out 2>&1 &
    sleep 5 # 给予一些时间让应用启动
    if ! pgrep -f "${mainClass}" > /dev/null; then
        echo "Failed to start the application, check ${LOGS_DIR}/nohup.out for details."
    else
        echo "启动完成."
    fi
}

# 远程调试
debug(){
    echo "正在启动调试模式..."
    echo "Using JAR path: ${JAR_PATH}"
    echo "Using configuration path: ${conf_path}"
    echo "Debug command: ${debug_cmd}"
    nohup ${debug_cmd} > ${LOGS_DIR}/nohup.out 2>&1 &
    sleep 5
    if ! pgrep -f "${mainClass}" > /dev/null; then
        echo "Failed to start the application in debug mode, check ${LOGS_DIR}/nohup.out for details."
    else
        echo "启动完成, 远程调试端口:${debug_port}."
    fi
}

# 停止
stop(){
    echo "正在停止服务..."
    # 查找符合mainClass和conf_path特征的java进程
    pids=$(jps -ml | grep "${mainClass}" | grep "${conf_path}" | awk '{print $1}')
    if [ -n "$pids" ]; then
        kill $pids
        echo "发送停止信号给PID: $pids"
        sleep 5 # 等待一段时间让进程有机会优雅地关闭

        # 检查每个pid是否已经停止，如果没有则强制终止
        for pid in $pids; do
            if kill -0 $pid > /dev/null 2>&1; then
                echo "进程$pid未响应，将强制终止."
                kill -9 $pid
            fi
        done

        echo "停止完成."
    else
        echo "没有找到运行中的服务."
    fi
}

# 重启
restart(){
    stop
    sleep 3
    start
}

# 判断是否是root账户启动进程
if_use_root(){
    user=$(whoami)
    if [ "$user" = "root" ]; then
        read -p "不建议使用 \"$user\" 账户启动服务, 确定要启动吗? (yes or no): " use_root
        case "${use_root}" in
            y|yes)
                echo "正在使用 \"$user\" 账户启动服务."
                ;;
            n|no)
                echo "退出,请切换到其他用户启动服务."
                exit 1
                ;;
            *)
                echo "请输入 yes 或 no"
                if_use_root
                ;;
        esac
    fi
}

# 检查必要文件是否存在
if [ ! -f "${JAR_PATH}" ]; then
    echo "${JAR_PATH} 不存在, 请检查启动脚本中的版本配置或检查文件是否存在."
    exit 1
fi

if [ ! -f "${conf_path}" ]; then
    echo "${conf_path} 不存在, 请检查启动脚本中的配置文件路径是否正确或检查文件是否存在."
    exit 1
fi

if [ ! -f "${BASE_DIR}/conf/logback.xml" ]; then
    echo "${BASE_DIR}/conf/logback.xml 不存在, 请检查日志配置文件是否正确."
    exit 1
fi

# 检查参数数量
if [ $# -lt 1 -o $# -gt 2 ]; then
    echo "运行命令:
\t$0 start
\t$0 stop
\t$0 debug
\t$0 restart
\t$0 start test
\t$0 start prd
\t$0 start saas
\t$0 start private
\t$0 debug port"
    exit 1
fi

# 根据动作执行相应操作
if [ "${action}" = "start" ]; then
    if_use_root
    set_jvm
    start
elif [ "${action}" = "stop" ]; then
    stop
elif [ "${action}" = "restart" ]; then
    if_use_root
    set_jvm
    restart
elif [ "${action}" = "debug" ]; then
    if_use_root
    set_jvm
    debug
else
    echo "不支持 ${action}, 只支持 [start, stop, debug, restart]"
    exit 1
fi

sleep 2

# 显示当前运行的Java进程
echo "当前运行的Java进程:"
jps -mlv | grep ${mainClass}