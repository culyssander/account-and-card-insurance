package com.santander.mspolicyservices.services;

import com.santander.mspolicyservices.constants.PolicyConstants;
import com.santander.mspolicyservices.dto.PolicyRequestDto;
import com.santander.mspolicyservices.dto.PolicyResponseDto;
import com.santander.mspolicyservices.exception.AccessDeniedException;
import com.santander.mspolicyservices.clients.ProductsServicesClients;
import com.santander.mspolicyservices.clients.UserServicesClients;
import com.santander.mspolicyservices.dto.ProductResponseDto;
import com.santander.mspolicyservices.dto.UserResponseDto;
import com.santander.mspolicyservices.exception.NotFoundException;
import com.santander.mspolicyservices.model.Policy;
import com.santander.mspolicyservices.model.PolicyStatus;
import com.santander.mspolicyservices.repository.PolicyRepository;
import com.santander.mspolicyservices.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PolicyServices {

    private PolicyRepository policyRepository;
    private ProductsServicesClients productsServicesClients;
    private ProtocolSequencialServicos protocolSequencialServicos;
    private UserServicesClients userServicesClients;
    private MessageSource messageSource;
    private JwtUtil jwtUtil;

    public PolicyResponseDto createPolicy(PolicyRequestDto policyRequest, HttpServletRequest request, Locale locale) {
        String productCode = policyRequest.getProductCode();
        UserResponseDto userLogado = getUserLogged(request, locale);
        ProductResponseDto response = getProductsByCode(productCode, locale);
        String protocol = protocolSequencialServicos.generateProtocol(productCode);

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

        return entityToDto(policy, response, userLogado);
    }

    private ProductResponseDto getProductsByCode(String code, Locale locale) {
        ProductResponseDto products = productsServicesClients.findProductsByCode(code);

        if (products == null) {
            throw new NotFoundException(messageSource.getMessage(PolicyConstants.POLICY_NOT_FOUND, new Object[] {}, locale));
        }

        return products;
    }

    private Optional<String> getToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return Optional.of(header.substring(7));
        }

        return Optional.empty();
    }

    private UserResponseDto getUserLogged(HttpServletRequest request, Locale locale) {
        Optional<String> token = getToken(request);

        if (token.isPresent()) {
            String username = jwtUtil.extractUsername(token.get());
            UserResponseDto user = userServicesClients.findByEmail(username);
            validateRole(user.getRole(), locale);
            return user;
        }

        throw new AccessDeniedException(messageSource.getMessage(PolicyConstants.POLICY_ACCESS_DENIED, new Object[] {}, locale));
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
                .orElseThrow(() -> new NotFoundException(messageSource.getMessage(PolicyConstants.POLICY_NOT_FOUND, new Object[] {}, locale)));
    }

    public PolicyResponseDto findByAdminOrAnalysis(String policyNumber, Locale locale) {
        Policy policy = findByPolicyNumber(policyNumber, locale);

        return mapClientes(policy);
    }

    public PolicyResponseDto findByInsured(String policyNumber,HttpServletRequest request, Locale locale) {
        UserResponseDto userLogged = getUserLogged(request, locale);
        validateRole(userLogged.getRole(), locale);
        Policy policy = findByCPFAndPolicyNumber(userLogged.getCpf(), policyNumber, locale);

        return mapClientes(policy);
    }

    private PolicyResponseDto mapClientes(Policy policy) {
        UserResponseDto userResponseDto = userServicesClients.findByCpf(policy.getCpf());
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
