package com.santander.mspolicyservices.clients;

import com.santander.mspolicyservices.dto.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "productsServicesClients", url = "${products.service.url}")
public interface ProductsServicesClients {

    @GetMapping(path = "/{code}")
    ProductResponseDto findProductsByCode(@PathVariable String code);
}
