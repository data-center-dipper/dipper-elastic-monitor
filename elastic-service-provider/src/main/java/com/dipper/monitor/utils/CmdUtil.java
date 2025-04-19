package com.dipper.monitor.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdUtil {

    private static final Logger log = LoggerFactory.getLogger(CmdUtil.class);

    /**
     * 执行给定的命令并返回结果。
     *
     * @param filePath 文件路径，作为工作目录。
     * @param withLine 是否在每行后添加换行符。
     * @param cmd      要执行的命令。
     * @return 命令执行的结果。
     */
    public static String executeShell(File filePath, boolean withLine, String... cmd) {
        StringBuilder cmdResult = new StringBuilder();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.directory(filePath);
            Process process = processBuilder.redirectErrorStream(true).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));


            String line;
            while ((line = reader.readLine()) != null) {
                cmdResult.append(line);
                if (withLine) {
                    cmdResult.append(System.lineSeparator());
                }
            }

            log.info("Command output: {}", cmdResult.toString().trim());
            process.waitFor();

            log.info("Executed command - {} successfully", Arrays.toString(cmd));
        } catch (IOException | InterruptedException e) {
            log.warn("Failed to execute command {}: {}", Arrays.toString(cmd), e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        return cmdResult.toString().trim();
    }

    /**
     * 执行命令并返回是否成功完成。
     *
     * @param message  日志信息前缀。
     * @param command  要执行的命令。
     * @return 如果命令在指定时间内完成，则返回 true；否则 false。
     */
    public static boolean execCommond(String message, String... command) throws IOException, InterruptedException {
        try (InputStreamReader stdISR = new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream());
             BufferedReader stdBR = new BufferedReader(stdISR)) {

            String line;
            while ((line = stdBR.readLine()) != null) {
                log.info("{}执行结果: {}", message, line);
            }

            boolean exitValue = Runtime.getRuntime().exec(command).waitFor(300L, TimeUnit.SECONDS);
            log.info("{}执行结果 : {}", message, exitValue);
            return exitValue;
        } catch (IOException | InterruptedException e) {
            log.error("执行命令时发生异常: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 执行命令并返回命令的输出字符串。
     *
     * @param command 要执行的命令。
     * @return 命令执行的输出结果。
     */
    public static String execCommondReturnString(String command) throws IOException, InterruptedException {
        try (InputStreamReader stdISR = new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream());
             BufferedReader stdBR = new BufferedReader(stdISR)) {

            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = stdBR.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }

            boolean exitValue = Runtime.getRuntime().exec(command).waitFor(300L, TimeUnit.SECONDS);
            log.info("Script exited with: {}", exitValue);
            return builder.toString().trim();
        } catch (IOException | InterruptedException e) {
            log.error("执行命令时发生异常: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 检查环境变量 FLINK_MODE 是否为 'yarn'。
     *
     * @return 如果环境变量设置为 'yarn' 则返回 true；否则 false。
     */
    public static boolean flinkModeIsYarn() {
        try {
            String flinkMode = System.getenv("FLINK_MODE");
            if (StringUtils.isNotBlank(flinkMode)) {
                switch (flinkMode.toLowerCase()) {
                    case "yarn":
                        return true;
                    case "standalone":
                        return false;
                    default:
                        log.error("未知的 Flink 模式: {}", flinkMode);
                        break;
                }
            }
        } catch (Exception e) {
            log.error("获取环境变量 FLINK_MODE 时发生异常:", e);
        }
        return false;
    }
}