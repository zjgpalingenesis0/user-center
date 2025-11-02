package com.zjg.usercenter.once;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;

import static com.alibaba.excel.cache.Ehcache.BATCH_COUNT;



@Slf4j
public class TableListener implements ReadListener<TableUserInfo> {

    /**
     * 这个每一条数据解析都会来调用
     * @param data
     * @param analysisContext
     */
    @Override
    public void invoke(TableUserInfo data, AnalysisContext analysisContext) {
        System.out.println(data);
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        System.out.println("已解析完成");
    }


}