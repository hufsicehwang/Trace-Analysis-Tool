package com.gwd.tracetool.controller;

import com.gwd.tracetool.domain.ApiModel;
import com.gwd.tracetool.domain.statistic.api.*;
import com.gwd.tracetool.service.ApiAnalysisService;
import com.gwd.tracetool.service.ApiParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/analysis/dags-log")
public class ApiStatisticController {
    private final ApiParserService apiParserService;
    private final ApiAnalysisService apiAnalysisService;

    @GetMapping("/time-consume")
    public String calcTime(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        TimeStatistic stat = apiAnalysisService.calcTime(apiParserService.readApiList(session.getAttribute("fileDate").toString()));
        model.addAttribute("data",stat);
        return "/api/consumeTime_table";
    }

    @GetMapping("/destination-host")
    public String calcDestinationHost(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        DestinationHostStatistic stat = apiAnalysisService.calcDestinationHost(apiParserService.readApiList(session.getAttribute("fileDate").toString()));
        model.addAttribute("data",stat);
        return "/api/destinationHost_table";
    }

    @GetMapping("/status-code")
    public String calcStatusCode(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        StatusCodeStatistic stat = apiAnalysisService.calcStatusCode(apiParserService.readApiList(session.getAttribute("fileDate").toString()));
        model.addAttribute("data",stat);
        return "/api/statusCode_table";
    }

    @GetMapping("/dags-host")
    public String calcDagsHost(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        DagsHostStatistic stat = apiAnalysisService.calcDagsHost(apiParserService.readApiList(session.getAttribute("fileDate").toString()));
        model.addAttribute("data",stat);
        return "api/dagsHostServer_table";
    }

    @GetMapping("/api-type")
    public String calcType(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        TypeStatistic stat = apiAnalysisService.calcType(apiParserService.readApiList(session.getAttribute("fileDate").toString()));
        model.addAttribute("data",stat);
        return "/api/apiType_table";
    }
}
