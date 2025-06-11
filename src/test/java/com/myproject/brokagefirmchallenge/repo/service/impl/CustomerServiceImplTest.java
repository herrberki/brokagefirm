package com.myproject.brokagefirmchallenge.repo.service.impl;

import com.myproject.brokagefirmchallenge.repo.entity.Customer;
import com.myproject.brokagefirmchallenge.repo.enumtype.AuditAction;
import com.myproject.brokagefirmchallenge.repo.exceptions.*;
import com.myproject.brokagefirmchallenge.repo.repository.CustomerRepository;
import com.myproject.brokagefirmchallenge.repo.service.AuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock
    private AuditService auditService;
    @Spy
    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    @DisplayName("should_createCustomer_success_inOrder")
    void should_createCustomer_success_inOrder() {
        // given
        Customer input = new Customer();
        input.setUsername("u");
        input.setEmail("e@x.com");
        input.setPassword("raw");
        when(customerRepository.existsByUsername("u")).thenReturn(false);
        when(customerRepository.existsByEmail("e@x.com")).thenReturn(false);
        when(passwordEncoder.encode("raw")).thenReturn("enc");
        Customer saved = new Customer();
        saved.setId(1L);
        when(customerRepository.save(input)).thenReturn(saved);
        // when
        Customer result = customerService.createCustomer(input);
        // then
        assertThat(result).isSameAs(saved);
        InOrder inOrder = inOrder(customerRepository, passwordEncoder, customerRepository, auditService);
        inOrder.verify(customerRepository).existsByUsername("u");
        inOrder.verify(customerRepository).existsByEmail("e@x.com");
        inOrder.verify(passwordEncoder).encode("raw");
        inOrder.verify(customerRepository).save(input);
        inOrder.verify(auditService).auditCreate("Customer", 1L, input);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("should_createCustomer_dupUsername_throwDuplicateUsernameException")
    void should_createCustomer_dupUsername_throw() {
        // given
        Customer c = new Customer();
        c.setUsername("u");
        when(customerRepository.existsByUsername("u")).thenReturn(true);
        // when // then
        assertThrows(DuplicateUsernameException.class, () -> customerService.createCustomer(c));
        verify(customerRepository).existsByUsername("u");
        verifyNoMoreInteractions(customerRepository, passwordEncoder, auditService);
    }

    @Test
    @DisplayName("should_createCustomer_dupEmail_throwDuplicateEmailException")
    void should_createCustomer_dupEmail_throw() {
        // given
        Customer c = new Customer();
        c.setUsername("u");
        c.setEmail("e@x.com");
        when(customerRepository.existsByUsername("u")).thenReturn(false);
        when(customerRepository.existsByEmail("e@x.com")).thenReturn(true);
        // when // then
        assertThrows(DuplicateEmailException.class, () -> customerService.createCustomer(c));
        InOrder in = inOrder(customerRepository);
        in.verify(customerRepository).existsByUsername("u");
        in.verify(customerRepository).existsByEmail("e@x.com");
        verifyNoMoreInteractions(passwordEncoder, auditService);
    }

    @Test
    @DisplayName("should_updateCustomer_success_inOrder")
    void should_updateCustomer_success_inOrder() {
        // given
        Long id = 5L;
        Customer existing = new Customer();
        existing.setId(id);
        existing.setUsername("u");
        existing.setEmail("old@x.com");
        existing.setFullName("Old");
        existing.setPhoneNumber("000");
        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByEmail("new@x.com")).thenReturn(false);
        when(customerRepository.save(existing)).thenReturn(existing);
        Customer update = new Customer();
        update.setEmail("new@x.com");
        update.setFullName("New");
        update.setPhoneNumber("111");
        // when
        Customer result = customerService.updateCustomer(id, update);
        // then
        assertThat(result).isSameAs(existing);
        assertThat(existing.getEmail()).isEqualTo("new@x.com");
        assertThat(existing.getFullName()).isEqualTo("New");
        assertThat(existing.getPhoneNumber()).isEqualTo("111");
        InOrder in = inOrder(customerRepository, customerRepository, customerRepository, auditService);
        in.verify(customerRepository).findById(id);
        in.verify(customerRepository).existsByEmail("new@x.com");
        in.verify(customerRepository).save(existing);
        in.verify(auditService).auditUpdate(eq("Customer"), eq(id), any(Customer.class), eq(existing));
        in.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("should_updateCustomer_notFound_throwCustomerNotFoundException")
    void should_updateCustomer_notFound_throw() {
        // given
        when(customerRepository.findById(7L)).thenReturn(Optional.empty());
        // when // then
        assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomer(7L, new Customer()));
        verify(customerRepository).findById(7L);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    @DisplayName("should_changePassword_success_inOrder")
    void should_changePassword_success_inOrder() {
        // given
        Long id = 8L;
        Customer cust = new Customer();
        cust.setId(id);
        cust.setPassword("encOld");
        when(customerRepository.findById(id)).thenReturn(Optional.of(cust));
        when(passwordEncoder.matches("old", "encOld")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("encNew");
        when(customerRepository.save(cust)).thenReturn(cust);
        // when
        Customer result = customerService.changePassword(id, "old", "new");
        // then
        assertThat(result).isSameAs(cust);
        assertThat(cust.getPassword()).isEqualTo("encNew");
        InOrder in = inOrder(customerRepository, passwordEncoder, passwordEncoder, customerRepository, auditService);
        in.verify(customerRepository).findById(id);
        in.verify(passwordEncoder).matches("old", "encOld");
        in.verify(passwordEncoder).encode("new");
        in.verify(customerRepository).save(cust);
        in.verify(auditService).auditAction(AuditAction.PASSWORD_CHANGED, "Customer", id, null, null);
        in.verifyNoMoreInteractions();
    }
}