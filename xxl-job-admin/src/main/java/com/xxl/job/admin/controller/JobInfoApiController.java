package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.thread.JobTriggerPoolHelper;
import com.xxl.job.admin.core.trigger.TriggerTypeEnum;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.GsonTool;
import com.xxl.job.core.util.XxlJobRemotingUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyright (C), 2021, 北京同创永益科技发展有限公司
 *
 * @author tanyuanzhi
 * @version 3.0.0
 * @description
 * @date 2021/12/13 17:20
 */
@Controller
@RequestMapping("/jobinfo/api")
public class JobInfoApiController {

    @Resource
    private XxlJobService xxlJobService;


    @Resource
    private XxlJobGroupDao xxlJobGroupDao;

    /**
     * api
     *
     * @param uri  url
     * @param data 数据
     * @return
     */
    @RequestMapping("/{uri}")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> api(HttpServletRequest request, @PathVariable("uri") String uri, @RequestBody(required = false) String data) {

        // valid
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, HttpMethod not support.");
        }
        if (uri == null || uri.trim().length() == 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping empty.");
        }
        if (XxlJobAdminConfig.getAdminConfig().getAccessToken() != null
                && XxlJobAdminConfig.getAdminConfig().getAccessToken().trim().length() > 0
                && !XxlJobAdminConfig.getAdminConfig().getAccessToken().equals(request.getHeader(XxlJobRemotingUtil.XXL_JOB_ACCESS_TOKEN))) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "The access token is wrong.");
        }

        // services mapping
        if ("add".equals(uri)) {
            XxlJobInfo xxlJobInfo = GsonTool.fromJson(data, XxlJobInfo.class);
            String appname = xxlJobInfo.getAppname();
            if (appname == null || appname.length() == 0) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_choose")+I18nUtil.getString("jobinfo_field_jobgroup")) );
            }
            List<XxlJobGroup> xxlJobGroups = xxlJobGroupDao.pageList(0, 1, appname, null);
            if (CollectionUtils.isEmpty(xxlJobGroups)) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_choose")+I18nUtil.getString("jobinfo_field_jobgroup")) );
            }
            xxlJobInfo.setJobGroup(xxlJobGroups.get(0).getId());
            return xxlJobService.add(xxlJobInfo);
        } else if ("update".equals(uri)) {
            XxlJobInfo xxlJobInfo = GsonTool.fromJson(data, XxlJobInfo.class);
            String appname = xxlJobInfo.getAppname();
            if (appname == null || appname.length() == 0) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_choose")+I18nUtil.getString("jobinfo_field_jobgroup")) );
            }
            List<XxlJobGroup> xxlJobGroups = xxlJobGroupDao.pageList(0, 1, appname, null);
            if (CollectionUtils.isEmpty(xxlJobGroups)) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, (I18nUtil.getString("system_please_choose")+I18nUtil.getString("jobinfo_field_jobgroup")) );
            }
            xxlJobInfo.setJobGroup(xxlJobGroups.get(0).getId());
            return xxlJobService.update(xxlJobInfo);
        } else if ("remove".equals(uri)) {
            return xxlJobService.remove(Integer.parseInt(data));
        } else if ("start".equals(uri)) {
            return xxlJobService.start(Integer.parseInt(data));
        } else if ("stop".equals(uri)) {
            return xxlJobService.stop(Integer.parseInt(data));
        } else if ("trigger".equals(uri)) {
            int id = Integer.parseInt(data);
            XxlJobInfo xxlJobInfo = xxlJobService.getById(id);
            JobTriggerPoolHelper.trigger(id, TriggerTypeEnum.MANUAL, -1, null, xxlJobInfo.getExecutorParam(), null);
            return ReturnT.SUCCESS;
        } else if ("batchRemove".equals(uri)) {
            Integer[] ids = GsonTool.fromJson(data, Integer[].class);
            return xxlJobService.batchRemove(Arrays.asList(ids));
        } else {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping(" + uri + ") not found.");
        }

    }

}
