package com.demo.controller;

import com.demo.Service.impl.IInjuryRecordService;
import com.demo.dto.Result;
import com.demo.entity.InjuryRecord;
import com.demo.utils.ExcelImportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    private IInjuryRecordService injuryRecordService;
    
    // 文件上传目录
    private static final String UPLOAD_DIR = "uploads/";
    
    @PostMapping("/uploadInjuryRecordExcel")
    public Result uploadInjuryRecordExcel(@RequestParam("file") MultipartFile file) {
        try {
            ExcelImportUtil.importExcelToDb(file, injuryRecordService, InjuryRecord.class);
            return Result.ok("导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("导入失败：" + e.getMessage());
        }
    }
    
    @PostMapping("/uploadPatientExcel")
    public ResponseEntity<Map<String, Object>> uploadPatientExcel(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== 开始处理文件上传 ===");
            System.out.println("文件名: " + file.getOriginalFilename());
            System.out.println("文件大小: " + file.getSize());
            
            // 保存文件到服务器
            String savedFilePath = saveUploadedFile(file);
            System.out.println("文件保存路径: " + savedFilePath);
            
            // 调用Python脚本进行数据导入
            System.out.println("=== 开始调用Python脚本 ===");
            boolean importSuccess = callPythonImportScript(savedFilePath);
            System.out.println("=== Python脚本调用结果: " + importSuccess + " ===");
            
            if (importSuccess) {
                System.out.println("=== 导入成功，返回成功响应 ===");
                Map<String, Object> result = new HashMap<>();
                result.put("message", "患者信息导入成功");
                result.put("savedFilePath", savedFilePath);
                result.put("fileName", file.getOriginalFilename());
                result.put("fileSize", file.getSize());
                
                // 返回前端期望的格式：{code: 200, data: {...}}
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("data", result);
                response.put("message", "数据导入成功");
                
                return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
            } else {
                System.out.println("=== 导入失败，返回失败响应 ===");
                // 如果导入失败，删除保存的文件
                deleteFile(savedFilePath);
                
                // 返回前端期望的格式：{code: 500, message: "..."}
                Map<String, Object> response = new HashMap<>();
                response.put("code", 500);
                response.put("message", "数据导入失败，请检查Excel文件格式");
                
                return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            System.err.println("=== 导入过程中发生异常 ===");
            e.printStackTrace();
            
            // 返回前端期望的格式：{code: 500, message: "..."}
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "导入失败：" + e.getMessage());
            
            return new org.springframework.http.ResponseEntity<>(response, org.springframework.http.HttpStatus.OK);
        }
    }
    
    /**
     * 保存上传的文件到服务器
     * @param file 上传的文件
     * @return 保存后的文件路径
     * @throws IOException
     */
    private String saveUploadedFile(MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // 保存文件
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath);
        
        return filePath.toString();
    }
    
    /**
     * 调用Python脚本进行数据导入
     * @param excelFilePath Excel文件路径
     * @return 导入是否成功
     */
    private boolean callPythonImportScript(String excelFilePath) {
        Process process = null;
        
        try {
            // Python脚本路径
            String pythonScriptPath = "F:/Healthineers-visualization/python_data/v2/sum_import.py";
            
            // 使用正确的Python路径（包含requests包的Python环境）
            String pythonPath = "C:/Users/titk/AppData/Local/Programs/Python/Python310/python.exe";
            
            // 将相对路径转换为绝对路径
            String absoluteExcelPath;
            if (excelFilePath.startsWith("uploads/") || excelFilePath.startsWith("uploads\\")) {
                // 如果是相对路径，转换为绝对路径
                absoluteExcelPath = "F:/Healthineers-visualization/back-end/healthineers-visualization/" + excelFilePath.replace("\\", "/");
            } else {
                absoluteExcelPath = excelFilePath;
            }
            
            System.out.println("传递给Python脚本的文件路径: " + absoluteExcelPath);
            
            // 构建命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                pythonPath, 
                pythonScriptPath, 
                absoluteExcelPath
            );
            
            // 设置工作目录
            processBuilder.directory(new File("F:/Healthineers-visualization"));
            
            // 启动进程
            process = processBuilder.start();
            
            // 读取输出 - 使用GBK编码处理Windows下的中文输出
            final BufferedReader finalReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            final BufferedReader finalErrorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "GBK"));
            
            final StringBuilder output = new StringBuilder();
            final StringBuilder errorOutput = new StringBuilder();
            
            // 使用线程来读取输出，避免阻塞
            Thread outputThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = finalReader.readLine()) != null) {
                        output.append(line).append("\n");
                        System.out.println("Python输出: " + line);
                    }
                } catch (IOException e) {
                    System.err.println("读取输出流失败: " + e.getMessage());
                }
            });
            
            Thread errorThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = finalErrorReader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                        System.err.println("Python错误: " + line);
                    }
                } catch (IOException e) {
                    System.err.println("读取错误流失败: " + e.getMessage());
                }
            });
            
            // 启动读取线程
            outputThread.start();
            errorThread.start();
            
            // 等待进程完成，设置超时时间（5分钟）
            boolean finished = process.waitFor(5, java.util.concurrent.TimeUnit.MINUTES);
            
            if (!finished) {
                System.err.println("Python脚本执行超时，强制终止进程");
                process.destroyForcibly();
                return false;
            }
            
            // 等待读取线程完成
            outputThread.join(1000); // 最多等待1秒
            errorThread.join(1000);
            
            int exitCode = process.exitValue();
            
            System.out.println("Python脚本执行完成，退出码: " + exitCode);
            System.out.println("输出: " + output.toString());
            if (errorOutput.length() > 0) {
                System.err.println("错误输出: " + errorOutput.toString());
            }
            
            // 完全基于退出码判断成功/失败，避免字符编码问题
            String outputStr = output.toString();
            String errorStr = errorOutput.toString();
            
            System.out.println("=== Python脚本执行结果分析 ===");
            System.out.println("退出码: " + exitCode);
            System.out.println("标准输出: " + outputStr);
            System.out.println("错误输出: " + errorStr);
            
            // 多重判断逻辑：退出码 + 输出内容
            boolean isSuccess = false;
            
            // 1. 首先检查退出码（最可靠）
            if (exitCode == 0) {
                isSuccess = true;
                System.out.println("✅ 退出码为0，判断为成功");
            } else {
                System.out.println("❌ 退出码非0: " + exitCode);
            }
            
            // 2. 如果退出码为0，再检查输出内容作为双重确认
            if (isSuccess) {
                // 检查输出中是否包含成功标识（不区分大小写，处理编码问题）
                String outputLower = outputStr.toLowerCase();
                boolean hasSuccessKeyword = outputLower.contains("success") || 
                                          outputLower.contains("成功") ||
                                          outputLower.contains("完成");
                
                if (hasSuccessKeyword) {
                    System.out.println("✅ 输出内容包含成功标识，确认成功");
                } else {
                    System.out.println("⚠️ 退出码为0但输出内容未明确显示成功，仍按成功处理");
                }
            }
            
            System.out.println("=== 最终判断结果 ===");
            System.out.println("退出码: " + exitCode);
            System.out.println("判断结果: " + isSuccess);
            
            if (isSuccess) {
                System.out.println("✅ Python脚本执行成功，数据导入完成");
            } else {
                System.out.println("❌ Python脚本执行失败，退出码: " + exitCode);
            }
            
            return isSuccess;
            
        } catch (Exception e) {
            System.err.println("调用Python脚本失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // 确保资源被正确关闭
            try {
                if (process != null) {
                    process.getInputStream().close();
                    process.getOutputStream().close();
                    process.getErrorStream().close();
                }
            } catch (IOException e) {
                System.err.println("关闭资源失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 删除文件
     * @param filePath 文件路径
     */
    private void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("删除文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试Python脚本执行（使用真实文件）
     */
    @PostMapping("/testPythonWithFile")
    public Result testPythonWithFile(@RequestParam("file") MultipartFile file) {
        try {
            // 保存文件到服务器
            String savedFilePath = saveUploadedFile(file);
            
            // 调用Python脚本进行测试
            boolean importSuccess = callPythonImportScript(savedFilePath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("filePath", savedFilePath);
            result.put("importSuccess", importSuccess);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            
            if (importSuccess) {
                result.put("message", "Python脚本测试成功");
                return Result.ok(result);
            } else {
                result.put("message", "Python脚本测试失败");
                return Result.fail("Python脚本测试失败");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 测试Python脚本连接
     */
    @GetMapping("/testPython")
    public Result testPythonScript() {
        try {
            // 检查Python脚本文件是否存在
            String pythonScriptPath = "F:/Healthineers-visualization/python_data/v2/sum_import.py";
            File scriptFile = new File(pythonScriptPath);
            
            if (!scriptFile.exists()) {
                return Result.fail("Python脚本文件不存在: " + pythonScriptPath);
            }
            
            // 使用正确的Python路径
            String pythonPath = "C:/Users/titk/AppData/Local/Programs/Python/Python310/python.exe";
            
            // 测试Python命令是否可用
            ProcessBuilder testBuilder = new ProcessBuilder(pythonPath, "--version");
            Process testProcess = testBuilder.start();
            int testExitCode = testProcess.waitFor();
            
            if (testExitCode != 0) {
                return Result.fail("Python环境不可用，请检查Python安装");
            }
            
            // 测试脚本语法
            ProcessBuilder syntaxBuilder = new ProcessBuilder(pythonPath, "-m", "py_compile", pythonScriptPath);
            Process syntaxProcess = syntaxBuilder.start();
            int syntaxExitCode = syntaxProcess.waitFor();
            
            if (syntaxExitCode != 0) {
                return Result.fail("Python脚本语法错误");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Python脚本环境正常");
            result.put("scriptPath", pythonScriptPath);
            result.put("scriptExists", scriptFile.exists());
            result.put("scriptSize", scriptFile.length());
            
            return Result.ok(result);
            
        } catch (Exception e) {
            return Result.fail("Python脚本测试失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取上传文件列表
     */
    @GetMapping("/list")
    public Result getUploadedFiles() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                return Result.ok(new File[0]);
            }
            
            File[] files = uploadPath.toFile().listFiles();
            return Result.ok(files != null ? files : new File[0]);
        } catch (Exception e) {
            return Result.fail("获取文件列表失败：" + e.getMessage());
        }
    }
}
