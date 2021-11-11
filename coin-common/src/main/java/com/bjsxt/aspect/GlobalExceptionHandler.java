package com.bjsxt.aspect;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.api.IErrorCode;
import com.baomidou.mybatisplus.extension.exceptions.ApiException;
import com.bjsxt.model.R;

import com.bjsxt.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @Autowired
    private LogUtil<JSONObject> kafkaSender;

    /**
     * 1 内部API调用的异常处理
     */
    @ExceptionHandler(value = ApiException.class)
    public R handlerApiException(ApiException exception){
        IErrorCode errorCode = exception.getErrorCode();
        if(errorCode!=null){
            return R.fail(errorCode) ;
        }
        return R.fail(exception.getMessage()) ;
    }

    /**
     * 2 方法参数校验失败的异常
     * MethodArgumentNotValidException
     *
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handlerMethodArgumentNotValidException(MethodArgumentNotValidException exception){
        BindingResult bindingResult = exception.getBindingResult();
        if(bindingResult.hasErrors()){
            FieldError fieldError = bindingResult.getFieldError();
            if(fieldError!=null){
                return R.fail(fieldError.getField()+fieldError.getDefaultMessage()) ;
            }
        }
        return R.fail(exception.getMessage()) ;
    }

    /**
     * 3 对象内部使用Validate 没有校验成功的异常
     */
    @ExceptionHandler(BindException.class)
    public R handlerBindException(BindException bindException){
        BindingResult bindingResult = bindException.getBindingResult();
        if(bindingResult.hasErrors()){
            FieldError fieldError = bindingResult.getFieldError();
            if(fieldError!=null){
                return R.fail(fieldError.getField()+fieldError.getDefaultMessage()) ;
            }
        }
        return R.fail(bindException.getMessage()) ;
    }
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public JSONObject exceptionHandler(Exception e) {
        log.info("###全局捕获异常###,error:{}", e);

        // 1.封装异常日志信息
        JSONObject errorJson = new JSONObject();
        JSONObject logJson = new JSONObject();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
        logJson.put("request_time", df.format(new Date()));
        logJson.put("error_info", e);

        errorJson.put("request_error", logJson);
        kafkaSender.sendLogMessage(errorJson);
        // 2. 返回错误信息
        JSONObject result = new JSONObject();
        result.put("code", 500);
        result.put("msg", "系统错误");

        // 参数中记录下，IP和端口号
        return result;
    }

}
