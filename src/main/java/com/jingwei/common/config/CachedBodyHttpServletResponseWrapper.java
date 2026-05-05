package com.jingwei.common.config;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 可缓存响应体的 HttpServletResponse 包装器
 * <p>
 * 用于幂等拦截器：在响应写入时同时缓存一份副本，
 * 请求完成后将缓存的响应体存入 Redis，供重复请求直接返回。
 * </p>
 *
 * @author JingWei
 */
public class CachedBodyHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream cachedBody = new ByteArrayOutputStream();
    private ServletOutputStream outputStream;
    private PrintWriter writer;

    public CachedBodyHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new CachedServletOutputStream(super.getOutputStream(), cachedBody);
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(
                    new TeeOutputStream(super.getWriter(), cachedBody), StandardCharsets.UTF_8));
        }
        return writer;
    }

    /**
     * 获取缓存的响应体内容
     *
     * @return 响应体字符串
     */
    public String getBody() {
        return cachedBody.toString(StandardCharsets.UTF_8);
    }

    /**
     * 同时写入原始输出流和缓存的 ServletOutputStream
     */
    private static class CachedServletOutputStream extends ServletOutputStream {
        private final ServletOutputStream original;
        private final ByteArrayOutputStream cache;

        CachedServletOutputStream(ServletOutputStream original, ByteArrayOutputStream cache) {
            this.original = original;
            this.cache = cache;
        }

        @Override
        public void write(int b) throws IOException {
            original.write(b);
            cache.write(b);
        }

        @Override
        public boolean isReady() {
            return original.isReady();
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            original.setWriteListener(listener);
        }
    }

    /**
     * 同时写入原始 PrintWriter 和缓存的 OutputStream
     */
    private static class TeeOutputStream extends java.io.OutputStream {
        private final PrintWriter original;
        private final ByteArrayOutputStream cache;

        TeeOutputStream(PrintWriter original, ByteArrayOutputStream cache) {
            this.original = original;
            this.cache = cache;
        }

        @Override
        public void write(int b) throws IOException {
            original.write(b);
            cache.write(b);
            original.flush();
        }
    }
}
