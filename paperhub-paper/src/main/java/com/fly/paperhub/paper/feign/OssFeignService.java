package com.fly.paperhub.paper.feign;

import org.hibernate.validator.constraints.URL;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("paperhub-thirdparty")
@Service
public interface OssFeignService {

    @PostMapping(value = "/oss/upload_paper_to_oss", consumes = "application/json")
    Map<String, Object> uploadPaperToOss(@RequestBody Map<String, Object> params);

}
