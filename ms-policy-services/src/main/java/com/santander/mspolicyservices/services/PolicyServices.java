package com.santander.mspolicyservices.services;

import com.santander.mspolicyservices.clients.ProtocolSequencialClients;
import com.santander.mspolicyservices.constants.PolicyConstants;
import com.santander.mspolicyservices.dto.PolicyRequestDto;
import com.santander.mspolicyservices.dto.PolicyResponseDto;
import com.santander.mspolicyservices.exception.AccessDeniedException;
import com.santander.mspolicyservices.clients.ProductsServicesClients;
import com.santander.mspolicyservices.clients.UserServicesClients;
import com.santander.mspolicyservices.dto.ProductResponseDto;
import com.santander.mspolicyservices.dto.UserResponseDto;
import com.santander.mspolicyservices.exception.BusinessException;
import com.santander.mspolicyservices.exception.NotFoundException;
import com.santander.mspolicyservices.model.Policy;
import com.santander.mspolicyservices.model.PolicyStatus;
import com.santander.mspolicyservices.repository.PolicyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class PolicyServices {

    private PolicyRepository policyRepository;
    private ProductsServicesClients productsServicesClients;
    private ProtocolSequencialClients protocolSequencialClients;
    private UserServicesClients userServicesClients;
    private MessageSource messageSource;

    public PolicyResponseDto createPolicy(PolicyRequestDto policyRequest, Locale locale) {
        String productCode = policyRequest.getProductCode();
        UserResponseDto userLogado = getUserLogged(locale);
        ProductResponseDto response = getProductsByCode(productCode, locale);
        String protocol = protocolSequencialClients.generateProtocol(productCode);

        Policy policy = Policy.builder()
                .policyNumber(protocol)
                .productId(response.getId())
                .productId(response.getId())
                .productCode(productCode)
                .cpf(userLogado.getCpf())
                .status(PolicyStatus.DRAFT.name())
                .startDate(LocalDateTime.now())
                .build();

        policy = policyRepository.save(policy);

        PolicyResponseDto policyResponseDto = entityToDto(policy, response, userLogado);
        log.info("CREATE POLICY: {} ", policyResponseDto);
        return policyResponseDto;
    }

    private ProductResponseDto getProductsByCode(String code, Locale locale) {
        try {
            ProductResponseDto products = productsServicesClients.findProductsByCode(code);

            if (products == null) {
                throw new NotFoundException(messageSource.getMessage(PolicyConstants.POLICY_NOT_FOUND, new Object[] {}, locale));
            }

            return products;
        } catch (BusinessException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    private UserResponseDto getUserLogged(Locale locale) {
        try {
            UserResponseDto user = userServicesClients.findByUserLogged();
            log.info("GET USER LOGGED: {} ", user);
            if (Objects.nonNull(user)) {
                validateRole(user.getRole(), locale);
            }
            return user;
        } catch (AccessDeniedException e) {
            log.error("ERROR IN GET USER LOGGED: {}", e.getMessage());
            throw new AccessDeniedException(messageSource.getMessage(PolicyConstants.POLICY_ACCESS_DENIED, new Object[] {}, locale));
        }
    }

    private void validateRole(String role, Locale locale) {
        if (!role.equals("INSURED"))
            throw new AccessDeniedException(messageSource.getMessage(PolicyConstants.POLICY_ACCESS_DENIED, new Object[] {}, locale));
    }

    public Policy findByPolicyNumber(String policyNumber, Locale locale) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new NotFoundException(messageSource.getMessage(PolicyConstants.POLICY_NOT_FOUND, new Object[] {}, locale)));
    }

    public Policy findByCPFAndPolicyNumber(String cpf, String policyNumber, Locale locale) {
        return policyRepository.findByCpfAndPolicyNumber(cpf, policyNumber)
                .orElseThrow(() -> new NotFoundException(messageSource.getMessage(PolicyConstants.POLICY_NOT_FOUND, new Object[] {policyNumber}, locale)));
    }

    public PolicyResponseDto findByAdminOrAnalysis(String policyNumber, Locale locale) {
        Policy policy = findByPolicyNumber(policyNumber, locale);

        PolicyResponseDto policyResponseDto = mapClientes(policy);
        log.error("FIND POLICY ADMIN OR ANALYST: {}", policyResponseDto);
        return policyResponseDto;
    }

    public PolicyResponseDto findByInsured(String policyNumber, Locale locale) {

        UserResponseDto userLogged = getUserLogged(locale);
        validateRole(userLogged.getRole(), locale);
        Policy policy = findByCPFAndPolicyNumber(userLogged.getCpf(), policyNumber, locale);

        PolicyResponseDto policyResponseDto = mapClientes(policy);
        log.error("FIND POLICY INSURED: {}", policyResponseDto);
        return policyResponseDto;
    }

    private PolicyResponseDto mapClientes(Policy policy) {
        UserResponseDto userResponseDto = userServicesClients.findByInsuredLogged();
        ProductResponseDto productResponseDto = productsServicesClients.findProductsByCode(policy.getProductCode());

        return entityToDto(policy, productResponseDto, userResponseDto);
    }

    private PolicyResponseDto entityToDto(Policy policy, ProductResponseDto product, UserResponseDto user) {
        return PolicyResponseDto.builder()
                .policyNumber(policy.getPolicyNumber())
                .produtos(product)
                .cpf(user.getCpf())
                .status(policy.getStatus())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .build();
    }
}
