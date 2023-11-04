package com.cbg.reggie.controller;

import com.cbg.common.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传和下载
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //file是临时文件，本次请求完成后自动删除，需要转存到指定位置
        log.info(file.toString());

        //获取原始文件名
        String originalFilename = file.getOriginalFilename();
        //截取文件后缀 输出带点
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID生成文件名，防止文件名重复造成覆盖
        String fileName = UUID.randomUUID() + suffix;

        //判断当前目录是否存在
        File dir = new File(basePath);
        if (!dir.exists()) {
            //目录不存在,则创建
            dir.mkdirs();
        }

        try {
            //将临时文件存到指定位置
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    /**
     * 文件下载
     *
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            //输入流读取文件
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //输出流将文件写回游览器，在游览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            //关闭资源
            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
