package com.demo.controller;

import com.demo.Service.impl.IInjuryRecordService;
import com.demo.dto.Result;
import com.demo.entity.InjuryRecord;
import com.demo.upload.exception.DataValidationException;
import com.demo.upload.service.ComprehensiveDataImportService;
import com.demo.utils.ExcelImportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.demo.upload.dto.ValidationErrorDTO;

@RestController
@RequestMapping("/api/file")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    @Autowired
    private IInjuryRecordService injuryRecordService;
    
    @Autowired
    private ComprehensiveDataImportService comprehensiveDataImportService;
    
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
            logger.info("=== 开始处理文件上传 ===");
            logger.info("文件名: {}", file.getOriginalFilename());
            logger.info("文件大小: {}", file.getSize());
            
            // 保存文件到服务器
            String savedFilePath = saveUploadedFile(file);
            logger.info("文件保存路径: {}", savedFilePath);
            
            // 调用批量导入服务进行数据导入（整合所有9张表）
            logger.info("=== 开始批量导入所有表的数据 ===");
            Map<String, Object> importResult = comprehensiveDataImportService.validateAndImportAllTables(savedFilePath);
            
            Boolean success = (Boolean) importResult.get("success");
            String message = (String) importResult.get("message");
            Integer totalErrorCount = (Integer) importResult.get("totalErrorCount");
            
            if (success != null && success) {
                logger.info("=== 导入成功，返回成功响应 ===");
                Map<String, Object> result = new HashMap<>();
                result.put("message", message);
                result.put("savedFilePath", savedFilePath);
                result.put("fileName", file.getOriginalFilename());
                result.put("fileSize", file.getSize());
                result.put("tables", importResult.get("tables"));
                
                // 返回前端期望的格式：{code: 200, data: {...}}
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("data", result);
                response.put("message", message);
                
                return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
            } else {
                logger.warn("=== 导入失败，返回失败响应 ===");
                logger.warn("总错误数: {}", totalErrorCount);
                
                // 如果导入失败，删除保存的文件
                deleteFile(savedFilePath);
                
                // 返回前端期望的格式：{code: 500, message: "...", errors: [...]}
                Map<String, Object> response = new HashMap<>();
                response.put("code", 500);
                response.put("message", message);
                response.put("totalErrorCount", totalErrorCount);
                Object errorsObj = importResult.get("allErrors");
                response.put("errors", errorsObj);

                // 生成错误导出CSV
                try {
                    if (errorsObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<ValidationErrorDTO> errors = (List<ValidationErrorDTO>) errorsObj;
                        String fileName = writeErrorsCsv(errors);
                        response.put("errorExportFile", fileName);
                        response.put("errorExportUrl", "/api/file/downloadErrorReport?file=" + fileName);
                    }
                } catch (Exception ex) {
                    logger.warn("生成错误导出文件失败: {}", ex.getMessage());
                }
                response.put("tables", importResult.get("tables"));
                
                return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
            }
        } catch (DataValidationException e) {
            logger.warn("=== 数据验证失败 ===");
            logger.warn("错误信息: {}", e.getMessage());
            
            // 从异常中提取错误详情
            Map<String, Object> errorDetails = e.getErrorDetails();
            
            // 返回前端期望的格式：{code: 500, message: "...", errors: [...], tables: {...}}
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", e.getMessage());
            response.put("totalErrorCount", errorDetails.get("totalErrorCount"));
            Object errorsObj = errorDetails.get("allErrors");
            response.put("errors", errorsObj);
            // 生成错误导出CSV
            try {
                if (errorsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<ValidationErrorDTO> errors = (List<ValidationErrorDTO>) errorsObj;
                    String fileName = writeErrorsCsv(errors);
                    response.put("errorExportFile", fileName);
                    response.put("errorExportUrl", "/api/file/downloadErrorReport?file=" + fileName);
                }
            } catch (Exception ex) {
                logger.warn("生成错误导出文件失败: {}", ex.getMessage());
            }
            response.put("tables", errorDetails.get("tables"));
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("=== 导入过程中发生异常 ===", e);
            
            // 返回前端期望的格式：{code: 500, message: "..."}
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "导入失败：" + e.getMessage());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @GetMapping("/downloadErrorReport")
    public ResponseEntity<Resource> downloadErrorReport(@RequestParam("file") String fileName) {
        try {
            Path exportDir = Paths.get(UPLOAD_DIR, "error-exports");
            Path filePath = exportDir.resolve(fileName);
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            FileSystemResource resource = new FileSystemResource(filePath.toFile());
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
     * 将错误列表写入CSV文件，返回生成的文件名
     */
    private String writeErrorsCsv(List<ValidationErrorDTO> errors) throws IOException {
        Path exportDir = Paths.get(UPLOAD_DIR, "error-exports");
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }
        String fileName = "import-errors-" + System.currentTimeMillis() + ".csv";
        Path csvPath = exportDir.resolve(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8)) {
            // 写入UTF-8 BOM，便于Excel正确识别编码
            writer.write('\uFEFF');
            // 表头
            writer.write("行号,患者ID,字段,错误值,错误原因");
            writer.newLine();
            if (errors != null) {
                for (ValidationErrorDTO err : errors) {
                    String row = err.getRow() == null ? "" : String.valueOf(err.getRow());
                    String patientId = err.getPatientId() == null ? "" : String.valueOf(err.getPatientId());
                    String field = safeCsv(err.getField());
                    String value = err.getValue() == null ? "" : safeCsv(String.valueOf(err.getValue()));
                    String message = safeCsv(err.getMessage());
                    writer.write(String.join(",", quote(row), quote(patientId), quote(field), quote(value), quote(message)));
                    writer.newLine();
                }
            }
        }
        return fileName;
    }

    private String safeCsv(String s) {
        if (s == null) return "";
        return s.replace("\r", " ").replace("\n", " ").trim();
    }

    private String quote(String s) {
        if (s == null) return "\"\"";
        // 如果包含逗号或引号，进行转义并包裹引号
        boolean needQuote = s.contains(",") || s.contains("\"");
        String escaped = s.replace("\"", "\"\"");
        return needQuote ? "\"" + escaped + "\"" : escaped;
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
