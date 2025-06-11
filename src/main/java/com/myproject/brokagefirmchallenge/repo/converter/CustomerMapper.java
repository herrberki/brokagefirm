package com.myproject.brokagefirmchallenge.repo.converter;
import com.myproject.brokagefirmchallenge.repo.entity.Customer;
import com.myproject.brokagefirmchallenge.repo.request.CreateCustomerRequest;
import com.myproject.brokagefirmchallenge.repo.vo.CustomerVO;
import org.mapstruct.*;

@Mapper(config = CentralMapperConfig.class)
public interface CustomerMapper extends BaseMapper<Customer, CreateCustomerRequest, CustomerVO> {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "isLocked", constant = "false")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "lastLoginDate", ignore = true)
    Customer toEntity(CreateCustomerRequest dto);

    @Mapping(target = "customerId", source = "id")
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "traceId", ignore = true)
    CustomerVO toVO(Customer entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(CreateCustomerRequest dto, @MappingTarget Customer entity);
}
