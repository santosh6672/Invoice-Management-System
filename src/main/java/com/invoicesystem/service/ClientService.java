package com.invoicesystem.service;

import com.invoicesystem.dto.ClientDto;
import com.invoicesystem.model.Client;
import com.invoicesystem.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    public java.util.List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client createClient(ClientDto.Request request) {
        if (clientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Client with email already exists: " + request.getEmail());
        }

        Client client = Client.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .companyName(request.getCompanyName())
                .taxNumber(request.getTaxNumber())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .paymentTermsDays(request.getPaymentTermsDays())
                .build();

        Client saved = clientRepository.save(client);
        log.info("Client created: {}", saved.getEmail());
        return saved;
    }

    public Client updateClient(Long id, ClientDto.Request request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + id));

        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setCompanyName(request.getCompanyName());
        client.setTaxNumber(request.getTaxNumber());
        client.setAddress(request.getAddress());
        client.setCity(request.getCity());
        client.setCountry(request.getCountry());
        client.setPostalCode(request.getPostalCode());
        client.setPaymentTermsDays(request.getPaymentTermsDays());

        return clientRepository.save(client);
    }

    public void deactivateClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + id));
        client.setActive(false);
        clientRepository.save(client);
        log.info("Client deactivated: {}", id);
    }

    @Transactional(readOnly = true)
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Client> getActiveClients(Pageable pageable) {
        return clientRepository.findByActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Client> searchClients(String query, Pageable pageable) {
        return clientRepository.searchClients(query, pageable);
    }
}
